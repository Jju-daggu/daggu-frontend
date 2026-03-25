package com.example.daggumaker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class StickerPreviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_preview);

        TextView btnBack = findViewById(R.id.btn_back);
        TextView btnMain = findViewById(R.id.btn_main);
        Button btnStore = findViewById(R.id.btn_store);
        Button btnPlace = findViewById(R.id.btn_place);

        btnBack.setOnClickListener(v -> finish());
        btnMain.setOnClickListener(v -> {
            Intent intent = new Intent(StickerPreviewActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        btnStore.setOnClickListener(v -> {
            Intent intent = new Intent(StickerPreviewActivity.this, VaultActivity.class);
            startActivity(intent);
        });

        btnPlace.setOnClickListener(v -> {
            Intent intent = new Intent(StickerPreviewActivity.this, PlacementActivity.class);
            startActivity(intent);
        });
    }
}
