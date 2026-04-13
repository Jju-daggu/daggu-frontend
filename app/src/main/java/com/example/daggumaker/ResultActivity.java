package com.example.daggumaker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        TextView tvExtractedRes = findViewById(R.id.tv_extracted_text_result);
        TextView tvSentimentRes = findViewById(R.id.tv_sentiment_result);
        TextView tvKeywordsRes = findViewById(R.id.tv_keywords_result);
        View btnMain = findViewById(R.id.btn_main);

        String finalText = getIntent().getStringExtra("final_text");
        if (finalText != null) {
            tvExtractedRes.setText(finalText);

            SentimentAnalyzer analyzer = new SentimentAnalyzer(this);
            int score = analyzer.analyzeSentimentWithWeight(finalText);

            List<String> rawKeywords = analyzer.extractKeywords(finalText);
            List<String> selected = new ArrayList<>();

            // 🌟 다꾸 핵심 키워드 리스트 (우선순위)
            String[] coreRef = {"기자", "인터뷰", "동아리", "강아지", "똘이", "할머니", "추억", "기사"};
            for (String core : coreRef) {
                for (String kw : rawKeywords) {
                    if (kw.equals(core) && !selected.contains(kw)) selected.add(kw);
                }
            }
            // 남은 자리에 일반 키워드 채우기
            for (String kw : rawKeywords) {
                if (selected.size() < 3 && !selected.contains(kw)) selected.add(kw);
            }

            updateUI(score, selected, tvSentimentRes, tvKeywordsRes);
        }

        if (btnMain != null) btnMain.setOnClickListener(v -> finish());
    }

    private void updateUI(int score, List<String> keywords, TextView tvSentiment, TextView tvKeywords) {

        // 🌟 시나리오 기반 맞춤형 출력
        if (score >= 10) {
            // 성장 시나리오 감지 시
            tvSentiment.setText("어려움을 극복하고 훌쩍 성장한 하루네요! 🚀");
            tvSentiment.setTextColor(Color.parseColor("#4CAF50")); // 초록색
        } else if (score <= -10) {
            // 그리움 시나리오 감지 시
            tvSentiment.setText("소중한 과거의 추억이 사무치게 그리운 날 ☁️");
            tvSentiment.setTextColor(Color.parseColor("#F44336")); // 빨간색
        } else if (score >= 1) {
            // 일반 긍정
            tvSentiment.setText("기분 좋은 일이 있었던 하루네요 ✨");
            tvSentiment.setTextColor(Color.parseColor("#8BC34A")); // 연두색
        } else if (score <= -3) {
            // 일반 부정
            tvSentiment.setText("마음이 조금 지치고 힘든 날인가요? 🌧️");
            tvSentiment.setTextColor(Color.parseColor("#E53935")); // 진한 빨강
        } else {
            // 중립/평온
            tvSentiment.setText("잔잔하고 차분한 일상이었어요 🍃");
            tvSentiment.setTextColor(Color.parseColor("#7A5C46")); // 갈색
        }

        StringBuilder sb = new StringBuilder();
        for (String s : keywords) sb.append("#").append(s).append(" ");
        tvKeywords.setText(keywords.isEmpty() ? "#일상 #기록" : sb.toString().trim());
    }
}