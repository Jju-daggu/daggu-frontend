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
        View btnSubmit = findViewById(R.id.cv_analysis);
        etExtractedText = findViewById(R.id.et_extracted_text);

        // [추가] UploadActivity로부터 전달받은 OCR 텍스트 설정
        String initialText = getIntent().getStringExtra("extracted_text");
        if (initialText != null && etExtractedText != null) {
            etExtractedText.setText(initialText);
        }

        // 2. 뒤로가기 버튼
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // 3. 메인 버튼
        if (btnMain != null) {
            btnMain.setOnClickListener(v -> {
                Intent intent = new Intent(AnalysisActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        // 4. 분석 버튼 클릭 → ResultActivity로 이동 (텍스트 및 이미지 경로 전달)
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                String finalText = etExtractedText.getText().toString();
                Intent intent = new Intent(AnalysisActivity.this, ResultActivity.class);

                // 수정된 텍스트 전달
                intent.putExtra("final_text", finalText);

                // ✨ [추가] 배달온 이미지 경로를 그대로 다음 화면(ResultActivity)으로 전달
                intent.putExtra("diary_image_uri", getIntent().getStringExtra("diary_image_uri"));

                startActivity(intent);
            });
        }
    }
}