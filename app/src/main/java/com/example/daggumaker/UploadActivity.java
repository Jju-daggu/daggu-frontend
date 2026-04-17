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

    private static final String[] FALLBACK_MODELS = {
            "gemini-3.1-pro-preview",
            "gemini-3.0-pro",
            "gemini-2.5-flash-image-preview",
            "gemini-3.1-flash-lite-preview", // 복사본에 있던 모델 추가
            "gemini-2.5-flash"
    };

    private String currentPhotoPath;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;

    private ImageView ivPreview;
    private TextView tvPreviewText;
    private Bitmap selectedBitmap;
    private View pbScanning;
    private View llScanBtnContent;

    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        ivPreview = findViewById(R.id.iv_preview);
        tvPreviewText = findViewById(R.id.tv_preview_text);
        pbScanning = findViewById(R.id.pb_scanning);
        llScanBtnContent = findViewById(R.id.ll_scan_btn_content);

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result) {
                        selectedImageUri = Uri.fromFile(new File(currentPhotoPath));
                        setPicFromFile();
                    }
                }
        );

        pickMediaLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        setPicFromUri(uri);
                    }
                }
        );

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

    private void performOcr(Bitmap bitmap) {
        Bitmap resizedBitmap = getResizedBitmap(bitmap, 1024);

        if (pbScanning != null) pbScanning.setVisibility(View.VISIBLE);
        if (llScanBtnContent != null) llScanBtnContent.setVisibility(View.INVISIBLE);

        attemptOcrWithModel(resizedBitmap, 0);
    }

    private void attemptOcrWithModel(Bitmap resizedBitmap, int modelIndex) {
        if (modelIndex >= FALLBACK_MODELS.length) {
            Toast.makeText(UploadActivity.this, "AI 모델 한도 초과/응답 없음. 기기 내 스캐너로 대체합니다.", Toast.LENGTH_LONG).show();
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
                    // 복사본 로직 반영: 에러 메시지 추출 및 로깅
                    String errorMsg = extractFriendlyErrorMessage(t);
                    android.util.Log.e("OCR_ERROR", currentModelName + " 모델 실패. 원인: " + errorMsg);

                    attemptOcrWithModel(resizedBitmap, modelIndex + 1);
                });
            }
        }, androidx.core.content.ContextCompat.getMainExecutor(UploadActivity.this));
    }

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
                    // 복사본 에러 처리 방식 반영
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
            selectedBitmap = decodeSampledBitmapFromFile(currentPhotoPath, 1024, 1024);
            updatePreview(selectedBitmap);
        }
    }

    private void setPicFromUri(Uri uri) {
        try {
            selectedBitmap = decodeSampledBitmapFromUri(uri, 1024, 1024);
            updatePreview(selectedBitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

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

    private void updatePreview(Bitmap bitmap) {
        if (ivPreview != null && bitmap != null) {
            ivPreview.setImageBitmap(bitmap);
            ivPreview.setVisibility(View.VISIBLE);
            if (tvPreviewText != null) tvPreviewText.setVisibility(View.GONE);
        }
    }

    // 복사본 반영: 에러 친화적 변환 메서드
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

    // 복사본 반영: 할당량 초과 확인 메서드
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

    private void openAnalysisWithText(String extractedText) {
        Intent intent = new Intent(UploadActivity.this, AnalysisActivity.class);
        intent.putExtra("extracted_text", extractedText);

        if (selectedImageUri != null) {
            intent.putExtra("diary_image_uri", selectedImageUri.toString());
        }

        startActivity(intent);
    }
}