package com.example.ticketapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class Verify extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private String ticketId, walletAddress, ownerId, price;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        db = FirebaseFirestore.getInstance();

        //Get ticket details passed from SellNTransfer
        ticketId = getIntent().getStringExtra("ticketId");
        walletAddress = getIntent().getStringExtra("walletAddress");
        ownerId = getIntent().getStringExtra("ownerId");
        price = getIntent().getStringExtra("price");

        Log.d("Verify", "Received Data - Ticket ID: " + ticketId + ", Wallet: " + walletAddress + ", OwnerID: " + ownerId + ", Price: " + price);

        startBlockchainVerification();
    }

    private void startBlockchainVerification() {
        progressDialog = new ProgressDialog(Verify.this);
        progressDialog.setMessage("Verifying ticket on blockchain...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        //delay of 3 seconds
        new Handler().postDelayed(this::checkUserOwnership, 3000);
    }

    private void checkUserOwnership() {
        progressDialog.setMessage("Checking ticket ownership...");

        //Query the "users" collection where ownerID matches
        db.collection("users")
                .whereEqualTo("ownerID", ownerId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0);

                        List<String> ticketsOwned = (List<String>)userDoc.get("ticketsOwned");
                        if (ticketsOwned != null && ticketsOwned.contains(ticketId)) {
                            verificationSuccess();
                        } else {
                            verificationFailed();
                        }
                    } else {
                        verificationFailed();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Verify", "Error checking users collection", e);
                    verificationFailed();
                });
    }

    private void verificationSuccess() {
        progressDialog.dismiss();
        Toast.makeText(Verify.this, "Ticket Verified Successfully!", Toast.LENGTH_LONG).show();

        Intent resultIntent = new Intent();
        resultIntent.putExtra("ticketId", getIntent().getStringExtra("ticketId"));
        resultIntent.putExtra("walletAddress", getIntent().getStringExtra("walletAddress"));
        resultIntent.putExtra("ownerId", getIntent().getStringExtra("ownerId"));
        resultIntent.putExtra("price", getIntent().getStringExtra("price"));
        setResult(RESULT_OK, resultIntent);
        finish();

    }

    private void verificationFailed() {
        progressDialog.dismiss();

        //Show failure popup
        new AlertDialog.Builder(Verify.this)
                .setTitle("Verification Failed")
                .setMessage("Ticket details could not be verified.\nPlease check and try again.")
                .setPositiveButton("OK", (dialog, which) -> {
                    finish(); //Go back to SellNTransfer
                })
                .show();
    }
}
