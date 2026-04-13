package com.example.daggumaker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResultActivity extends AppCompatActivity {

    private SharedPreferences keywordPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        keywordPrefs = getSharedPreferences("KeywordMemory", MODE_PRIVATE);

        TextView tvExtractedRes = findViewById(R.id.tv_extracted_text_result);
        TextView tvSentimentRes = findViewById(R.id.tv_sentiment_result);
        TextView tvKeywordsRes = findViewById(R.id.tv_keywords_result);

        // 버튼 뷰 연결
        View btnMain = findViewById(R.id.btn_main);
        View btnBack = findViewById(R.id.btn_back); // 🌟 XML의 뒤로가기 버튼 ID와 일치시켜주세요! (예: btn_back, iv_back 등)

        String finalText = getIntent().getStringExtra("final_text");
        if (finalText != null) {
            tvExtractedRes.setText(finalText);

            SentimentAnalyzer analyzer = new SentimentAnalyzer(this);
            int score = analyzer.analyzeSentimentWithWeight(finalText);
            List<String> currentKeywords = analyzer.extractRawKeywords(finalText);
            learnKeywords(currentKeywords);
            List<String> topKeywords = getTopGlobalKeywords(3);

            updateUI(score, topKeywords, tvSentimentRes, tvKeywordsRes);
        }

        // 🌟 클릭 이벤트 처리
        if (btnMain != null) {
            btnMain.setOnClickListener(v -> {
                // 메인으로 가는 로직이 필요하다면 수정, 일단은 화면 종료로 둠
                finish();
            });
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                finish(); // 🌟 현재 화면(ResultActivity)을 종료하고 이전 화면으로 돌아감
            });
        }
    }

    private void learnKeywords(List<String> keywords) {
        SharedPreferences.Editor editor = keywordPrefs.edit();
        for (String kw : keywords) {
            int count = keywordPrefs.getInt(kw, 0);
            editor.putInt(kw, count + 1);
        }
        editor.apply();
    }

    private List<String> getTopGlobalKeywords(int limit) {
        Map<String, ?> allEntries = keywordPrefs.getAll();
        List<Map.Entry<String, Integer>> list = new ArrayList<>();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getValue() instanceof Integer) {
                list.add(new AbstractMap.SimpleEntry<>(entry.getKey(), (Integer) entry.getValue()));
            }
        }

        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        List<String> result = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, list.size()); i++) {
            result.add(list.get(i).getKey());
        }
        return result;
    }

    private void updateUI(int score, List<String> keywords, TextView tvSentiment, TextView tvKeywords) {
        if (score >= 10) {
            tvSentiment.setText("어려움을 극복하고 훌쩍 성장한 하루네요! 🚀");
            tvSentiment.setTextColor(Color.parseColor("#4CAF50"));
        } else if (score <= -10) {
            tvSentiment.setText("소중한 과거의 추억이 사무치게 그리운 날 ☁️");
            tvSentiment.setTextColor(Color.parseColor("#F44336"));
        } else if (score >= 1) {
            tvSentiment.setText("기분 좋은 일이 있었던 하루네요 ✨");
            tvSentiment.setTextColor(Color.parseColor("#8BC34A"));
        } else if (score <= -3) {
            tvSentiment.setText("마음이 조금 지치고 힘든 날인가요? 🌧️");
            tvSentiment.setTextColor(Color.parseColor("#E53935"));
        } else {
            tvSentiment.setText("잔잔하고 차분한 일상이었어요 🍃");
            tvSentiment.setTextColor(Color.parseColor("#7A5C46"));
        }

        StringBuilder sb = new StringBuilder();
        for (String s : keywords) sb.append("#").append(s).append(" ");
        tvKeywords.setText(keywords.isEmpty() ? "#일상 #기록" : sb.toString().trim());
    }
}