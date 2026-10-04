package com.example.ticketapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.Stripe;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.view.CardInputWidget;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class Payment2 extends AppCompatActivity {

    private TextView txtTicketId, txtTicketPrice;
    private Button btnPurchase;
    private ProgressBar progressPurchase;
    private CardInputWidget cardInputWidget;

    private String ticketId, sellerWallet, resalePrice;
    private Stripe stripe;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage storage;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment2);

        //Get intent extras
        ticketId = getIntent().getStringExtra("ticketId");
        resalePrice = getIntent().getStringExtra("price");
        sellerWallet = getIntent().getStringExtra("sellerWallet");


        txtTicketId = findViewById(R.id.txtTicketId2);
        txtTicketPrice = findViewById(R.id.txtPrice2);
        btnPurchase = findViewById(R.id.btnPay2);
        progressPurchase = findViewById(R.id.progressBar2);
        cardInputWidget = findViewById(R.id.cardInputWidget2);

        txtTicketId.setText("Ticket ID: " + ticketId);
        txtTicketPrice.setText("Price: £" + resalePrice);

        //Setup services
        PaymentConfiguration.init(getApplicationContext(), "pk_test_51R9ScF2YHRACdAJjDj2QrrPF2nR5vpRcRjDNi3fqnDqAGo0lofiRJcukBZ4yvt7MZSXIOJxy5wa6BqwUSDQEyjWf00QNWUszZI");
        stripe = new Stripe(getApplicationContext(), PaymentConfiguration.getInstance(getApplicationContext()).getPublishableKey());

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Processing resale payment...");
        progressDialog.setCancelable(false);

        btnPurchase.setOnClickListener(v -> processPayment());
    }

    //process the resale payment
    private void processPayment() {
        PaymentMethodCreateParams params = cardInputWidget.getPaymentMethodCreateParams();
        if (params == null) {
            Toast.makeText(this, "Enter valid card details", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        progressPurchase.setVisibility(View.VISIBLE);
        btnPurchase.setEnabled(false);

        String buyerId = user.getUid();

        firestore.collection("users").document(buyerId).get().addOnSuccessListener(document -> {
            if (!document.contains("walletAddress")) {
                Toast.makeText(this, "Please link your wallet first", Toast.LENGTH_LONG).show();
                startActivity(new Intent(Payment2.this, LinkWallet.class));
                finish();
                return;
            }

            String buyerWallet = document.getString("walletAddress");
            performResalePurchase(ticketId, buyerWallet);

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error fetching user", Toast.LENGTH_SHORT).show();
            progressPurchase.setVisibility(View.GONE);
            btnPurchase.setEnabled(true);
        });
    }

    //simulate the resale purchase
    private void performResalePurchase(String ticketId, String buyerWallet) {
        progressDialog.show();

        Bitmap qrBitmap = generateQrCode(ticketId);
        if (qrBitmap == null) {
            Toast.makeText(this, "QR code generation failed", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            progressPurchase.setVisibility(View.GONE);
            btnPurchase.setEnabled(true);
            return;
        }

        uploadQrCode(ticketId, qrBitmap, buyerWallet);
    }

    //generate QR code
    private Bitmap generateQrCode(String ticketId) {
        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            return encoder.encodeBitmap(ticketId, BarcodeFormat.QR_CODE, 500, 500);
        } catch (Exception e) {
            Log.e("Payment2", "QR code generation error", e);
            return null;
        }
    }

    //upload QR to Firebase Storage
    private void uploadQrCode(String ticketId, Bitmap bitmap, String buyerWallet) {
        StorageReference qrRef = storage.getReference().child("resaleqrcodes/" + ticketId + ".png");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageData = baos.toByteArray();

        qrRef.putBytes(imageData)
                .addOnSuccessListener(task -> qrRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    updateTicketOwnership(ticketId, buyerWallet, uri.toString());
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to upload QR", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    progressPurchase.setVisibility(View.GONE);
                    btnPurchase.setEnabled(true);
                });
    }

    //update ticket ownership after purchase
    private void updateTicketOwnership(String ticketId, String buyerWallet, String qrUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("buyerWallet", buyerWallet);
        updates.put("status", "sold");
        updates.put("qrCodeUrl", qrUrl);

        firestore.collection("ResaleTickets").document(ticketId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Ticket purchased successfully!", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                    startActivity(new Intent(Payment2.this, Settings.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update resale ticket", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    progressPurchase.setVisibility(View.GONE);
                    btnPurchase.setEnabled(true);
                });
    }
}
