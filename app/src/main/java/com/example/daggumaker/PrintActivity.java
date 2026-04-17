package com.example.daggumaker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView; // 복사본 반영
import androidx.print.PrintHelper;

import com.bumptech.glide.Glide;
import java.util.ArrayList;

public class PrintActivity extends AppCompatActivity {

    private CardView llSelectedSticker; // 복사본 반영: LinearLayout -> CardView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);

        llSelectedSticker = findViewById(R.id.ll_selected_sticker);
        TextView btnBack = findViewById(R.id.btn_back);
        TextView btnMain = findViewById(R.id.btn_main);
        TextView btnPrintTrigger = findViewById(R.id.btn_print_trigger); // 복사본 반영: Button -> TextView

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnMain != null) {
            btnMain.setOnClickListener(v -> {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
        }

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

        if (btnPrintTrigger != null) {
            btnPrintTrigger.setOnClickListener(v -> {
                doPrint();
            });
        }
    }

    private void doPrint() {
        Bitmap bitmap = Bitmap.createBitmap(llSelectedSticker.getWidth(), llSelectedSticker.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        canvas.drawColor(Color.WHITE);
        llSelectedSticker.draw(canvas);

        PrintHelper printHelper = new PrintHelper(this);
        printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);

        String jobName = getString(R.string.app_name) + "_Sticker_Print";
        printHelper.printBitmap(jobName, bitmap);

        Toast.makeText(this, "인쇄 화면을 준비 중입니다...", Toast.LENGTH_SHORT).show();
    }
}