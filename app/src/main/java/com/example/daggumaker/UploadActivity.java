package com.example.daggumaker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class UploadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        TextView btnBack = findViewById(R.id.btn_back);
        Button btnUpload = findViewById(R.id.btn_upload_submit);

        btnBack.setOnClickListener(v -> finish());
        btnUpload.setOnClickListener(v -> {
            Intent intent = new Intent(UploadActivity.this, AnalysisActivity.class);
            startActivity(intent);
        });
    }
}
