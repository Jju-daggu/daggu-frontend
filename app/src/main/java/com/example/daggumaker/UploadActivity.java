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

    private static final String GEMINI_MODEL = "gemini-3.1-flash-lite-preview";

    private String currentPhotoPath;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;

    private ImageView ivPreview;
    private TextView tvPreviewText;
    private Bitmap selectedBitmap; // 선택된 이미지 저장용
    private View pbScanning;
    private View llScanBtnContent;

    // ✨ [추가] 선택된 이미지의 위치(Uri)를 저장할 변수
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
                        // ✨ [추가] 촬영된 파일의 Uri 저장
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
                        // ✨ [추가] 갤러리에서 선택된 Uri 저장
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

        // '스캔하기' 버튼 클릭 시 Gemini OCR 실행
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
     * Gemini API를 사용하여 OCR 수행
     */
    private void performOcr(Bitmap bitmap) {
        // 1. 이미지 리사이징 (용량 및 성능 최적화)
        Bitmap resizedBitmap = getResizedBitmap(bitmap, 1024);

        // UI 상태 변경
        if (pbScanning != null) pbScanning.setVisibility(View.VISIBLE);
        if (llScanBtnContent != null) llScanBtnContent.setVisibility(View.INVISIBLE);

        // Gemini 모델 생성
        com.google.ai.client.generativeai.GenerativeModel gm = new com.google.ai.client.generativeai.GenerativeModel(
                GEMINI_MODEL,
                BuildConfig.GEMINI_API_KEY
        );

        com.google.ai.client.generativeai.type.Content content = new com.google.ai.client.generativeai.type.Content.Builder()
                .addImage(resizedBitmap)
                .addText("이 이미지에 포함된 일기 내용을 텍스트로 추출해줘. 텍스트만 출력해.")
                .build();

        // 비동기 실행 (Java용 GenerativeModelFutures 활용)
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
                    android.util.Log.d("OCR_SUCCESS", "Extracted: " + extractedText);
                    openAnalysisWithText(extractedText);
                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> {
                    android.util.Log.e("OCR_ERROR", "OCR failed with message: " + t.getMessage(), t);

                    if (isQuotaExceededError(t)) {
                        Toast.makeText(UploadActivity.this, "Gemini 사용 한도에 도달했습니다. 기기 내 OCR로 대체합니다.", Toast.LENGTH_LONG).show();
                        performLocalOcr(resizedBitmap);
                        return;
                    }

                    if (pbScanning != null) pbScanning.setVisibility(View.GONE);
                    if (llScanBtnContent != null) llScanBtnContent.setVisibility(View.VISIBLE);

                    String errorMsg = extractFriendlyErrorMessage(t);
                    Toast.makeText(UploadActivity.this, "OCR 실패: " + errorMsg, Toast.LENGTH_LONG).show();
                });
            }
        }, androidx.core.content.ContextCompat.getMainExecutor(this));
    }

    private void performLocalOcr(Bitmap bitmap) {
        TextRecognizer recognizer = TextRecognition.getClient(
                new KoreanTextRecognizerOptions.Builder().build()
        );

        InputImage image = InputImage.fromBitmap(bitmap, 0);
        recognizer.process(image)
                .addOnSuccessListener(result -> {
                    String extractedText = extractTextFromBlocks(result);
                    android.util.Log.d("OCR_FALLBACK_SUCCESS", "Extracted locally: " + extractedText);
                    openAnalysisWithText(extractedText);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("OCR_FALLBACK_ERROR", "Local OCR failed", e);
                    String errorMsg = extractFriendlyErrorMessage(e);
                    Toast.makeText(UploadActivity.this, "대체 OCR도 실패: " + errorMsg, Toast.LENGTH_LONG).show();
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

    // --- [카메라 관련 로직] ---
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

    private void setPicFromFile() {
        if (currentPhotoPath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            selectedBitmap = bitmap; // 저장
            updatePreview(bitmap);
        }
    }

    // --- [갤러리 관련 로직] ---
    private void setPicFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            selectedBitmap = bitmap; // 저장
            updatePreview(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 공통 미리보기 업데이트 메서드
    private void updatePreview(Bitmap bitmap) {
        if (ivPreview != null && bitmap != null) {
            ivPreview.setImageBitmap(bitmap);
            ivPreview.setVisibility(View.VISIBLE);
            if (tvPreviewText != null) tvPreviewText.setVisibility(View.GONE);
        }
    }

    private String extractFriendlyErrorMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && !message.trim().isEmpty()) {
                String lower = message.toLowerCase(Locale.getDefault());
                if (lower.contains("quota") || lower.contains("rate limit") || lower.contains("limit: 0")) {
                    return "Gemini 사용 한도 초과";
                }
                if (message.contains("404") && message.contains("models/")) {
                    return "Gemini 모델을 찾을 수 없습니다.";
                }
                if (message.contains("403")) {
                    return "API 키 문제 또는 권한 없음 (403)";
                }
                if (!message.contains("MissingFieldException")) {
                    return message;
                }
            }
            current = current.getCause();
        }
        return "알 수 없는 오류";
    }

    private boolean isQuotaExceededError(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                String lower = message.toLowerCase(Locale.getDefault());
                if (lower.contains("quota") || lower.contains("rate limit") || lower.contains("limit: 0")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
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

    // ✨ [수정] 다음 화면으로 이동할 때 이미지 Uri를 함께 전달합니다.
    private void openAnalysisWithText(String extractedText) {
        Intent intent = new Intent(UploadActivity.this, AnalysisActivity.class);
        intent.putExtra("extracted_text", extractedText);

        // 이미지 경로가 있다면 Extra에 추가하여 다음 화면으로 배달
        if (selectedImageUri != null) {
            intent.putExtra("diary_image_uri", selectedImageUri.toString());
        }

        startActivity(intent);
    }
}