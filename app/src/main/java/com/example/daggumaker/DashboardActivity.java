package com.example.daggumaker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // 1. 사진 업로드 버튼 연결
        // XML에서 Button을 사용하셨으므로 View로 찾거나 AppCompatButton으로 캐스팅합니다.
        View btnUpload = findViewById(R.id.btn_upload);
        if (btnUpload != null) {
            btnUpload.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, UploadActivity.class);
                startActivity(intent);
            });
        }

        // 2. 스티커 보관함 버튼 연결
        View btnVault = findViewById(R.id.btn_vault);
        if (btnVault != null) {
            btnVault.setOnClickListener(v -> {
                // 아직 VaultActivity를 만들지 않았다면 오류가 날 수 있습니다.
                // 파일이 있는지 확인 후 주석을 해제하세요.
                Intent intent = new Intent(DashboardActivity.this, VaultActivity.class);
                startActivity(intent);
            });
        }

        // 참고: 이전에 '전체보기' 기능을 삭제하기로 했으므로 관련 tvCalendar 코드는 제외했습니다.
    }
}