package com.example.daggumaker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // 1. 인텐트 데이터 모두 받아오기 (내 코드 + 팀원 코드 합침)
        Intent intent = getIntent();
        ArrayList<String> stickerUrls = intent.getStringArrayListExtra("ai_sticker_urls");
        String finalText = intent.getStringExtra("final_text");
        String aiEmotion = intent.getStringExtra("ai_emotion");
        String tagsString = intent.getStringExtra("ai_tags_string");

        // 🌟 [팀원 코드 기능] 이전 화면에서 넘어온 원본 사진 주소 받기
        String diaryImageUri = intent.getStringExtra("diary_image_uri");

        // 2. 화면에 텍스트 뿌리기
        ((TextView)findViewById(R.id.tv_extracted_text_result)).setText(finalText);
        ((TextView)findViewById(R.id.tv_sentiment_result)).setText(aiEmotion);
        ((TextView)findViewById(R.id.tv_keywords_result)).setText(tagsString);

        // 3. 🌟 [내 코드 기능] AI가 만든 대표 스티커 액자에 띄우기
        ImageView iv = findViewById(R.id.iv_generated_sticker);
        if (stickerUrls != null && !stickerUrls.isEmpty()) {
            Glide.with(this).load(stickerUrls.get(0)).centerCrop().into(iv);
        }

        // 4. 뒤로가기 및 메인 버튼
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_main).setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });

        // 5. 스티커 생성(다음 화면으로 이동) 버튼
        findViewById(R.id.cv_generate_sticker).setOnClickListener(v -> {
            Intent next = new Intent(this, StickerPreviewActivity.class);

            // 🌟 [내 코드] 스티커 3장 리스트 넘기기
            next.putStringArrayListExtra("new_sticker_urls", stickerUrls);

            // 🌟 [팀원 코드] 사진 주소 및 기타 데이터 넘기기
            next.putExtra("diary_image_uri", diaryImageUri);
            next.putExtra("ai_emotion", aiEmotion);
            next.putExtra("final_text", finalText);

            startActivity(next);
            finish(); // 🌟 중복 방지를 위해 현재 창 닫기 유지
        });
    }
}