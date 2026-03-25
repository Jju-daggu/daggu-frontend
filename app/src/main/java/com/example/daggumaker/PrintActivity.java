package com.example.daggumaker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PrintActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);

        TextView btnBack = findViewById(R.id.btn_back);
        TextView btnMain = findViewById(R.id.btn_main);
        Button btnPrint = findViewById(R.id.btn_print_trigger);

        btnBack.setOnClickListener(v -> finish());
        btnMain.setOnClickListener(v -> {
            Intent intent = new Intent(PrintActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        btnPrint.setOnClickListener(v -> {
            Toast.makeText(this, "프린트/저장이 완료되었습니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(PrintActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }
}
