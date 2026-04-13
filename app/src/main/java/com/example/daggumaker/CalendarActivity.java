package com.example.daggumaker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    private TextView tvMonth, tvSelectedDay;
    private GridView gvCalendar;
    private View vDetailEmotion;

    // xml에 만들어져 있는 키워드 텍스트뷰를 담을 변수
    private TextView tvKeywords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // 1. 뷰 연결
        tvMonth = findViewById(R.id.tv_month);
        tvSelectedDay = findViewById(R.id.tv_selected_day);
        gvCalendar = findViewById(R.id.gv_calendar);
        vDetailEmotion = findViewById(R.id.v_detail_emotion);
        TextView btnBack = findViewById(R.id.btn_back);

        // ✨ xml에 있는 실제 아이디(tv_keywords) 연결
        tvKeywords = findViewById(R.id.tv_keywords);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 2. 캘린더 생성 및 어댑터 연결
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

                // 하단 박스의 감정 원형을 빨간색으로 활성화 (현재 임시 로직)
                if (vDetailEmotion != null) {
                    vDetailEmotion.setBackgroundResource(R.drawable.bg_circle_red);
                }

                // ✨ 선택한 날짜의 키워드를 SharedPreferences에서 불러와 화면에 띄우기
                loadKeywordsForDate(month, selectedDate);
            }
        });
    }

    // ✨ 특정 날짜의 저장된 키워드를 가져와서 UI를 업데이트하는 메서드
    private void loadKeywordsForDate(int currentMonth, String day) {
        // ResultActivity에서 저장했던 동일한 이름("DailyKeywordMemory")의 저장소 열기
        SharedPreferences dailyPrefs = getSharedPreferences("DailyKeywordMemory", MODE_PRIVATE);

        // 1. Key("yyyy-MM-dd") 조립
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);

        // 날짜를 무조건 "2026-04-05" 처럼 두 자리 포맷으로 맞춤
        String dateKey = String.format(Locale.KOREA, "%04d-%02d-%02d", year, currentMonth, Integer.parseInt(day));

        // 2. 해당 날짜로 저장된 키워드 꺼내기 (데이터가 없으면 기본 안내 문구)
        String savedKeywords = dailyPrefs.getString(dateKey, "일기가 없어요ㅠ 📝");

        // 3. UI (tv_keywords) 업데이트
        if (tvKeywords != null) {
            if (!savedKeywords.equals("일기가 없어요ㅠ 📝") && !savedKeywords.isEmpty()) {

                // ✨ [수정] 쉼표(", ")를 기준으로 문자열을 배열로 쪼갭니다.
                String[] words = savedKeywords.split(", ");
                StringBuilder sb = new StringBuilder();

                // ✨ [수정] 배열의 크기와 3 중에 더 작은 값을 한계치로 설정 (키워드가 3개 미만일 경우 에러 방지)
                int limit = Math.min(3, words.length);

                // ✨ [수정] 딱 limit 갯수(최대 3개)만큼만 해시태그를 붙여서 조립합니다.
                for (int i = 0; i < limit; i++) {
                    sb.append("#\u2060").append(words[i]).append(" ");
                }

                // 완성된 3개의 키워드 문자열을 세팅 (맨 끝 공백 제거를 위해 trim() 사용)
                tvKeywords.setText(sb.toString().trim());

            } else {
                tvKeywords.setText(savedKeywords);
            }
        }
    }
}