package com.example.ticketapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SellNTransfer extends AppCompatActivity {

    private TabLayout tabsSellTransfer;
    private LinearLayout layoutSellSection, layoutTransferSection;
    private EditText edtSellTicketId, edtSellWalletAddress, edtSellOwnerId, edtSellPrice;
    private EditText edtBuyerEmail, edtBuyerWalletAddress;
    private EditText edtTransferTicketId, edtTransferOwnerId, edtTransferWalletAddress;
    private Button btnSellVerifyTicket, btnConfirmSellTicket, btnConfirmTransferTicket, btnVerifyTransferTicket;

    private boolean isVerified = false;
    private boolean isTransferVerified = false;
    private String verifiedTicketId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell_ntransfer);

        //initialize views
        tabsSellTransfer = findViewById(R.id.tabsSellTransfer);
        layoutSellSection = findViewById(R.id.layoutSellSection);
        layoutTransferSection = findViewById(R.id.layoutTransferSection);

        edtSellTicketId = findViewById(R.id.edtSellTicketId);
        edtSellWalletAddress = findViewById(R.id.edtSellWalletAddress);
        edtSellOwnerId = findViewById(R.id.edtSellOwnerId);
        edtSellPrice = findViewById(R.id.edtSellPrice);
        edtBuyerEmail = findViewById(R.id.edtBuyerEmail);
        edtBuyerWalletAddress = findViewById(R.id.edtBuyerWalletAddress);

        edtTransferTicketId = findViewById(R.id.edtTransferTicketId);
        edtTransferOwnerId = findViewById(R.id.edtTransferOwnerId);
        edtTransferWalletAddress = findViewById(R.id.edtTransferWalletAddress);

        btnSellVerifyTicket = findViewById(R.id.btnSellVerifyTicket);
        btnConfirmSellTicket = findViewById(R.id.btnConfirmSellTicket);
        btnConfirmTransferTicket = findViewById(R.id.btnConfirmTransferTicket);
        btnVerifyTransferTicket = findViewById(R.id.btnVerifyTransferTicket);

        //disable fields initially
        edtBuyerEmail.setEnabled(false);
        edtBuyerWalletAddress.setEnabled(false);
        btnConfirmTransferTicket.setEnabled(false);

        isVerified = getIntent().getBooleanExtra("verificationSuccess", false);

        if (isVerified) {
            Toast.makeText(this, "Ticket verified, ready to resell!", Toast.LENGTH_LONG).show();
            btnConfirmSellTicket.setEnabled(true);
        } else {
            btnConfirmSellTicket.setEnabled(false);
        }

        //tab switching
        tabsSellTransfer.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(@NonNull TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    switchTabs(layoutTransferSection, layoutSellSection);
                } else {
                    switchTabs(layoutSellSection, layoutTransferSection);
                }
            }

            @Override
            public void onTabUnselected(@NonNull TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(@NonNull TabLayout.Tab tab) {}
        });

        //button clicks
        btnSellVerifyTicket.setOnClickListener(v -> verifySellTicket());
        btnConfirmSellTicket.setOnClickListener(v -> sellTicket());
        btnVerifyTransferTicket.setOnClickListener(v -> verifyTransferTicket());
        btnConfirmTransferTicket.setOnClickListener(v -> confirmTransfer());
    }

    private void switchTabs(View hideView, View showView) {
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        hideView.startAnimation(fadeOut);
        hideView.setVisibility(View.GONE);

        showView.startAnimation(fadeIn);
        showView.setVisibility(View.VISIBLE);
    }

    private void verifySellTicket() {
        //Get all sell form values
        String ticketId = edtSellTicketId.getText().toString().trim();
        String wallet = edtSellWalletAddress.getText().toString().trim();
        String owner = edtSellOwnerId.getText().toString().trim();
        String price = edtSellPrice.getText().toString().trim();

        //form validation
        if (ticketId.isEmpty() || wallet.isEmpty() || owner.isEmpty() || price.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        //Launch Verify screen
        Intent intent = new Intent(SellNTransfer.this, Verify.class);
        intent.putExtra("ticketId", ticketId);
        intent.putExtra("walletAddress", wallet);
        intent.putExtra("ownerId", owner);
        intent.putExtra("price", price);
        startActivityForResult(intent, 123); //request code to catch return
    }


    private void sellTicket() {
        if (!isVerified) {
            Toast.makeText(this, "Please verify the ticket first!", Toast.LENGTH_SHORT).show();
            return;
        }

        String ticketId = edtSellTicketId.getText().toString().trim();
        String price = edtSellPrice.getText().toString().trim();
        String wallet = edtSellWalletAddress.getText().toString().trim();
        String owner = edtSellOwnerId.getText().toString().trim();

        if (ticketId.isEmpty() || price.isEmpty() || wallet.isEmpty() || owner.isEmpty()) {
            Toast.makeText(this, "Missing ticket details!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> resaleData = new HashMap<>();
        resaleData.put("ticketId", ticketId);
        resaleData.put("price", price);
        resaleData.put("sellerWallet", wallet);
        resaleData.put("ownerId", owner);
        resaleData.put("status", "available");

        db.collection("ResaleTickets").document(ticketId)
                .set(resaleData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Ticket Listed for Resale Successfully!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(SellNTransfer.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to list ticket for resale", Toast.LENGTH_SHORT).show();
                });
    }

    private void verifyTransferTicket() {
        String ticketId = edtTransferTicketId.getText().toString().trim();
        String ownerId = edtTransferOwnerId.getText().toString().trim();
        String walletAddress = edtTransferWalletAddress.getText().toString().trim();

        if (ticketId.isEmpty() || ownerId.isEmpty() || walletAddress.isEmpty()) {
            Toast.makeText(this, "Please fill out all verification fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("ownerID", ownerId)
                .whereEqualTo("walletAddress", walletAddress)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0);
                        if (userDoc.contains("ticketsOwned")) {
                            if (((java.util.List<String>) userDoc.get("ticketsOwned")).contains(ticketId)) {
                                Toast.makeText(this, "Ticket verified for transfer!", Toast.LENGTH_SHORT).show();
                                edtBuyerEmail.setEnabled(true);
                                edtBuyerWalletAddress.setEnabled(true);
                                btnConfirmTransferTicket.setEnabled(true);
                                isTransferVerified = true;
                                verifiedTicketId = ticketId;
                            } else {
                                Toast.makeText(this, "Ticket ID not found under your ownership!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(this, "Owner ID and Wallet mismatch!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error verifying ticket!", Toast.LENGTH_SHORT).show();
                });
    }

    private void confirmTransfer() {
        if (!isTransferVerified) {
            Toast.makeText(this, "Please verify ticket first!", Toast.LENGTH_SHORT).show();
            return;
        }

        String buyerEmail = edtBuyerEmail.getText().toString().trim();
        String buyerWallet = edtBuyerWalletAddress.getText().toString().trim();

        if (buyerEmail.isEmpty() || buyerWallet.isEmpty()) {
            Toast.makeText(this, "Please fill out buyer details!", Toast.LENGTH_SHORT).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Warning")
                .setMessage("This ticket transfer is irreversible.\nPlease ensure the buyer is trusted.\n\nProceed?")
                .setPositiveButton("Yes", (dialog, which) -> performTransfer(verifiedTicketId, buyerEmail, buyerWallet))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performTransfer(String ticketId, String buyerEmail, String buyerWallet) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("email", buyerEmail)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot buyerDoc = querySnapshot.getDocuments().get(0);
                        String buyerWalletInDb = buyerDoc.getString("walletAddress");
                        String buyerUserId = buyerDoc.getId();

                        if (buyerWalletInDb != null && buyerWalletInDb.equals(buyerWallet)) {
                            db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .update("ticketsOwned", com.google.firebase.firestore.FieldValue.arrayRemove(ticketId))
                                    .addOnSuccessListener(aVoid -> {
                                        db.collection("users").document(buyerUserId)
                                                .update("ticketsOwned", com.google.firebase.firestore.FieldValue.arrayUnion(ticketId))
                                                .addOnSuccessListener(aVoid2 -> {
                                                    Toast.makeText(this, "Ticket transferred successfully!", Toast.LENGTH_LONG).show();
                                                    startActivity(new Intent(SellNTransfer.this, MainActivity.class));
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(this, "Failed to add ticket to buyer.", Toast.LENGTH_SHORT).show();
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to remove ticket from seller.", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(this, "Buyer wallet address mismatch!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Buyer email not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error searching for buyer.", Toast.LENGTH_SHORT).show();
                });
    }
    //Data validation
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 123 && resultCode == RESULT_OK) {
            //Get data passed back
            String verifiedTicketId = data.getStringExtra("ticketId");
            String verifiedWallet = data.getStringExtra("walletAddress");
            String verifiedOwnerId = data.getStringExtra("ownerId");
            String verifiedPrice = data.getStringExtra("price");

            //Compare with current field values
            if (
                    verifiedTicketId.equals(edtSellTicketId.getText().toString().trim()) &&
                            verifiedWallet.equals(edtSellWalletAddress.getText().toString().trim()) &&
                            verifiedOwnerId.equals(edtSellOwnerId.getText().toString().trim()) &&
                            verifiedPrice.equals(edtSellPrice.getText().toString().trim())
            ) {
                isVerified = true;

                //Disable all editable fields after verification
                edtSellTicketId.setEnabled(false);
                edtSellWalletAddress.setEnabled(false);
                edtSellOwnerId.setEnabled(false);
                edtSellPrice.setEnabled(false);

                Toast.makeText(this, "Ticket verified and fields locked!", Toast.LENGTH_LONG).show();
                btnConfirmSellTicket.setEnabled(true);
            }

        } else {
                Toast.makeText(this, "Details mismatch. Verification failed!", Toast.LENGTH_LONG).show();
                btnConfirmSellTicket.setEnabled(false);
            }
        }
    }


