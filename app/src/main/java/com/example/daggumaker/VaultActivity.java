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

        // 1. 상단 타이틀 및 뒤로가기 버튼 설정
        TextView btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 2. 새로운 스티커가 들어갈 상단 레이아웃 찾기
        LinearLayout layoutNewStickers = findViewById(R.id.layout_new_stickers);

        // 3. StickerPreviewActivity에서 보낸 사진 데이터 리스트 받기
        // (주의: 보낼 때와 받는 때의 키 값이 "sticker_uri_list"로 동일해야 합니다)
        ArrayList<String> stickerUriList = getIntent().getStringArrayListExtra("sticker_uri_list");

        // 4. 새로운 사진 처리 로직
        if (stickerUriList != null && !stickerUriList.isEmpty()) {
            // 전달받은 사진이 있으면 상단 영역(layout_new_stickers)을 보여줍니다.
            // XML 구조상 이 레이아웃이 위에 있으므로 기존 사진들은 자동으로 아래로 밀립니다.
            if (layoutNewStickers != null) {
                layoutNewStickers.setVisibility(View.VISIBLE);
            }

            // 상단에 배치될 새로운 이미지 뷰 ID 배열
            int[] newImageViewIds = {
                    R.id.iv_new_1,
                    R.id.iv_new_2,
                    R.id.iv_new_3,
                    R.id.iv_new_4,
                    R.id.iv_new_5
            };

            // 리스트에 담긴 Uri를 하나씩 이미지 뷰에 로드
            for (int i = 0; i < stickerUriList.size(); i++) {
                if (i < newImageViewIds.length) {
                    ImageView targetView = findViewById(newImageViewIds[i]);
                    if (targetView != null) {
                        Glide.with(this)
                                .load(stickerUriList.get(i))
                                .placeholder(android.R.color.transparent) // 로딩 전 투명 처리
                                .into(targetView);
                    }
                }
            }
        } else {
            // 전달받은 사진이 없으면 상단 영역을 아예 보이지 않게(GONE) 설정합니다.
            if (layoutNewStickers != null) {
                layoutNewStickers.setVisibility(View.GONE);
            }
        }
    }
}