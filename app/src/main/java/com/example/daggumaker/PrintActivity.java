package com.example.daggumaker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.print.PrintHelper; // 인쇄를 위한 중요 라이브러리

import com.bumptech.glide.Glide;
import java.util.ArrayList;

public class PrintActivity extends AppCompatActivity {

    private LinearLayout llSelectedSticker; // 인쇄할 영역

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);

        // 1. 뷰 연결
        llSelectedSticker = findViewById(R.id.ll_selected_sticker);
        TextView btnBack = findViewById(R.id.btn_back);
        TextView btnMain = findViewById(R.id.btn_main);
        Button btnPrintTrigger = findViewById(R.id.btn_print_trigger);

        // 2. 뒤로가기 버튼
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // 3. 메인으로 버튼
        if (btnMain != null) {
            btnMain.setOnClickListener(v -> {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
        }

        // 4. 전달받은 스티커 리스트 뿌리기
        ArrayList<String> stickerUriList = getIntent().getStringArrayListExtra("sticker_uri_list");
        if (stickerUriList != null && !stickerUriList.isEmpty()) {
            int[] printViewIds = {
                    R.id.iv_print_1, R.id.iv_print_2, R.id.iv_print_3, R.id.iv_print_4, R.id.iv_print_5
            };

            for (int i = 0; i < stickerUriList.size(); i++) {
                if (i < printViewIds.length) {
                    ImageView targetView = findViewById(printViewIds[i]);
                    if (targetView != null) {
                        Glide.with(this).load(stickerUriList.get(i)).into(targetView);
                    }
                }
            }
        }

        // 5. 🌟 [프린트 하기] 버튼 클릭 시 실제 인쇄 기능 실행
        if (btnPrintTrigger != null) {
            btnPrintTrigger.setOnClickListener(v -> {
                doPrint(); // 인쇄 함수 호출
            });
        }
    }

    /**
     * 🖨️ 실제 인쇄를 수행하는 함수
     */
    private void doPrint() {
        // [A] 인쇄할 영역(스티커 카드)을 비트맵 이미지로 캡처합니다.
        Bitmap bitmap = Bitmap.createBitmap(llSelectedSticker.getWidth(), llSelectedSticker.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // 배경을 하얗게 칠해줘야 인쇄했을 때 깔끔합니다.
        canvas.drawColor(Color.WHITE);
        llSelectedSticker.draw(canvas);

        // [B] 안드로이드 표준 PrintHelper를 사용하여 인쇄창을 띄웁니다.
        PrintHelper printHelper = new PrintHelper(this);

        // SCALE_MODE_FIT: 종이 크기에 맞춰서 스티커 이미지를 조절합니다.
        printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);

        // [C] 시스템 인쇄 서비스에 이미지 전달
        String jobName = getString(R.string.app_name) + "_Sticker_Print";
        printHelper.printBitmap(jobName, bitmap);

        Toast.makeText(this, "인쇄 화면을 준비 중입니다...", Toast.LENGTH_SHORT).show();
    }
}