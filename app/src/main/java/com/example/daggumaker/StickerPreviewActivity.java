package com.example.daggumaker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.ArrayList;
import java.util.LinkedHashSet;

public class StickerPreviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_preview);

        ImageView[] ivs = {
                findViewById(R.id.iv_sticker_1),
                findViewById(R.id.iv_sticker_2),
                findViewById(R.id.iv_sticker_3),
                findViewById(R.id.iv_sticker_4),
                findViewById(R.id.iv_sticker_5)
        };

        // 🌟 1. 데이터 받기 (스티커 URL & 팀원분이 ResultActivity에서 넘겨준 다이어리 원본 사진 URI)
        ArrayList<String> urls = getIntent().getStringArrayListExtra("new_sticker_urls");
        String diaryImageUri = getIntent().getStringExtra("diary_image_uri"); // 추가됨!
        ArrayList<String> validUrls = new ArrayList<>();

        if (urls != null) {
            for (String u : urls) {
                if (u != null && u.trim().startsWith("http")) validUrls.add(u.trim());
            }

            for (int i = 0; i < ivs.length; i++) {
                if (ivs[i] != null) {
                    if (i < validUrls.size() && i < 3) {
                        ivs[i].setVisibility(View.VISIBLE);
                        // 잘림 방지! fitCenter()로 원본 비율 유지
                        Glide.with(this)
                                .load(validUrls.get(i))
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .fitCenter()
                                .into(ivs[i]);
                    } else {
                        ivs[i].setImageDrawable(null);
                        // 자리 유지! 레이아웃이 안 망가지게 INVISIBLE 사용
                        ivs[i].setVisibility(View.INVISIBLE);
                    }
                }
            }

            if (!validUrls.isEmpty()) saveToVault(validUrls);
        }

        // 🌟 2. 기존 버튼들 (보관함, 뒤로가기, 메인)
        findViewById(R.id.btn_store).setOnClickListener(v -> startActivity(new Intent(this, VaultActivity.class)));
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_main).setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finish();
        });

        // 🌟 3. [복구 완료] 배치 버튼 기능!
        View btnArrange = findViewById(R.id.btn_place); // btn_place로 변경!
        if (btnArrange != null) {
            btnArrange.setOnClickListener(v -> {
                // 🚨 중요: ArrangeActivity 부분은 팀원분이 만드신 '배치 화면 자바 파일 이름'으로 꼭 바꿔주세요!
                Intent arrangeIntent = new Intent(StickerPreviewActivity.this, PlacementActivity.class);

                // 팀원분이 원하시는 '일기 원본 사진 주소'와 세민 님이 뽑은 '새 스티커 3장'을 토스!
                arrangeIntent.putStringArrayListExtra("new_sticker_urls", validUrls);
                arrangeIntent.putExtra("diary_image_uri", diaryImageUri);

                startActivity(arrangeIntent);
            });
        }
    }

    // 보관함 데이터베이스 완벽 청소 및 순서 정렬 (LinkedHashSet 활용)
    private void saveToVault(ArrayList<String> newUrls) {
        SharedPreferences prefs = getSharedPreferences("StickerVault", Context.MODE_PRIVATE);
        String savedData = prefs.getString("uris", "");

        LinkedHashSet<String> uniqueUrls = new LinkedHashSet<>();

        for (String u : newUrls) {
            if (u != null && u.trim().startsWith("http")) uniqueUrls.add(u.trim());
        }
        if (!savedData.isEmpty()) {
            for (String s : savedData.split(",")) {
                if (s != null && s.trim().startsWith("http")) uniqueUrls.add(s.trim());
            }
        }

        String result = TextUtils.join(",", uniqueUrls);
        prefs.edit().putString("uris", result).apply();
    }
}