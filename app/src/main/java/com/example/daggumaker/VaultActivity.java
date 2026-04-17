package com.example.daggumaker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet; // 중복 제거를 위해 추가

public class VaultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vault);

        TextView btnBack = findViewById(R.id.btn_back);
        TextView btnMain = findViewById(R.id.btn_main);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (btnMain != null) btnMain.setOnClickListener(v -> finish()); // 혹은 메인 이동 로직

        // 1. 🖼️ 기존 더미 이미지 싹 비우기
        int[] allImageIds = {
                R.id.iv_new_1, R.id.iv_new_2, R.id.iv_new_3, R.id.iv_new_4, R.id.iv_new_5,
                R.id.iv_vault_1, R.id.iv_vault_2, R.id.iv_vault_3, R.id.iv_vault_4, R.id.iv_vault_5
        };
        for (int id : allImageIds) {
            ImageView iv = findViewById(id);
            if (iv != null) iv.setImageDrawable(null);
        }

        // 2. 💾 저장소에서 데이터 불러오기 (LinkedHashSet으로 중복 자동 제거)
        SharedPreferences prefs = getSharedPreferences("StickerVault", Context.MODE_PRIVATE);
        String savedData = prefs.getString("uris", "");

        // 중복을 허용하지 않으면서 순서를 유지하는 Set 사용
        LinkedHashSet<String> stickerSet = new LinkedHashSet<>();
        if (!savedData.isEmpty()) {
            stickerSet.addAll(Arrays.asList(savedData.split(",")));
        }

        // 3. 📩 새로 들어온 스티커 추가 (최신 것이 맨 앞으로 오게 처리)
        ArrayList<String> newStickerUris = getIntent().getStringArrayListExtra("sticker_uri_list");
        if (newStickerUris != null && !newStickerUris.isEmpty()) {
            // 새로운 리스트를 만들어서 [새것 + 기존것] 순서로 합침 (중복은 Set이 알아서 거름)
            ArrayList<String> combinedList = new ArrayList<>(newStickerUris);
            combinedList.addAll(stickerSet);

            stickerSet.clear();
            stickerSet.addAll(combinedList);

            // 💾 깨끗해진 목록을 다시 저장
            StringBuilder sb = new StringBuilder();
            for (String uri : stickerSet) {
                if (sb.length() > 0) sb.append(",");
                sb.append(uri);
            }
            prefs.edit().putString("uris", sb.toString()).apply();
        }

        // 4. 🎨 화면에 그리기
        ArrayList<String> finalDisplayList = new ArrayList<>(stickerSet);
        for (int i = 0; i < finalDisplayList.size() && i < allImageIds.length; i++) {
            ImageView targetView = findViewById(allImageIds[i]);
            if (targetView != null) {
                Glide.with(this)
                        .load(finalDisplayList.get(i))
                        .into(targetView);
            }
        }
    }
}