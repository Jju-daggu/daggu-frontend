package com.example.daggumaker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class CalendarAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> dayList;
    private LayoutInflater inflater;

    public CalendarAdapter(Context context, ArrayList<String> dayList) {
        this.context = context;
        this.dayList = dayList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() { return dayList.size(); }
    @Override
    public Object getItem(int position) { return dayList.get(position); }
    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_calendar_day, parent, false);
        }

        TextView tvDate = convertView.findViewById(R.id.tv_date);
        View ivEmotion = convertView.findViewById(R.id.iv_emotion);

        String day = dayList.get(position);
        tvDate.setText(day);

        // 시안처럼 격자 테두리를 유지하기 위해, 날짜가 없는 공백칸도 레이아웃은 보여줍니다.
        // 나중에 데이터가 연결되면 ivEmotion의 visibility를 바꾸고 배경색을 설정하면 됩니다.
        if (day.equals("")) {
            ivEmotion.setVisibility(View.INVISIBLE);
        } else {
            // 임시로 모든 날짜에 빨간 원을 보여줍니다. (시안 느낌 확인용)
            ivEmotion.setVisibility(View.VISIBLE);
        }

        return convertView;
    }
}