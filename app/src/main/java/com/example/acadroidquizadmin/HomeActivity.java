package com.example.acadroidquizadmin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

public class HomeActivity extends AppCompatActivity {

    RelativeLayout categoryRelative, speedMathRelative;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        categoryRelative = findViewById(R.id.categoryRelative);
        categoryRelative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent categories = new Intent(HomeActivity.this, CategoryActivity.class);
                startActivity(categories);
            }
        });

        speedMathRelative = findViewById(R.id.categorySpeedMath);
        speedMathRelative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent setsActivity = new Intent(HomeActivity.this, SpeedmathActivity.class);
                startActivity(setsActivity);
            }
        });

    }
}