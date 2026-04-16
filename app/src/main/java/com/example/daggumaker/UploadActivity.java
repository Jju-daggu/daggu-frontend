package com.example.daggumaker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UploadActivity extends AppCompatActivity {

    // ✨ 1순위부터 차례대로 시도할 모델 리스트 (요청하신 폴백 체인 적용!)
    private static final String[] FALLBACK_MODELS = {
            "gemini-3.1-pro-preview",         // 1순위: 최신, 최강
            "gemini-3.0-pro",                 // 2순위: 이전 세대 Pro, 더 안정적
            "gemini-2.5-flash-image-preview", // 3순위: Flash 버전, 빠름
            "gemini-2.5-flash"                // 최종: 가장 안정적인 Flash
    };

    private String currentPhotoPath;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;

    private ImageView ivPreview;
    private TextView tvPreviewText;
    private Bitmap selectedBitmap; // 선택된 이미지 저장용
    private View pbScanning;
    private View llScanBtnContent;

    // 선택된 이미지의 위치(Uri)를 저장할 변수
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // 1. 뷰 초기화
        ivPreview = findViewById(R.id.iv_preview);
        tvPreviewText = findViewById(R.id.tv_preview_text);
        pbScanning = findViewById(R.id.pb_scanning);
        llScanBtnContent = findViewById(R.id.ll_scan_btn_content);

        // 2. [카메라] 런처 설정
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result) {
                        selectedImageUri = Uri.fromFile(new File(currentPhotoPath));
                        setPicFromFile();
                    }
                }
        );

        // 3. [갤러리] 런처 설정
        pickMediaLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        setPicFromUri(uri);
                    }
                }
        );

        // 4. 클릭 리스너 설정
        View cameraBtn = findViewById(R.id.cv_camera_btn);
        if (cameraBtn != null) cameraBtn.setOnClickListener(v -> dispatchTakePictureIntent());

        View galleryBtn = findViewById(R.id.cv_gallery_btn);
        if (galleryBtn != null) {
            galleryBtn.setOnClickListener(v -> pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build()));
        }

        View backBtn = findViewById(R.id.btn_back);
        if (backBtn != null) backBtn.setOnClickListener(v -> finish());

        // '스캔하기' 버튼 클릭 시 Gemini OCR 릴레이 실행
        View scanSubmitBtn = findViewById(R.id.cv_upload_submit);
        if (scanSubmitBtn != null) {
            scanSubmitBtn.setOnClickListener(v -> {
                if (selectedBitmap != null) {
                    performOcr(selectedBitmap);
                } else {
                    Toast.makeText(this, "먼저 사진을 선택해주세요.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * ✨ [1단계] OCR 스캔 시작 (릴레이의 첫 번째 주자 출발)
     */
    private void performOcr(Bitmap bitmap) {
        Bitmap resizedBitmap = getResizedBitmap(bitmap, 1024);

        if (pbScanning != null) pbScanning.setVisibility(View.VISIBLE);
        if (llScanBtnContent != null) llScanBtnContent.setVisibility(View.INVISIBLE);

        // 0번 인덱스(1순위 모델)부터 시도 시작!
        attemptOcrWithModel(resizedBitmap, 0);
    }

    private void attemptOcrWithModel(Bitmap resizedBitmap, int modelIndex) {
        // 더 이상 시도할 AI 모델이 없다면? -> 기기 자체(로컬) OCR로 최종 우회!
        if (modelIndex >= FALLBACK_MODELS.length) {
            Toast.makeText(UploadActivity.this, "모든 AI 모델이 바쁩니다. 기기 내 스캐너로 대체합니다.", Toast.LENGTH_LONG).show();
            performLocalOcr(resizedBitmap);
            return;
        }

        String currentModelName = FALLBACK_MODELS[modelIndex];
        android.util.Log.d("OCR_ATTEMPT", currentModelName + " 모델로 스캔 시도 중... (" + (modelIndex + 1) + "/" + FALLBACK_MODELS.length + ")");

        com.google.ai.client.generativeai.GenerativeModel gm = new com.google.ai.client.generativeai.GenerativeModel(
                currentModelName,
                BuildConfig.GEMINI_API_KEY
        );

        com.google.ai.client.generativeai.type.Content content = new com.google.ai.client.generativeai.type.Content.Builder()
                .addImage(resizedBitmap)
                .addText("이 이미지에 포함된 일기 내용을 텍스트로 추출해줘. 텍스트만 출력해.")
                .build();

        com.google.ai.client.generativeai.java.GenerativeModelFutures modelFutures =
                com.google.ai.client.generativeai.java.GenerativeModelFutures.from(gm);

        com.google.common.util.concurrent.ListenableFuture<com.google.ai.client.generativeai.type.GenerateContentResponse> response =
                modelFutures.generateContent(content);

        com.google.common.util.concurrent.Futures.addCallback(response, new com.google.common.util.concurrent.FutureCallback<com.google.ai.client.generativeai.type.GenerateContentResponse>() {
            @Override
            public void onSuccess(com.google.ai.client.generativeai.type.GenerateContentResponse result) {
                runOnUiThread(() -> {
                    if (pbScanning != null) pbScanning.setVisibility(View.GONE);
                    if (llScanBtnContent != null) llScanBtnContent.setVisibility(View.VISIBLE);

                    String extractedText = result.getText();
                    android.util.Log.d("OCR_SUCCESS", currentModelName + " 로 추출 대성공! 🎉");
                    openAnalysisWithText(extractedText);
                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> {
                    android.util.Log.e("OCR_ERROR", currentModelName + " 모델 실패. 원인: " + t.getMessage());
                    // ✨ 에러가 나면 바로 다음 순위(+1)의 모델로 바통을 넘깁니다!
                    attemptOcrWithModel(resizedBitmap, modelIndex + 1);
                });
            }
        }, androidx.core.content.ContextCompat.getMainExecutor(UploadActivity.this));
    }

    /**
     * ✨ [최종 백업] 기기 내장 스캐너(MLKit)
     */
    private void performLocalOcr(Bitmap bitmap) {
        TextRecognizer recognizer = TextRecognition.getClient(
                new KoreanTextRecognizerOptions.Builder().build()
        );

        InputImage image = InputImage.fromBitmap(bitmap, 0);
        recognizer.process(image)
                .addOnSuccessListener(result -> {
                    String extractedText = extractTextFromBlocks(result);
                    android.util.Log.d("OCR_FALLBACK_SUCCESS", "기기 내장 스캐너로 추출 성공!");
                    openAnalysisWithText(extractedText);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("OCR_FALLBACK_ERROR", "기기 스캐너도 실패", e);
                    Toast.makeText(UploadActivity.this, "OCR을 완전히 실패했습니다. 이미지가 너무 흐릿한지 확인해주세요.", Toast.LENGTH_LONG).show();
                })
                .addOnCompleteListener(task -> {
                    recognizer.close();
                    if (pbScanning != null) pbScanning.setVisibility(View.GONE);
                    if (llScanBtnContent != null) llScanBtnContent.setVisibility(View.VISIBLE);
                });
    }

    private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    // --- [사진 불러오기 및 OOM(메모리 초과 방지) 로직] ---

    private void dispatchTakePictureIntent() {
        File photoFile = null;
        try { photoFile = createImageFile(); }
        catch (IOException ex) { Toast.makeText(this, "파일 생성 실패", Toast.LENGTH_SHORT).show(); }

        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            takePictureLauncher.launch(photoURI);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile("JPEG_" + timeStamp + "_", ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // 파일에서 이미지를 메모리 안전하게 불러오기
    private void setPicFromFile() {
        if (currentPhotoPath != null) {
            selectedBitmap = decodeSampledBitmapFromFile(currentPhotoPath, 1024, 1024);
            updatePreview(selectedBitmap);
        }
    }

    // 갤러리에서 이미지를 메모리 안전하게 불러오기
    private void setPicFromUri(Uri uri) {
        try {
            selectedBitmap = decodeSampledBitmapFromUri(uri, 1024, 1024);
            updatePreview(selectedBitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 이미지 사이즈를 계산하여 압축
    private Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    // 이미지 사이즈를 계산하여 압축 (Uri 버전)
    private Bitmap decodeSampledBitmapFromUri(Uri uri, int reqWidth, int reqHeight) throws Exception {
        InputStream is = getContentResolver().openInputStream(uri);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, options);
        is.close();

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        is = getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
        is.close();
        return bitmap;
    }

    // 최적의 압축 비율(inSampleSize) 계산
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    // 공통 미리보기 업데이트 메서드
    private void updatePreview(Bitmap bitmap) {
        if (ivPreview != null && bitmap != null) {
            ivPreview.setImageBitmap(bitmap);
            ivPreview.setVisibility(View.VISIBLE);
            if (tvPreviewText != null) tvPreviewText.setVisibility(View.GONE);
        }
    }

    private String extractTextFromBlocks(Text text) {
        StringBuilder builder = new StringBuilder();
        for (Text.TextBlock block : text.getTextBlocks()) {
            String blockText = block.getText();
            if (blockText != null && !blockText.trim().isEmpty()) {
                if (builder.length() > 0) builder.append('\n');
                builder.append(blockText.trim());
            }
        }
        return builder.toString().trim();
    }

    // 다음 화면으로 이동할 때 텍스트와 이미지 Uri를 함께 전달합니다.
    private void openAnalysisWithText(String extractedText) {
        Intent intent = new Intent(UploadActivity.this, AnalysisActivity.class);
        intent.putExtra("extracted_text", extractedText);

        if (selectedImageUri != null) {
            intent.putExtra("diary_image_uri", selectedImageUri.toString());
        }

        startActivity(intent);
    }
}