package com.example.daggumaker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    private TextView tvExtractedText, tvSentiment, tvKeywords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // 1. 뷰 연결
        tvExtractedText = findViewById(R.id.tv_extracted_text_result);
        tvSentiment = findViewById(R.id.tv_sentiment_result);
        tvKeywords = findViewById(R.id.tv_keywords_result);

        View btnBack = findViewById(R.id.btn_back);
        View btnMain = findViewById(R.id.btn_main);

        // 🌟 스티커 생성 버튼 연결 (XML의 ID와 맞는지 확인해 주세요!)
        View btnCreateSticker = findViewById(R.id.cv_generate_sticker);

        // 2. AnalysisActivity에서 보낸 데이터 받기
        Intent intent = getIntent();
        String finalText = intent.getStringExtra("final_text");
        String aiEmotion = intent.getStringExtra("ai_emotion");
        String tagsFromServer = intent.getStringExtra("ai_tags_string");

        // 3. 화면에 데이터 뿌리기
        if (finalText != null) {
            tvExtractedText.setText(finalText);
        }

        if (aiEmotion != null) {
            tvSentiment.setText(aiEmotion);
        }

        if (tagsFromServer != null && !tagsFromServer.isEmpty()) {
            tvKeywords.setText(tagsFromServer);
        } else if (aiEmotion != null) {
            tvKeywords.setText("#" + aiEmotion + " #다꾸메이커 #오늘의기록");
        }

        // 4. 버튼 리스너
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnMain != null) {
            btnMain.setOnClickListener(v -> {
                Intent goToMain = new Intent(ResultActivity.this, MainActivity.class);
                goToMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(goToMain);
                finish();
            });
        }

        // 🌟 스티커 생성 버튼 리스너 (StickerPreviewActivity로 이동)
        if (btnCreateSticker != null) {
            btnCreateSticker.setOnClickListener(v -> {
                Intent previewIntent = new Intent(ResultActivity.this, StickerPreviewActivity.class);

                // 다음 화면에서도 감정이나 텍스트 데이터가 필요할 수 있으니 챙겨서 보냅니다.
                previewIntent.putExtra("ai_emotion", aiEmotion);
                previewIntent.putExtra("final_text", finalText);

                startActivity(previewIntent);
            });
        }
    }
}