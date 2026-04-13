package com.example.daggumaker;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;

public class SentimentAnalyzer {

    private List<String> positiveWords = new ArrayList<>();
    private List<String> negativeWords = new ArrayList<>();
    private Komoran komoran;

    public SentimentAnalyzer(Context context) {
        loadDictionary(context);
        komoran = new Komoran(DEFAULT_MODEL.LIGHT);
    }

    private void loadDictionary(Context context) {
        try {
            InputStream is = context.getAssets().open("emotion_dictionary.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONObject obj = new JSONObject(json);
            JSONArray posArray = obj.getJSONArray("positive");
            for (int i = 0; i < posArray.length(); i++) positiveWords.add(posArray.getString(i));
            JSONArray negArray = obj.getJSONArray("negative");
            for (int i = 0; i < negArray.length(); i++) negativeWords.add(negArray.getString(i));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 🌟 시나리오 기반 감정 분석 (과거형 감가상각 포함)
    public int analyzeSentimentWithWeight(String text) {
        if (text == null || text.isEmpty()) return 0;

        // 시나리오 A: 성장/극복
        if ((text.contains("힘들었") || text.contains("어려웠") || text.contains("처음엔")) &&
                (text.contains("익숙해") || text.contains("좋았") || text.contains("뿌듯") || text.contains("보람"))) {
            return 10;
        }

        // 시나리오 B: 상실/그리움
        if ((text.contains("키웠었") || text.contains("옛날") || text.contains("예전")) &&
                (text.contains("갔다") || text.contains("보고 싶") || text.contains("그립"))) {
            return -10;
        }

        String[] sentences = text.split("(?<=[.!?])\\s*");
        double finalScore = 0;
        int n = sentences.length;

        for (int i = 0; i < n; i++) {
            String sentence = sentences[i];
            int sScore = getBaseScore(sentence);

            // 과거형 어미 포함 시 긍정 점수 반토막
            if (sentence.contains("었") || sentence.contains("았") || sentence.contains("더랬")) {
                if (sScore > 0) sScore = (int)(sScore * 0.5);
            }

            double weight = 1.0 + ((double) i / n) * 1.5;
            finalScore += (sScore * weight);
        }
        return (int) Math.round(finalScore);
    }

    private int getBaseScore(String text) {
        KomoranResult result = komoran.analyze(text);
        List<Token> tokens = result.getTokenList();
        int score = 0;
        for (Token token : tokens) {
            String word = token.getMorph();
            String pos = token.getPos();
            if (pos.startsWith("NN") || pos.startsWith("VV") || pos.startsWith("VA")) {
                for (String p : positiveWords) if (word.contains(p)) { score += 2; break; }
                for (String n : negativeWords) if (word.contains(n)) { score -= 1; break; }
            }
        }
        return score;
    }

    // 🌟 동적 학습을 위한 '순수 명사' 추출기
    public List<String> extractRawKeywords(String text) {
        if (text == null || text.isEmpty()) return new ArrayList<>();
        KomoranResult result = komoran.analyze(text);
        List<Token> tokenList = result.getTokenList();
        List<String> filtered = new ArrayList<>();

        for (Token token : tokenList) {
            String word = token.getMorph();
            String pos = token.getPos();

            // 일반명사, 고유명사, 외국어 중 2글자 이상, 숫자 제외
            if ((pos.equals("NNG") || pos.equals("SL") || pos.equals("NNP"))
                    && word.length() >= 2 && !word.matches(".*\\d.*")) {

                // 절대 들어가면 안 되는 불용어 하드코딩
                if(!word.matches(".*요일") && !word.equals("학원") && !word.equals("처음") && !word.equals("생각") && !word.equals("오늘") && !word.equals("제목")) {
                    if (!filtered.contains(word)) filtered.add(word);
                }
            }
        }
        return filtered;
    }
}