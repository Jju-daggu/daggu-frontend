package com.example.daggumaker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // 1. 뷰 연결
        TextView tvExtractedRes = findViewById(R.id.tv_extracted_text_result);
        TextView tvSentimentRes = findViewById(R.id.tv_sentiment_result);
        TextView tvKeywordsRes = findViewById(R.id.tv_keywords_result);
        View btnBack = findViewById(R.id.btn_back);
        View btnMain = findViewById(R.id.btn_main);
        View cvSticker = findViewById(R.id.cv_generate_sticker);

        // 2. 데이터 수신 및 분석
        String finalText = getIntent().getStringExtra("final_text");
        if (finalText != null) {
            if (tvExtractedRes != null) tvExtractedRes.setText(finalText);

            // 분석기 실행 (백그라운드 권장이나 로직의 단순함을 위해 현재는 메인에서 처리)
            try {
                SentimentAnalyzer analyzer = new SentimentAnalyzer(this);
                
                // 감정 분석
                int score = analyzer.analyzeSentiment(finalText);
                String sentimentStr;
                if (score > 0) {
                    sentimentStr = "오늘 기분은 아주 좋아요! ☀️ (+" + score + ")";
                    tvSentimentRes.setTextColor(Color.parseColor("#4CAF50"));
                } else if (score < 0) {
                    sentimentStr = "조금 우울한 날일까요? ☁️ (" + score + ")";
                    tvSentimentRes.setTextColor(Color.parseColor("#F44336"));
                } else {
                    sentimentStr = "평온한 하루였네요. 🍃";
                    tvSentimentRes.setTextColor(Color.parseColor("#7A5C46"));
                }
                if (tvSentimentRes != null) tvSentimentRes.setText(sentimentStr);

                // 키워드 추출
                List<String> keywords = analyzer.extractKeywords(finalText);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < Math.min(keywords.size(), 3); i++) {
                    sb.append("#").append(keywords.get(i)).append(" ");
                }
                if (tvKeywordsRes != null) {
                    tvKeywordsRes.setText(sb.length() > 0 ? sb.toString() : "#일상 #기록");
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "분석 중 오류 발생", Toast.LENGTH_SHORT).show();
            }
        }

        // 3. 버튼 리스너
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (btnMain != null) {
            btnMain.setOnClickListener(v -> {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }
        if (cvSticker != null) {
            cvSticker.setOnClickListener(v -> {
                Intent intent = new Intent(this, StickerPreviewActivity.class);
                startActivity(intent);
            });
        }
    }
}