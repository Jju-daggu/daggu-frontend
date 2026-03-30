package com.example.daggumaker;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import java.util.ArrayList;

public class VaultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vault);

        // 1. 뒤로가기 버튼
        TextView btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 2. 레이아웃 찾기
        LinearLayout layoutNewStickers = findViewById(R.id.layout_new_stickers);

        // 3. 사진 데이터 리스트 받기
        ArrayList<String> stickerUriList = getIntent().getStringArrayListExtra("sticker_uri_list");

        // 🌟 [핵심 로직]
        if (stickerUriList != null && !stickerUriList.isEmpty()) {
            // 전달받은 사진이 있으면 상단 영역을 보여줍니다. (샘플은 자동으로 아래로 밀림)
            layoutNewStickers.setVisibility(View.VISIBLE);

            int[] newImageViewIds = {
                    R.id.iv_new_1,
                    R.id.iv_new_2,
                    R.id.iv_new_3,
                    R.id.iv_new_4,
                    R.id.iv_new_5
            };

            for (int i = 0; i < stickerUriList.size(); i++) {
                if (i < newImageViewIds.length) {
                    ImageView targetView = findViewById(newImageViewIds[i]);
                    if (targetView != null) {
                        Glide.with(this)
                                .load(stickerUriList.get(i))
                                .into(targetView);
                    }
                }
            }
        } else {
            // 전달받은 사진이 없으면 상단 영역을 아예 보이지 않게(GONE) 합니다.
            layoutNewStickers.setVisibility(View.GONE);
        }
    }
}