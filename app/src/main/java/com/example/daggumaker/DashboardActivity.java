package com.example.daggumaker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // 1. 뷰 연결
        View tvCalendar = findViewById(R.id.tv_view_calendar);
        View btnUpload = findViewById(R.id.btn_upload);
        View btnVault = findViewById(R.id.btn_vault);

        // 2. 날짜 자동 업데이트 실행
        updateRecentDates();

        // 3. 클릭 리스너 설정
        if (tvCalendar != null) {
            tvCalendar.setOnClickListener(v -> startActivityWithCheck(CalendarActivity.class));
        }

        if (btnUpload != null) {
            btnUpload.setOnClickListener(v -> startActivityWithCheck(UploadActivity.class));
        }

        if (btnVault != null) {
            btnVault.setOnClickListener(v -> startActivityWithCheck(VaultActivity.class));
        }
    }

    // 오늘 날짜를 기준으로 최근 4일의 '일' 정보를 텍스트뷰에 세팅함
    private void updateRecentDates() {
        TextView[] dateViews = {
                findViewById(R.id.tv_date_1),
                findViewById(R.id.tv_date_2),
                findViewById(R.id.tv_date_3),
                findViewById(R.id.tv_date_4)
        };

        Calendar cal = Calendar.getInstance(); // 현재 시간 정보 가져오기

        for (int i = 0; i < 4; i++) {
            if (dateViews[i] != null) {
                // 현재 설정된 날짜의 '일(Day)' 숫자 가져오기
                int day = cal.get(Calendar.DAY_OF_MONTH);
                dateViews[i].setText(String.valueOf(day));

                // 루프가 돌 때마다 하루씩 전으로 이동
                cal.add(Calendar.DAY_OF_MONTH, -1);
            }
        }
    }

    private void startActivityWithCheck(Class<?> cls) {
        try {
            Intent intent = new Intent(this, cls);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, cls.getSimpleName() + " 화면을 먼저 생성해주세요!", Toast.LENGTH_SHORT).show();
        }
    }
}