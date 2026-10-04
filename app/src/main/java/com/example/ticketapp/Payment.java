package com.example.ticketapp;

import android.app.AlertDialog;
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
import com.google.firebase.firestore.DocumentSnapshot;
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

public class Payment extends AppCompatActivity {

    private static final String TAG = "Payment";

    private TextView txtEvent, txtAmount;
    private Button btnPayNow;
    private CardInputWidget cardWidget;
    private ProgressDialog progressDialog;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private Stripe stripe;

    private String eventName, ticketId, sellerId;
    private double price;
    private String paymentDocId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        Log.d(TAG, "Initialising Stripe configuration");
        PaymentConfiguration.init(getApplicationContext(), "pk_test_51R9ScF2YHRACdAJjDj2QrrPF2nR5vpRcRjDNi3fqnDqAGo0lofiRJcukBZ4yvt7MZSXIOJxy5wa6BqwUSDQEyjWf00QNWUszZI");
        stripe = new Stripe(getApplicationContext(), PaymentConfiguration.getInstance(getApplicationContext()).getPublishableKey());

        eventName = getIntent().getStringExtra("eventName");
        ticketId = getIntent().getStringExtra("ticketId");
        sellerId = getIntent().getStringExtra("sellerId");
        price = getIntent().getDoubleExtra("price", 0.0);

        Log.d(TAG, "Received data: event=" + eventName + ", ticketId=" + ticketId + ", sellerId=" + sellerId + ", price=" + price);

        txtEvent = findViewById(R.id.txtEventName);
        txtAmount = findViewById(R.id.txtPrice);
        btnPayNow = findViewById(R.id.btnPay);
        cardWidget = findViewById(R.id.cardInputWidget);

        txtEvent.setText(eventName);
        txtAmount.setText(String.format("£%.2f", price));

        progressDialog = new ProgressDialog(Payment.this);
        progressDialog.setMessage("Processing payment...");
        progressDialog.setCancelable(false);

        btnPayNow.setOnClickListener(v -> {
            Log.d(TAG, "Pay Now clicked!");
            startPayment();
        });
    }

    private void startPayment() {
        Log.d(TAG, "startPayment: Starting...");
        PaymentMethodCreateParams params = cardWidget.getPaymentMethodCreateParams();
        if (params == null) {
            Toast.makeText(this, "Enter valid card details", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Card input invalid");
            return;
        }

        btnPayNow.setEnabled(false);
        Log.d(TAG, "Card input valid, proceeding");

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "No user logged in");
            return;
        }

        String userId = user.getUid();
        Log.d(TAG, "UserID: " + userId);

        db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
            if (!doc.contains("walletAddress")) {
                Toast.makeText(this, "Link wallet first", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Wallet not linked");
                startActivity(new Intent(Payment.this, LinkWallet.class));
                btnPayNow.setEnabled(true);
                return;
            }

            String walletAddr = doc.getString("walletAddress");
            Log.d(TAG, "Wallet address found: " + walletAddr);

            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("buyerId", userId);
            paymentData.put("sellerId", sellerId);
            paymentData.put("eventName", eventName);
            paymentData.put("ticketId", ticketId);
            paymentData.put("amount", price);
            paymentData.put("status", "pending");
            paymentData.put("timestamp", FieldValue.serverTimestamp());

            db.collection("Payments").add(paymentData).addOnSuccessListener(ref -> {
                paymentDocId = ref.getId();
                Log.d(TAG, "Payment created with ID: " + paymentDocId);
                Bitmap qr = generateQR(paymentDocId);
                if (qr != null) {
                    Log.d(TAG, "QR generated");
                    uploadQR(qr, eventName, ticketId, userId, paymentDocId, walletAddr);
                } else {
                    Log.e(TAG, "QR generation failed");
                    Toast.makeText(this, "QR generation failed", Toast.LENGTH_SHORT).show();
                    btnPayNow.setEnabled(true);
                }
            });
        });
    }

    private Bitmap generateQR(String text) {
        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            return encoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 500, 500);
        } catch (Exception e) {
            Log.e(TAG, "QR generation exception: ", e);
            return null;
        }
    }

    private void uploadQR(Bitmap bitmap, String event, String ticket, String user, String payId, String wallet) {
        Log.d(TAG, "Uploading QR...");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference ref = storage.getReference("qrcodes/" + ticket + ".png");
        ref.putBytes(data).addOnSuccessListener(task -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
            Log.d(TAG, "QR uploaded! Updating Firestore...");
            db.collection("Events").document(event).collection("Tickets").document(ticket)
                    .update("qrCodeUrl", uri.toString());

            simulateEscrow(wallet, uri.toString());
        }));
    }

    private void simulateEscrow(String wallet, String uri) {
        Log.d(TAG, "Escrow engaged...");
        progressDialog.setMessage("Verifying payment...");
        progressDialog.show();

        new android.os.Handler().postDelayed(() -> {
            progressDialog.setMessage("Minting NFT...");
            mintNFT(wallet, uri);
        }, 5000);
    }

    private void mintNFT(String wallet, String uri) {
        Log.d(TAG, "Starting mint task...");
        new MintTicketTask(wallet, uri, result -> {
            Log.d(TAG, "Minting result: " + result);
            progressDialog.dismiss();

            if (result.contains("Ticket Minted!") || result.contains("0x")) {
                String tx = result.contains("0x") ? result.substring(result.indexOf("0x")) : "unknown";
                Log.d(TAG, "Transaction hash: " + tx);
                storeTx(tx);
                updateTicketOwnership(tx);

                Intent intent = new Intent(Payment.this, Confirmation.class);
                intent.putExtra("ticketId", ticketId);
                intent.putExtra("txHash", tx);
                startActivity(intent);
                finish();
            } else {
                Log.e(TAG, "Mint failed");
                showMintError();
            }


            btnPayNow.setEnabled(true);
        }).execute();
    }

    private void storeTx(String tx) {
        if (paymentDocId == null) {
            Log.w(TAG, "Cannot store txHash!");
            return;
        }

        Log.d(TAG, "Storing transaction hash!");
        db.collection("Payments").document(paymentDocId)
                .update("status", "completed", "txHash", tx);
    }

    private void updateTicketOwnership(String tx) {
        Log.d(TAG, "Updating ticket ownership!");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("Events").document(eventName)
                .collection("Tickets").document(ticketId)
                .update("status", "sold");

        db.collection("users").document(userId)
                .update("ticketsOwned", FieldValue.arrayUnion(ticketId));

        db.collection("Events").document(eventName)
                .update("ticketsSold", FieldValue.increment(1));
    }

    private void showMintError() {
        Log.d(TAG, "Showing mint error dialog...");
        new AlertDialog.Builder(Payment.this)
                .setTitle("Mint Failed")
                .setMessage("Minting failed, please try again later!")
                .setPositiveButton("OK", null)
                .show();
    }
}
