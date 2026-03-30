package com.example.daggumaker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView; // TextView 사용을 위해 추가
import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        View btnBack = findViewById(R.id.btn_back);
        View btnMain = findViewById(R.id.btn_main);
        View cvSticker = findViewById(R.id.cv_generate_sticker);

        // 1. 뒤로가기
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 2. 메인으로 (스택 정리)
        if (btnMain != null) {
            btnMain.setOnClickListener(v -> {
                Intent intent = new Intent(ResultActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        // 3. 스티커 생성 버튼 클릭 시 (미리보기 화면으로 이동)
        if (cvSticker != null) {
            cvSticker.setOnClickListener(v -> {
                // StickerPreviewActivity로 이동하는 Intent 생성
                Intent intent = new Intent(ResultActivity.this, StickerPreviewActivity.class);

                // (선택사항) 만약 분석된 텍스트나 키워드를 다음 화면에 전달하고 싶다면 아래처럼 작성하세요.
                // intent.putExtra("key", "value");

                startActivity(intent);

                // 화면 전환 애니메이션을 넣고 싶다면 (커스텀 애니메이션이 있을 경우)
                // overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            });
        }
    }
}