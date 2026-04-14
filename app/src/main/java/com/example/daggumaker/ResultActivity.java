package com.example.daggumaker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.text.SimpleDateFormat;
import java.util.*;

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
        View btnBack = findViewById(R.id.btn_back);
        CardView cvGenerateSticker = findViewById(R.id.cv_generate_sticker);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        String finalText = getIntent().getStringExtra("final_text");
        if (finalText != null) {
            tvExtractedRes.setText(finalText);
            SentimentAnalyzer analyzer = new SentimentAnalyzer(this);

            // 🌟 반전 가중치가 적용된 점수 계산
            int score = analyzer.analyzeSentimentWithWeight(finalText);

            List<String> currentKeywords = analyzer.extractRawKeywords(finalText);
            learnKeywords(currentKeywords);
            saveDailyKeywords(currentKeywords);

            // 🌟 9가지 감정 UI 업데이트
            updateUI(score, getTopGlobalKeywords(3), tvSentimentRes, tvKeywordsRes);
        }

        if (cvGenerateSticker != null) {
            cvGenerateSticker.setOnClickListener(v -> {
                Intent intent = new Intent(this, StickerPreviewActivity.class);
                intent.putExtra("diary_image_uri", getIntent().getStringExtra("diary_image_uri"));
                startActivity(intent);
            });
        }
    }

    private void updateUI(int score, List<String> keywords, TextView tvSentiment, TextView tvKeywords) {
        // 9가지 감정 분류 규칙
        if (score >= 10) {
            tvSentiment.setText("어려움을 극복하고 훌쩍 성장한 하루네요! 🚀");
            tvSentiment.setTextColor(Color.parseColor("#4CAF50")); // 초록 (성장)
        } else if (score >= 6) {
            tvSentiment.setText("완전 신나고 에너지가 뿜뿜 넘치는 하루! 😆");
            tvSentiment.setTextColor(Color.parseColor("#FF9800")); // 주황 (신남)
        } else if (score >= 3) {
            tvSentiment.setText("가슴이 두근두근, 기분 좋은 설렘이 가득해요! 💓");
            tvSentiment.setTextColor(Color.parseColor("#E91E63")); // 핑크 (설렘)
        } else if (score >= 1) {
            tvSentiment.setText("기분 좋은 일이 있었던 행복한 하루네요 ✨");
            tvSentiment.setTextColor(Color.parseColor("#8BC34A")); // 연두 (행복)
        } else if (score == 0) {
            tvSentiment.setText("잔잔하고 차분한 평범한 일상이었어요 🍃");
            tvSentiment.setTextColor(Color.parseColor("#7A5C46")); // 갈색 (평범)
        } else if (score >= -3) {
            tvSentiment.setText("마음이 조금 지치고 피곤한 날인가요? ☕");
            tvSentiment.setTextColor(Color.parseColor("#9E9E9E")); // 회색 (피곤)
        } else if (score >= -6) {
            tvSentiment.setText("뜻대로 되지 않아 조금 답답하고 짜증이 났군요 😤");
            tvSentiment.setTextColor(Color.parseColor("#FF5722")); // 짙은 주황 (짜증)
        } else if (score >= -15) {
            tvSentiment.setText("속상하고 우울한 마음에 위로가 필요한 날이에요 💧");
            tvSentiment.setTextColor(Color.parseColor("#3F51B5")); // 남색 (우울)
        } else {
            tvSentiment.setText("소중한 과거의 추억이 사무치게 그리운 날 ☁️");
            tvSentiment.setTextColor(Color.parseColor("#F44336")); // 빨강 (그리움/이별)
        }

        StringBuilder sb = new StringBuilder();
        for (String s : keywords) sb.append("#").append(s).append(" ");
        tvKeywords.setText(keywords.isEmpty() ? "#일상 #기록" : sb.toString().trim());
    }

    private void saveDailyKeywords(List<String> keywords) {
        SharedPreferences dailyPrefs = getSharedPreferences("DailyKeywordMemory", MODE_PRIVATE);
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(new Date());
        dailyPrefs.edit().putString(todayDate, TextUtils.join(", ", keywords)).apply();
    }

    private void learnKeywords(List<String> keywords) {
        SharedPreferences.Editor editor = keywordPrefs.edit();
        for (String kw : keywords) editor.putInt(kw, keywordPrefs.getInt(kw, 0) + 1);
        editor.apply();
    }

    private List<String> getTopGlobalKeywords(int limit) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>();
        for (Map.Entry<String, ?> entry : keywordPrefs.getAll().entrySet()) {
            if (entry.getValue() instanceof Integer) list.add(new AbstractMap.SimpleEntry<>(entry.getKey(), (Integer) entry.getValue()));
        }
        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        List<String> res = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, list.size()); i++) res.add(list.get(i).getKey());
        return res;
    }
}