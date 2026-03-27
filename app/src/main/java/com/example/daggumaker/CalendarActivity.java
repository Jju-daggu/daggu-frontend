package com.example.daggumaker;

import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Calendar;

public class CalendarActivity extends AppCompatActivity {

    private TextView tvMonth, tvSelectedDay;
    private GridView gvCalendar;
    private View vDetailEmotion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // 뷰 연결
        tvMonth = findViewById(R.id.tv_month);
        tvSelectedDay = findViewById(R.id.tv_selected_day);
        gvCalendar = findViewById(R.id.gv_calendar);
        vDetailEmotion = findViewById(R.id.v_detail_emotion);
        TextView btnBack = findViewById(R.id.btn_back);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 캘린더 생성 및 어댑터 연결
        setupCalendar();
    }

    private void setupCalendar() {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;
        tvMonth.setText(month + "월"); // 현재 달 자동 표시

        // 이번 달 1일이 무슨 요일인지 계산
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int startDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        ArrayList<String> dayList = new ArrayList<>();

        // 월요일 시작 기준으로 빈 칸(gap) 계산
        int gap = (startDayOfWeek == Calendar.SUNDAY) ? 6 : startDayOfWeek - 2;
        for (int i = 0; i < gap; i++) {
            dayList.add("");
        }

        // 1일부터 마지막 날짜까지 추가
        for (int i = 1; i <= maxDay; i++) {
            dayList.add(String.valueOf(i));
        }

        // 커스텀 어댑터 적용 (격자 디자인 반영)
        CalendarAdapter adapter = new CalendarAdapter(this, dayList);
        gvCalendar.setAdapter(adapter);

        // 날짜 클릭 이벤트: 하단 상세 박스 업데이트
        gvCalendar.setOnItemClickListener((parent, view, position, id) -> {
            String selectedDate = dayList.get(position);
            if (!selectedDate.equals("")) {
                tvSelectedDay.setText(selectedDate + "일");
                // 하단 박스의 감정 원형도 빨간색으로 활성화
                if (vDetailEmotion != null) {
                    vDetailEmotion.setBackgroundResource(R.drawable.bg_circle_red);
                }
            }
        });
    }
}