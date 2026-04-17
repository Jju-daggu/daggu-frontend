package com.example.daggumaker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
            "gemini-3.1-flash-lite-preview",
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

    // --- OCR 로직 ---

    private void performOcr(Bitmap bitmap) {
        Bitmap resizedBitmap = getResizedBitmap(bitmap, 1024);
        if (pbScanning != null) pbScanning.setVisibility(View.VISIBLE);
        if (llScanBtnContent != null) llScanBtnContent.setVisibility(View.INVISIBLE);
        attemptOcrWithModel(resizedBitmap, 0);
    }

    private void attemptOcrWithModel(Bitmap resizedBitmap, int modelIndex) {
        if (modelIndex >= FALLBACK_MODELS.length) {
            performLocalOcr(resizedBitmap);
            return;
        }

        String currentModelName = FALLBACK_MODELS[modelIndex];
        com.google.ai.client.generativeai.GenerativeModel gm = new com.google.ai.client.generativeai.GenerativeModel(
                currentModelName, BuildConfig.GEMINI_API_KEY);

        com.google.ai.client.generativeai.type.Content content = new com.google.ai.client.generativeai.type.Content.Builder()
                .addImage(resizedBitmap)
                .addText("이 이미지에 포함된 일기 내용을 텍스트로 추출해줘. 텍스트만 출력해.")
                .build();

        com.google.ai.client.generativeai.java.GenerativeModelFutures modelFutures =
                com.google.ai.client.generativeai.java.GenerativeModelFutures.from(gm);

        com.google.common.util.concurrent.Futures.addCallback(modelFutures.generateContent(content),
                new com.google.common.util.concurrent.FutureCallback<com.google.ai.client.generativeai.type.GenerateContentResponse>() {
                    @Override
                    public void onSuccess(com.google.ai.client.generativeai.type.GenerateContentResponse result) {
                        runOnUiThread(() -> openAnalysisWithText(result.getText()));
                    }
                    @Override
                    public void onFailure(Throwable t) {
                        runOnUiThread(() -> attemptOcrWithModel(resizedBitmap, modelIndex + 1));
                    }
                }, androidx.core.content.ContextCompat.getMainExecutor(this));
    }

    private void performLocalOcr(Bitmap bitmap) {
        TextRecognizer recognizer = TextRecognition.getClient(new KoreanTextRecognizerOptions.Builder().build());
        recognizer.process(InputImage.fromBitmap(bitmap, 0))
                .addOnSuccessListener(result -> openAnalysisWithText(extractTextFromBlocks(result)))
                .addOnFailureListener(e -> openAnalysisWithText(""));
    }

    // 🌟 AI 호출 없이 AnalysisActivity로 데이터만 넘김
    private void openAnalysisWithText(String extractedText) {
        if (pbScanning != null) pbScanning.setVisibility(View.GONE);
        if (llScanBtnContent != null) llScanBtnContent.setVisibility(View.VISIBLE);

        Intent intent = new Intent(UploadActivity.this, AnalysisActivity.class);
        intent.putExtra("extracted_text", extractedText);
        if (selectedImageUri != null) intent.putExtra("diary_image_uri", selectedImageUri.toString());
        startActivity(intent);
    }

    // --- 헬퍼 함수들 (이미지 처리 등) ---

    private String extractTextFromBlocks(Text text) {
        StringBuilder builder = new StringBuilder();
        for (Text.TextBlock block : text.getTextBlocks()) {
            builder.append(block.getText()).append("\n");
        }
        return builder.toString().trim();
    }

    private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) { width = maxSize; height = (int) (width / bitmapRatio); }
        else { height = maxSize; width = (int) (height * bitmapRatio); }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private void dispatchTakePictureIntent() {
        File photoFile = null;
        try { photoFile = createImageFile(); } catch (IOException ex) { }
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
        selectedBitmap = BitmapFactory.decodeFile(currentPhotoPath);
        updatePreview(selectedBitmap);
    }

    private void setPicFromUri(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            selectedBitmap = BitmapFactory.decodeStream(is);
            updatePreview(selectedBitmap);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updatePreview(Bitmap bitmap) {
        if (ivPreview != null) {
            ivPreview.setImageBitmap(bitmap);
            ivPreview.setVisibility(View.VISIBLE);
            if (tvPreviewText != null) tvPreviewText.setVisibility(View.GONE);
        }
    }
}