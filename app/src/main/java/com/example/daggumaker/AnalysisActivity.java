package com.example.daggumaker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class AnalysisActivity extends AppCompatActivity {

    private EditText etDiaryContent, etImageHint;
    // 🌟 [중요] 서버 주소 확인!
    private String serverUrl = "https://cathouse-quadrant-opal.ngrok-free.dev/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        etDiaryContent = findViewById(R.id.et_extracted_text);
        etImageHint = findViewById(R.id.et_image_hint);
        View btnAnalyze = findViewById(R.id.cv_analysis);

        // 🌟 OCR 스캔 글자 받아서 채우기
        String scannedText = getIntent().getStringExtra("extracted_text");
        if (scannedText != null) etDiaryContent.setText(scannedText);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_main).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnAnalyze.setOnClickListener(v -> {
            String content = etDiaryContent.getText().toString();
            String hint = etImageHint.getText().toString();
            if (!content.isEmpty()) {
                Toast.makeText(this, "AI가 다꾸 스티커를 고민 중입니다...", Toast.LENGTH_SHORT).show();
                sendDiaryToServer(content, hint);
            }
        });
    }

    private void sendDiaryToServer(String content, String hint) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        apiService.analyzeDiary(new DiaryRequest(content, hint)).enqueue(new Callback<DiaryResponse>() {
            @Override
            public void onResponse(Call<DiaryResponse> call, Response<DiaryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Intent intent = new Intent(AnalysisActivity.this, ResultActivity.class);

                    // 기존 텍스트 및 스티커 데이터 넘기기
                    intent.putExtra("final_text", content);
                    intent.putExtra("ai_emotion", response.body().getEmotion());
                    intent.putExtra("ai_tags_string", response.body().getTagsString());
                    intent.putStringArrayListExtra("ai_sticker_urls", response.body().getStickerUrls());

                    // 🌟 [핵심 추가] UploadActivity에서 받은 '원본 사진 주소'를 꺼내서 다음 창으로 토스!
                    String diaryImageUri = getIntent().getStringExtra("diary_image_uri");
                    if (diaryImageUri != null) {
                        intent.putExtra("diary_image_uri", diaryImageUri);
                    }

                    startActivity(intent);
                }
            }
            @Override public void onFailure(Call<DiaryResponse> call, Throwable t) {
                Toast.makeText(AnalysisActivity.this, "서버 연결에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    interface ApiService { @POST("analyze") Call<DiaryResponse> analyzeDiary(@Body DiaryRequest request); }
    class DiaryRequest {
        String content, visual_hint;
        DiaryRequest(String c, String v) { this.content = c; this.visual_hint = v; }
    }
    class DiaryResponse {
        String emotion, tags_string;
        ArrayList<String> sticker_urls;
        public String getEmotion() { return emotion; }
        public String getTagsString() { return tags_string; }
        public ArrayList<String> getStickerUrls() { return sticker_urls; }
    }
}