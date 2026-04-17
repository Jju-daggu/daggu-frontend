package com.example.daggumaker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

// Retrofit 관련 임포트 (추가됨)
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class AnalysisActivity extends AppCompatActivity {

    // OCR 추출 텍스트 수정용 EditText
    private EditText etExtractedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        // 1. 뷰 연결
        View btnBack = findViewById(R.id.btn_back);
        View btnMain = findViewById(R.id.btn_main);
        View btnSubmit = findViewById(R.id.cv_analysis);
        etExtractedText = findViewById(R.id.et_extracted_text);

        // [추가] UploadActivity로부터 전달받은 OCR 텍스트 설정
        String initialText = getIntent().getStringExtra("extracted_text");
        if (initialText != null && etExtractedText != null) {
            etExtractedText.setText(initialText);
        }

        // 2. 뒤로가기 버튼
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // 3. 메인 버튼
        if (btnMain != null) {
            btnMain.setOnClickListener(v -> {
                Intent intent = new Intent(AnalysisActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        // 4. 분석 버튼 클릭 → 🌟 바로 넘어가지 않고 AI 분석 시작!
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                String finalText = etExtractedText.getText().toString();

                if (finalText.trim().isEmpty()) {
                    Toast.makeText(this, "일기 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 분석 중임을 알리는 토스트 메시지
                Toast.makeText(this, "AI가 일기를 분석하고 있습니다...", Toast.LENGTH_SHORT).show();

                // 버튼을 여러 번 누르는 것 방지
                btnSubmit.setEnabled(false);

                // AI 서버로 전송
                startAiAnalysis(finalText, btnSubmit);
            });
        }
    }

    // --- 🌟 AI 감정 분석 통신 로직 🌟 ---
    private void startAiAnalysis(String diaryText, View btnSubmit) {
        // 방금 뚫어둔 세민 님의 Ngrok 터널 주소입니다!
        String serverUrl = "https://cathouse-quadrant-opal.ngrok-free.dev";


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        DiaryService service = retrofit.create(DiaryService.class);

        // 줄바꿈 제거 (서버 JSON 에러 방지)
        String cleanText = diaryText.replace("\n", " ").replace("\r", " ");
        DiaryRequest request = new DiaryRequest(cleanText);

        service.analyzeDiary(request).enqueue(new Callback<DiaryResponse>() {
            @Override
            public void onResponse(Call<DiaryResponse> call, Response<DiaryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String emotion = response.body().getEmotion();
                    String sticker = response.body().getSticker();

                    runOnUiThread(() -> {
                        // 🌟 분석 성공! 결과를 들고 ResultActivity로 이동
                        Intent intent = new Intent(AnalysisActivity.this, ResultActivity.class);
                        intent.putExtra("final_text", cleanText);
                        intent.putExtra("ai_emotion", emotion);
                        intent.putExtra("ai_sticker", sticker);
                        intent.putExtra("diary_image_uri", getIntent().getStringExtra("diary_image_uri"));

                        startActivity(intent);

                        // 이동 후 버튼 다시 활성화
                        btnSubmit.setEnabled(true);
                    });
                } else {
                    Log.e("AI_API", "서버 응답 오류: " + response.code());
                    runOnUiThread(() -> {
                        Toast.makeText(AnalysisActivity.this, "분석 실패: 서버 응답 오류", Toast.LENGTH_SHORT).show();
                        btnSubmit.setEnabled(true);
                    });
                }
            }

            @Override
            public void onFailure(Call<DiaryResponse> call, Throwable t) {
                Log.e("AI_API", "분석 서버 연결 실패: " + t.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(AnalysisActivity.this, "서버 연결에 실패했습니다. (터미널이 켜져 있는지 확인하세요)", Toast.LENGTH_LONG).show();
                    btnSubmit.setEnabled(true);
                });
            }
        });
    }

    // --- 🌟 Retrofit용 내부 클래스/인터페이스 ---
    class DiaryRequest {
        String content;
        DiaryRequest(String content) { this.content = content; }
    }

    class DiaryResponse {
        String emotion;
        String sticker;
        public String getEmotion() { return emotion; }
        public String getSticker() { return sticker; }
    }

    interface DiaryService {
        @POST("/analyze")
        Call<DiaryResponse> analyzeDiary(@Body DiaryRequest request);
    }
}