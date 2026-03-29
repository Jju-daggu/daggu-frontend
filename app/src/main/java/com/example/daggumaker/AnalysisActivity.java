package com.example.daggumaker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText; // ✨ EditText 임포트
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AnalysisActivity extends AppCompatActivity {

    // ✨ 나중에 수정된 텍스트를 가져오기 위해 멤버 변수로 선언
    private EditText etExtractedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        // 1. 뷰 연결 (XML에서 변경한 ID 사용)
        View btnBack = findViewById(R.id.btn_back);
        View btnMain = findViewById(R.id.btn_main);
        etExtractedText = findViewById(R.id.et_extracted_text); // ✨ EditText로 연결

        // ---------------------------------------------------------
        // ✨ [백엔드 팀을 위한 영역] 나중에 여기에 코드를 넣습니다.
        // ---------------------------------------------------------

        // (가짜 데이터 세팅 예시)
        // 백엔드에서 "오늘 날씨 맑음, 기분 최고!"라는 OCR 결과를 받았다고 가정하면:
        // etExtractedText.setText("오늘 날씨 맑음, 기분 최고!");

        // 이제 사용자는 화면을 터치해서 이 글자를 "최고"를 "나쁨"으로 수정할 수 있습니다.
        // ---------------------------------------------------------

        // 2. 뒤로가기 버튼
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 3. 메인 버튼
        if (btnMain != null) {
            btnMain.setOnClickListener(v -> {
                Intent intent = new Intent(AnalysisActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        // 4. 분석 제출 버튼
        View btnSubmit = findViewById(R.id.cv_analysis);
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                // ✨ 중요: 사용자가 수정한 최종 텍스트를 가져옵니다.
                String finalExtractedText = etExtractedText.getText().toString();

                // (선택사항) 텍스트가 비어있는지 체크
                if (finalExtractedText.trim().isEmpty()) {
                    // Toast.makeText(this, "텍스트를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    // return;
                }

                // 수정된 텍스트를 결과 화면으로 넘겨줍니다.
                Intent intent = new Intent(AnalysisActivity.this, ResultActivity.class);
                intent.putExtra("edited_text", finalExtractedText); // ✨ 결과 화면에 데이터 전달
                startActivity(intent);
            });
        }
    }
}