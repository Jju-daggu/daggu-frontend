package com.example.daggumaker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class AnalysisActivity extends AppCompatActivity {

    private EditText etExtractedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        View btnBack = findViewById(R.id.btn_back);
        View btnMain = findViewById(R.id.btn_main);
        View btnSubmit = findViewById(R.id.cv_analysis);
        etExtractedText = findViewById(R.id.et_extracted_text);

        String initialText = getIntent().getStringExtra("extracted_text");
        if (initialText != null && etExtractedText != null) {
            etExtractedText.setText(initialText);
        }

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnMain != null) {
            btnMain.setOnClickListener(v -> {
                Intent intent = new Intent(AnalysisActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                String finalText = etExtractedText.getText().toString();

                if (finalText.trim().isEmpty()) {
                    Toast.makeText(this, "일기 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(this, "AI가 일기를 분석하고 있습니다...", Toast.LENGTH_SHORT).show();
                btnSubmit.setEnabled(false);

                startAiAnalysis(finalText, btnSubmit);
            });
        }
    }

    private void startAiAnalysis(String diaryText, View btnSubmit) {
        // 🌟 수정 1: 주소 맨 끝에 슬래시(/) 반드시 추가!
        String serverUrl = "https://cathouse-quadrant-opal.ngrok-free.dev/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        DiaryService service = retrofit.create(DiaryService.class);

        String cleanText = diaryText.replace("\n", " ").replace("\r", " ");
        DiaryRequest request = new DiaryRequest(cleanText);

        service.analyzeDiary(request).enqueue(new Callback<DiaryResponse>() {
            @Override
            public void onResponse(Call<DiaryResponse> call, Response<DiaryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String emotion = response.body().getEmotion();
                    String sticker = response.body().getSticker();
                    String tagsString = response.body().getTagsString();
                    String stickerUrl = response.body().getStickerUrl(); // 🌟 수정 2: AI 스티커 URL 받기

                    // 🌟 수정 3: runOnUiThread 제거 (이미 UI 스레드임)
                    Intent intent = new Intent(AnalysisActivity.this, ResultActivity.class);
                    intent.putExtra("final_text", cleanText);
                    intent.putExtra("ai_emotion", emotion);
                    intent.putExtra("ai_sticker", sticker);
                    intent.putExtra("ai_tags_string", tagsString);
                    intent.putExtra("ai_sticker_url", stickerUrl); // 다음 화면으로 스티커 URL 넘기기
                    intent.putExtra("diary_image_uri", getIntent().getStringExtra("diary_image_uri"));

                    startActivity(intent);
                    btnSubmit.setEnabled(true);
                } else {
                    Log.e("AI_API", "서버 응답 오류: " + response.code());
                    Toast.makeText(AnalysisActivity.this, "분석 실패: 서버 응답 오류", Toast.LENGTH_SHORT).show();
                    btnSubmit.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<DiaryResponse> call, Throwable t) {
                Log.e("AI_API", "연결 실패: " + t.getMessage());
                Toast.makeText(AnalysisActivity.this, "서버 연결에 실패했습니다.", Toast.LENGTH_LONG).show();
                btnSubmit.setEnabled(true);
            }
        });
    }

    // --- Retrofit 데이터 모델 ---
    class DiaryRequest {
        String content;
        DiaryRequest(String content) { this.content = content; }
    }

    class DiaryResponse {
        String emotion;
        String sticker;
        String tags_string;
        String sticker_url; // 🌟 새로 추가된 AI 스티커 이미지 URL

        public String getEmotion() { return emotion; }
        public String getSticker() { return sticker; }
        public String getTagsString() { return tags_string; }
        public String getStickerUrl() { return sticker_url; } // 🌟 게터 추가
    }

    interface DiaryService {
        // 🌟 BaseUrl 끝에 /가 있으므로, 여기서는 앞에 /를 빼는 것이 안전합니다.
        @POST("analyze")
        Call<DiaryResponse> analyzeDiary(@Body DiaryRequest request);
    }
}