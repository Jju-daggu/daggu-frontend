package com.example.daggumaker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AnalysisActivity extends AppCompatActivity {

    // OCR 추출 텍스트 수정용 EditText
    private EditText etExtractedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        // 1. 뷰 연결
        View btnBack = findViewById(R.id.btn_back);
        View btnMain = findViewById(R.id.btn_main);
        View btnSubmit = findViewById(R.id.cv_analysis); // XML에 있는 버튼 ID 사용
        etExtractedText = findViewById(R.id.et_extracted_text);

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

        // 4. 분석 버튼 클릭 → 결과 화면 이동
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                try {

                    // 사용자가 수정한 텍스트 가져오기
                    String finalExtractedText = "";
                    if (etExtractedText != null) {
                        finalExtractedText = etExtractedText.getText().toString();
                    }

                    // ResultActivity로 데이터 전달
                    Intent intent = new Intent(AnalysisActivity.this, ResultActivity.class);
                    intent.putExtra("edited_text", finalExtractedText);

                    startActivity(intent);

                } catch (Exception e) {
                    Toast.makeText(
                            AnalysisActivity.this,
                            "화면 전환 오류: " + e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                    e.printStackTrace();
                }
            });
        }
    }
}