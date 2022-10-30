package com.anurag.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button crop,disease;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        crop = findViewById(R.id.crop);
        disease = findViewById(R.id.disease);
        crop.setOnClickListener(this);
        disease.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent i;
        switch (view.getId()) {
            case R.id.crop:
                i = new Intent(MainActivity.this, CropRecommendation.class);
                startActivity(i);
                break;
            case R.id.disease:
                i = new Intent(MainActivity.this, DiseaseDetection.class);
                startActivity(i);
                break;
        }

    }
}