package com.example.daggumaker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AnalysisActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        TextView btnBack = findViewById(R.id.btn_back);
        TextView btnMain = findViewById(R.id.btn_main);
        Button btnCreateSticker = findViewById(R.id.btn_create_sticker);

        btnBack.setOnClickListener(v -> finish());
        btnMain.setOnClickListener(v -> {
            Intent intent = new Intent(AnalysisActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        btnCreateSticker.setOnClickListener(v -> {
            Intent intent = new Intent(AnalysisActivity.this, StickerPreviewActivity.class);
            startActivity(intent);
        });
    }
}
