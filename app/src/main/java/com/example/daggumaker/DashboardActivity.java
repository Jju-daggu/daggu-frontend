package com.example.daggumaker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // XML ID와 매칭
        View tvCalendar = findViewById(R.id.tv_view_calendar);
        View btnUpload = findViewById(R.id.btn_upload);
        View btnVault = findViewById(R.id.btn_vault);

        // 클릭 리스너 설정
        if (tvCalendar != null) {
            tvCalendar.setOnClickListener(v -> {
                startActivityWithCheck(CalendarActivity.class);
            });
        }

        if (btnUpload != null) {
            btnUpload.setOnClickListener(v -> {
                startActivityWithCheck(UploadActivity.class);
            });
        }

        if (btnVault != null) {
            btnVault.setOnClickListener(v -> {
                startActivityWithCheck(VaultActivity.class);
            });
        }
    }

    // 액티비티 존재 여부 체크용 메소드
    private void startActivityWithCheck(Class<?> cls) {
        try {
            Intent intent = new Intent(this, cls);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, cls.getSimpleName() + " 화면을 먼저 생성해주세요!", Toast.LENGTH_SHORT).show();
        }
    }
}