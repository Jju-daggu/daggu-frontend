package com.example.daggumaker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AnalysisActivity extends AppCompatActivity {

    // 팀원이 추가한 EditText 멤버 변수 (OCR 추출 텍스트 수정용)
    private EditText etExtractedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        // 1. 뷰 연결 (기본 버튼 +  EditText 추가)
        View btnBack = findViewById(R.id.btn_back);
        View btnMain = findViewById(R.id.btn_main);
        etExtractedText = findViewById(R.id.et_extracted_text);

        // 2. 뒤로가기 버튼: finish() 로직 적용 (Null 체크 포함)
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 3. 메인 버튼: Intent 플래그(중복 생성 방지) 로직 적용
        if (btnMain != null) {
            btnMain.setOnClickListener(v -> {
                Intent intent = new Intent(AnalysisActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        // 4. 분석 결과 제출 및 화면 전환 (데이터 전달 로직 포함)
        // XML ID가 'btn_create_sticker' 또는 'cv_analysis'인지 확인 후 연결
        View btnSubmit = findViewById(R.id.btn_create_sticker);
        if (btnSubmit == null) {
            btnSubmit = findViewById(R.id.cv_analysis);
        }

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                try {
                    // 사용자가 수정한 최종 텍스트 가져오기
                    String finalExtractedText = (etExtractedText != null) ? etExtractedText.getText().toString() : "";

                    // 결과 화면으로 데이터 전달하며 이동
                    Intent intent = new Intent(AnalysisActivity.this, ResultActivity.class);
                    // 목적지 액티비티 이름이 'StickerPreviewActivity'라면 아래 줄 수정 필요
                    intent.putExtra("edited_text", finalExtractedText);
                    startActivity(intent);

                } catch (Exception e) {
                    // 예외 발생 시 사용자에게 알림
                    Toast.makeText(AnalysisActivity.this, "화면 전환 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            });
        }
    }
}