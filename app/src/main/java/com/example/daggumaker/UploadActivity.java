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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UploadActivity extends AppCompatActivity {

    private String currentPhotoPath;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;

    private ImageView ivPreview;
    private TextView tvPreviewText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // 1. 뷰 초기화
        ivPreview = findViewById(R.id.iv_preview);
        tvPreviewText = findViewById(R.id.tv_preview_text);

        // 2. [카메라] 런처 설정
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result) { setPicFromFile(); }
                }
        );

        // 3. [갤러리] 런처 설정
        pickMediaLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) { setPicFromUri(uri); }
                }
        );

        // 4. 클릭 리스너 설정

        // 카메라 버튼
        View cameraBtn = findViewById(R.id.cv_camera_btn);
        if (cameraBtn != null) {
            cameraBtn.setOnClickListener(v -> dispatchTakePictureIntent());
        }

        // 갤러리 버튼
        View galleryBtn = findViewById(R.id.cv_gallery_btn);
        if (galleryBtn != null) {
            galleryBtn.setOnClickListener(v -> {
                pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            });
        }

        // 뒤로가기 버튼
        View backBtn = findViewById(R.id.btn_back);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> finish());
        }

        // ✨ [추가된 부분] 맨 아래 '스캔하기' 버튼 클릭 리스너
        View scanSubmitBtn = findViewById(R.id.cv_upload_submit);
        if (scanSubmitBtn != null) {
            scanSubmitBtn.setOnClickListener(v -> {
                // AnalysisActivity(결과 확인 화면)로 이동
                Intent intent = new Intent(UploadActivity.this, AnalysisActivity.class);
                startActivity(intent);
            });
        }
    }

    // --- [카메라 관련 로직] ---
    private void dispatchTakePictureIntent() {
        File photoFile = null;
        try { photoFile = createImageFile(); }
        catch (IOException ex) { Toast.makeText(this, "파일 생성 실패", Toast.LENGTH_SHORT).show(); }

        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider",
                    photoFile);
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
            updatePreview(bitmap);
        }
    }

    // --- [갤러리 관련 로직] ---
    private void setPicFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
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
}