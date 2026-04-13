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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

        View btnMain = findViewById(R.id.btn_main);
        View btnBack = findViewById(R.id.btn_back);
        CardView cvGenerateSticker = findViewById(R.id.cv_generate_sticker);

        String finalText = getIntent().getStringExtra("final_text");
        if (finalText != null) {
            tvExtractedRes.setText(finalText);
            SentimentAnalyzer analyzer = new SentimentAnalyzer(this);
            int score = analyzer.analyzeSentimentWithWeight(finalText);
            List<String> currentKeywords = analyzer.extractRawKeywords(finalText);
            learnKeywords(currentKeywords);
            List<String> topKeywords = getTopGlobalKeywords(3);
            updateUI(score, topKeywords, tvSentimentRes, tvKeywordsRes);

            // ✨ [추가] 추출된 현재 키워드를 해당 날짜 기준으로 덮어쓰기/저장
            saveDailyKeywords(currentKeywords);
        }

        if (btnMain != null) btnMain.setOnClickListener(v -> finish());
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // 🌟 [수정] 스티커 생성 버튼: 이미지 경로를 다음으로 전달
        if (cvGenerateSticker != null) {
            cvGenerateSticker.setOnClickListener(v -> {
                Intent intent = new Intent(ResultActivity.this, StickerPreviewActivity.class);

                // ✨ 배달온 이미지 경로를 다음 화면으로 전달
                intent.putExtra("diary_image_uri", getIntent().getStringExtra("diary_image_uri"));

                startActivity(intent);
            });
        }
    }

    // ✨ [추가] 날짜별 키워드를 저장하는 메서드
    private void saveDailyKeywords(List<String> keywords) {
        SharedPreferences dailyPrefs = getSharedPreferences("DailyKeywordMemory", MODE_PRIVATE);
        SharedPreferences.Editor editor = dailyPrefs.edit();

        // 1. 기준이 될 날짜 생성 (예: "2026-04-13")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        String todayDate = sdf.format(new Date());

        // 2. 리스트 형태의 키워드를 하나의 문자열로 변환 (예: "일상, 기록, 공부")
        String keywordString = TextUtils.join(", ", keywords);

        // 3. 날짜를 Key로 하여 저장 (같은 날짜면 자동으로 덮어씌워짐 = 일기 수정 시 키워드도 변경됨)
        editor.putString(todayDate, keywordString);
        editor.apply();
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
        if (score >= 10) { tvSentiment.setText("어려움을 극복하고 훌쩍 성장한 하루네요! 🚀"); tvSentiment.setTextColor(Color.parseColor("#4CAF50")); }
        else if (score <= -10) { tvSentiment.setText("소중한 과거의 추억이 사무치게 그리운 날 ☁️"); tvSentiment.setTextColor(Color.parseColor("#F44336")); }
        else if (score >= 1) { tvSentiment.setText("기분 좋은 일이 있었던 하루네요 ✨"); tvSentiment.setTextColor(Color.parseColor("#8BC34A")); }
        else if (score <= -3) { tvSentiment.setText("마음이 조금 지치고 힘든 날인가요? 🌧️"); tvSentiment.setTextColor(Color.parseColor("#E53935")); }
        else { tvSentiment.setText("잔잔하고 차분한 일상이었어요 🍃"); tvSentiment.setTextColor(Color.parseColor("#7A5C46")); }

        StringBuilder sb = new StringBuilder();
        for (String s : keywords) sb.append("#").append(s).append(" ");
        tvKeywords.setText(keywords.isEmpty() ? "#일상 #기록" : sb.toString().trim());
    }
}