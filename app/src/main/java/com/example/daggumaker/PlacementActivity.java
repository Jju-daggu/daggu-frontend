package com.example.daggumaker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PlacementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placement);

        TextView btnBack = findViewById(R.id.btn_back);
        TextView btnSave = findViewById(R.id.btn_save);

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> {
            Intent intent = new Intent(PlacementActivity.this, PrintActivity.class);
            startActivity(intent);
        });
    }
}
