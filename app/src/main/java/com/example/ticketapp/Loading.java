package com.example.ticketapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class Loading extends AppCompatActivity {

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        progressBar = findViewById(R.id.progressBar);

        //Delay the transition for 3 seconds
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(Loading.this, TermsAndConditions.class);
            startActivity(intent);
            finish();  // Close the activity
        }, 3000); //3 seconds

    }
}
