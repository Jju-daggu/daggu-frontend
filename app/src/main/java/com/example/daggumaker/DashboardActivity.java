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

        // 1. 뷰 연결 (본인과 팀원 코드 통합)
        TextView tvViewCalendar = findViewById(R.id.tv_view_calendar);
        View btnUpload = findViewById(R.id.btn_upload);
        View btnVault = findViewById(R.id.btn_vault);

        // 2. 날짜 자동 업데이트 실행 (팀원 로직: 오늘 날짜 기준 4일치 동기화)
        updateRecentDates();

        // 3. 클릭 리스너 설정 (본인의 Intent 로직 + 팀원의 예외 처리 메소드 활용)

        // '전체보기' 클릭 시 이동 (XML에 존재할 경우에만 작동)
        if (tvViewCalendar != null) {
            tvViewCalendar.setOnClickListener(v -> startActivityWithCheck(CalendarActivity.class));
        }

        // 사진 업로드 버튼 클릭 시 이동
        if (btnUpload != null) {
            btnUpload.setOnClickListener(v -> startActivityWithCheck(UploadActivity.class));
        }

        // 스티커 보관함 버튼 클릭 시 이동
        if (btnVault != null) {
            btnVault.setOnClickListener(v -> startActivityWithCheck(VaultActivity.class));
        }
    }

    /**
     * 팀원 제공 로직: 오늘 날짜를 기준으로 최근 4일의 '일' 정보를 텍스트뷰에 세팅함
     */
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
                // 현재 설정된 날짜의 '일(Day)' 숫자 가져오기
                int day = cal.get(Calendar.DAY_OF_MONTH);
                dateViews[i].setText(String.valueOf(day));

                // 하루씩 과거로 이동하며 텍스트 세팅
                cal.add(Calendar.DAY_OF_MONTH, -1);
            }
        }
    }

    /**
     * 안전한 액티비티 이동을 위한 공통 메소드
     */
    private void startActivityWithCheck(Class<?> cls) {
        try {
            Intent intent = new Intent(this, cls);
            // 메인급 화면 이동 시 중복 생성을 방지하고 싶다면 플래그 추가 가능
            // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        } catch (Exception e) {
            // 연결된 액티비티 파일이 아직 없을 경우 앱이 꺼지지 않게 방어
            Toast.makeText(this, cls.getSimpleName() + " 화면이 아직 준비되지 않았습니다.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}