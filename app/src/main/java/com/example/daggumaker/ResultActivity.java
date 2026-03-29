package com.example.daggumaker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

        // 3. 스티커 생성 버튼 클릭 시
        if (cvSticker != null) {
            cvSticker.setOnClickListener(v -> {
                // 스티커 생성 로직이나 다음 화면 연결
            });
        }
    }
}