package com.example.ticketapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class TermsAndConditions extends AppCompatActivity {

    private ImageView imgLogo1, imgLogo2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions);

        imgLogo1 = findViewById(R.id.imgLogo1);
        imgLogo2 = findViewById(R.id.imgLogo2);

        //Show the terms and conditions
        showTermsDialog();
    }

    private void showTermsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Terms and Conditions")
                .setMessage("By using this app, you agree to adhere to our security terms. Ensure that your ticket information is protected and do not share it with anyone.")
                .setCancelable(false) //Prevent cancellation
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //User accepts the terms, move to Main
                        Intent intent = new Intent(TermsAndConditions.this, MainActivity.class);
                        startActivity(intent);
                        finish(); //Close the activity
                    }
                })
                .setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //User declines terms, show a message and close app
                        new AlertDialog.Builder(TermsAndConditions.this)
                                .setTitle("Access Denied")
                                .setMessage("You cannot proceed without accepting the terms and conditions!")
                                .setPositiveButton("I Understand :(", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();  //Close the app
                                    }
                                })
                                .setCancelable(false)  //Prevent cancellation
                                .show();
                    }
                })
                .show();
    }
}
