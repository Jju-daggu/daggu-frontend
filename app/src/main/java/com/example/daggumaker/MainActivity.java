package com.example.daggumaker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // XML의 LinearLayout ID인 btn_start를 찾습니다.
        View startButton = findViewById(R.id.btn_start);
        if (startButton != null) {
            startButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                startActivity(intent);
            });
        }
    }
}