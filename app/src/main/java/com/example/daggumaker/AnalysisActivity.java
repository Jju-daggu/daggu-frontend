package com.example.daggumaker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AnalysisActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        // 1. 뷰 연결
        TextView btnBack = findViewById(R.id.btn_back);
        TextView btnMain = findViewById(R.id.btn_main);
        // XML에 Button으로 되어 있으므로 타입을 맞춰줍니다.
        Button btnCreateSticker = findViewById(R.id.btn_create_sticker);

        // 2. 뒤로가기 버튼: 현재 화면 종료 (이전 화면으로 이동)
        btnBack.setOnClickListener(v -> finish());

        // 3. 메인 버튼: 메인화면으로 이동 (위의 스택 모두 제거)
        btnMain.setOnClickListener(v -> {
            Intent intent = new Intent(AnalysisActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        // 4. 스티커 생성 버튼: 미리보기 화면으로 이동
        btnCreateSticker.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(AnalysisActivity.this, StickerPreviewActivity.class);
                startActivity(intent);
                // 화면 전환 애니메이션을 넣고 싶다면 여기에 추가 가능합니다.
            } catch (Exception e) {
                // 혹시 화면 전환 시 오류가 나면 토스트 메시지로 알려줍니다.
                Toast.makeText(AnalysisActivity.this, "화면 전환 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
    }
}