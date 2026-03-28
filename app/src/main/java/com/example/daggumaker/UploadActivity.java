package com.example.daggumaker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class UploadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // 1. 뒤로가기 버튼 연결
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish()); // 현재 화면 종료 (이전 화면으로)
        }

        // 2. 업로드 제출 버튼 연결 (XML의 CardView ID인 cv_upload_submit 사용 권장)
        View btnSubmit = findViewById(R.id.cv_upload_submit);

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                // 분석 화면(AnalysisActivity)으로 이동
                Intent intent = new Intent(UploadActivity.this, AnalysisActivity.class);
                startActivity(intent);
            });
        }
    }
}