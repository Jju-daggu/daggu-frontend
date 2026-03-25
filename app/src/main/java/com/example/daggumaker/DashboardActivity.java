package com.example.daggumaker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        TextView tvCalendar = findViewById(R.id.tv_view_calendar);
        Button btnUpload = findViewById(R.id.btn_upload);
        Button btnVault = findViewById(R.id.btn_vault);

        tvCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, CalendarActivity.class);
            startActivity(intent);
        });

        btnUpload.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, UploadActivity.class);
            startActivity(intent);
        });

        btnVault.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, VaultActivity.class);
            startActivity(intent);
        });
    }
}
