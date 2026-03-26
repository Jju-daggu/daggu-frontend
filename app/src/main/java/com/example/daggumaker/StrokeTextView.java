package com.example.daggumaker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;

public class StrokeTextView extends AppCompatTextView {
    private boolean stroke = true;
    private float strokeWidth = 6.0f;
    private int strokeColor = 0xFF000000; // 기본 검정색

    public StrokeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
    }

    public StrokeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        if (attrs == null) return;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StrokeTextView);
        try {
            stroke = a.getBoolean(R.styleable.StrokeTextView_text_stroke, true);
            // XML에서 설정한 dp 값을 px로 변환하여 가져옴
            strokeWidth = a.getDimensionPixelSize(R.styleable.StrokeTextView_text_stroke_width, 6);
            strokeColor = a.getColor(R.styleable.StrokeTextView_text_stroke_color, 0xFF000000);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (stroke) {
            // 1. 테두리 먼저 그리기
            getPaint().setStyle(Paint.Style.STROKE);
            getPaint().setStrokeWidth(strokeWidth);
            int originalColor = getCurrentTextColor();

            setTextColor(strokeColor);
            super.onDraw(canvas); // 테두리 출력

            // 2. 그 위에 글자 채우기
            getPaint().setStyle(Paint.Style.FILL);
            setTextColor(originalColor);
        }
        super.onDraw(canvas); // 글자 채우기 출력
    }
}