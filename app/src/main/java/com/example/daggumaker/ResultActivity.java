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

        // 1. Intent에서 데이터 가져오기 (UploadActivity가 보내준 데이터)
        String finalText = getIntent().getStringExtra("final_text");
        String aiEmotion = getIntent().getStringExtra("ai_emotion");
        String aiSticker = getIntent().getStringExtra("ai_sticker");

        if (finalText != null) {
            tvExtractedRes.setText(finalText);

            // 🌟 무거운 형태소 분석기(SentimentAnalyzer) 대신, 가벼운 자체 키워드 추출 사용!
            List<String> currentKeywords = extractSimpleKeywords(finalText);
            learnKeywords(currentKeywords);
            saveDailyKeywords(currentKeywords);

            // 2. AI 결과를 바탕으로 UI 업데이트
            updateUIWithAI(aiEmotion, aiSticker, getTopGlobalKeywords(3), tvSentimentRes, tvKeywordsRes);
        }

        if (cvGenerateSticker != null) {
            cvGenerateSticker.setOnClickListener(v -> {
                Intent intent = new Intent(this, StickerPreviewActivity.class);
                intent.putExtra("diary_image_uri", getIntent().getStringExtra("diary_image_uri"));
                startActivity(intent);
            });
        }
    }

    // --- 🌟 새롭게 추가된 가벼운 해시태그 추출기 ---
    private List<String> extractSimpleKeywords(String text) {
        List<String> keywords = new ArrayList<>();
        String[] words = text.split("\\s+"); // 띄어쓰기 기준으로 나누기
        for(String w : words) {
            // 특수문자 제거하고 순수 글자만 남기기
            String cleanWord = w.replaceAll("[^가-힣a-zA-Z]", "");
            // 2~5글자 단어만 키워드로 추출 (너무 긴 문장 방지)
            if(cleanWord.length() >= 2 && cleanWord.length() <= 5 && !keywords.contains(cleanWord)) {
                keywords.add(cleanWord);
            }
        }
        return keywords;
    }

    // 🌟 AI 감정에 맞춰 문구와 색상을 정해주는 함수
    private void updateUIWithAI(String emotion, String sticker, List<String> keywords, TextView tvSentiment, TextView tvKeywords) {
        String displayMsg;
        int color;

        switch (emotion != null ? emotion : "잔잔한") {
            case "즐거운": case "행복한":
                displayMsg = "정말 행복하고 기분 좋은 하루네요! " + (sticker != null ? sticker : "🥰");
                color = Color.parseColor("#8BC34A"); break;
            case "뿌듯한":
                displayMsg = "나 자신이 대견한, 정말 보람찬 하루예요! " + (sticker != null ? sticker : "💪");
                color = Color.parseColor("#4CAF50"); break;
            case "놀라운":
                displayMsg = "와! 깜짝 놀랄 만한 일이 있었던 날이네요! " + (sticker != null ? sticker : "😲");
                color = Color.parseColor("#FF9800"); break;
            case "불안":
                displayMsg = "마음이 조금 불안한가요? 다 잘 될 거예요. " + (sticker != null ? sticker : "😰");
                color = Color.parseColor("#9E9E9E"); break;
            case "속상한": case "슬픈":
                displayMsg = "마음이 울적할 땐 푹 쉬는 것도 방법이에요. " + (sticker != null ? sticker : "🥺");
                color = Color.parseColor("#3F51B5"); break;
            case "화난/짜증":
                displayMsg = "오늘은 정말 화가 나는 날이었군요. 후~ 숨을 골라봐요. " + (sticker != null ? sticker : "🔥");
                color = Color.parseColor("#FF5722"); break;
            default: // 잔잔한
                displayMsg = "평온하고 잔잔한 하루였네요. " + (sticker != null ? sticker : "🌿");
                color = Color.parseColor("#7A5C46"); break;
        }

        tvSentiment.setText(displayMsg);
        tvSentiment.setTextColor(color);

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