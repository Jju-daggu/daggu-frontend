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
        // KOMORAN 초기화 (Light 모델 사용 권장)
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
            for (int i = 0; i < posArray.length(); i++) {
                positiveWords.add(posArray.getString(i));
            }

            JSONArray negArray = obj.getJSONArray("negative");
            for (int i = 0; i < negArray.length(); i++) {
                negativeWords.add(negArray.getString(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int analyzeSentiment(String text) {
        if (text == null || text.isEmpty()) return 0;

        KomoranResult result = komoran.analyze(text);
        List<Token> tokens = result.getTokenList();

        int score = 0;
        for (Token token : tokens) {
            String word = token.getMorph();
            String pos = token.getPos();

            // 명사, 동사, 형용사 위주로 분석
            if (pos.startsWith("NN") || pos.startsWith("VV") || pos.startsWith("VA")) {
                for (String p : positiveWords) {
                    if (word.contains(p)) {
                        score++;
                        break;
                    }
                }
                for (String n : negativeWords) {
                    if (word.contains(n)) {
                        score--;
                        break;
                    }
                }
            }
        }
        return score;
    }

    public List<String> extractKeywords(String text) {
        if (text == null || text.isEmpty()) return new ArrayList<>();
        KomoranResult result = komoran.analyze(text);
        return result.getNouns();
    }
}
