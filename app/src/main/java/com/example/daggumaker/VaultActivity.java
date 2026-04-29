package com.example.daggumaker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

public class VaultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vault);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_main).setOnClickListener(v -> finish());

        int[] allImageIds = {
                R.id.iv_new_1, R.id.iv_new_2, R.id.iv_new_3, R.id.iv_new_4, R.id.iv_new_5,
                R.id.iv_vault_1, R.id.iv_vault_2, R.id.iv_vault_3, R.id.iv_vault_4, R.id.iv_vault_5
        };

        SharedPreferences prefs = getSharedPreferences("StickerVault", Context.MODE_PRIVATE);
        String savedData = prefs.getString("uris", "");
        ArrayList<String> validUrls = new ArrayList<>();

        if (!savedData.isEmpty()) {
            for (String s : savedData.split(",")) {
                if (s != null && s.trim().startsWith("http")) validUrls.add(s.trim());
            }
        }

        for (int i = 0; i < allImageIds.length; i++) {
            ImageView targetView = findViewById(allImageIds[i]);
            if (targetView != null) {
                if (i < validUrls.size()) {
                    targetView.setVisibility(View.VISIBLE);
                    // 🌟 보관함에서도 잘림 방지
                    Glide.with(this).load(validUrls.get(i)).fitCenter().into(targetView);
                } else {
                    targetView.setImageDrawable(null);
                    // 🌟 핵심! 구멍이 숭숭 뚫리지 않고 자리를 지키도록 설정
                    targetView.setVisibility(View.INVISIBLE);
                }
            }
        }
    }
}