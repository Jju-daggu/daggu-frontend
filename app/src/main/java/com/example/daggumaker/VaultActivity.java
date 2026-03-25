package com.example.daggumaker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class VaultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vault);

        TextView btnBack = findViewById(R.id.btn_back);
        TextView btnCalendar = findViewById(R.id.tv_view_calendar);

        btnBack.setOnClickListener(v -> finish());
        btnCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(VaultActivity.this, CalendarActivity.class);
            startActivity(intent);
        });
    }
}
