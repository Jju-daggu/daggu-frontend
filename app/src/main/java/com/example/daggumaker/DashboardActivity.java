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

        TextView tvViewCalendar = findViewById(R.id.tv_view_calendar); // '전체보기 〉' 텍스트뷰
        View btnUpload = findViewById(R.id.btn_upload);
        View btnVault = findViewById(R.id.btn_vault);

        // 2. 날짜 자동 업데이트 실행 (오늘 날짜 기준 4일치 동기화)
        updateRecentDates();

        // 3. 클릭 리스너 설정

        // '전체보기 〉' 클릭 시 캘린더 화면으로 이동
        if (tvViewCalendar != null) {
            tvViewCalendar.setOnClickListener(v -> {
                startActivityWithCheck(CalendarActivity.class);
            });
        }

        // 스티커 업로드 버튼 클릭 시 이동
        if (btnUpload != null) {
            btnUpload.setOnClickListener(v -> {
                startActivityWithCheck(UploadActivity.class);
            });
        }

        // 스티커 보관함 버튼 클릭 시 이동
        if (btnVault != null) {
            btnVault.setOnClickListener(v -> {
                startActivityWithCheck(VaultActivity.class);
            });
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

        Calendar cal = Calendar.getInstance(); // 현재 시스템 시간 가져오기

        for (int i = 0; i < 4; i++) {
            if (dateViews[i] != null) {
                // 현재 설정된 날짜의 '일(Day)' 숫자 가져오기 (예: 27)
                int day = cal.get(Calendar.DAY_OF_MONTH);
                dateViews[i].setText(String.valueOf(day));

                // 루프가 돌 때마다 하루씩 전으로 이동 (27 -> 26 -> 25 -> 24)
                cal.add(Calendar.DAY_OF_MONTH, -1);
            }
        }
    }

    // 액티비티 이동 및 예외 처리 메소드
    private void startActivityWithCheck(Class<?> cls) {
        try {
            Intent intent = new Intent(this, cls);
            startActivity(intent);
        } catch (Exception e) {
            // 해당 액티비티 클래스가 프로젝트에 생성되어 있지 않을 경우 알림 표시
            Toast.makeText(this, cls.getSimpleName() + " 화면을 먼저 생성해주세요!", Toast.LENGTH_SHORT).show();
        }
    }
}