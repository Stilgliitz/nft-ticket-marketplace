package com.example.ticketapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class LinkWallet extends AppCompatActivity {

    private EditText edtWalletAddress;
    private TextView txtWalletStatus;
    private Button btnSaveWallet;
    private Switch switchExistingWallet;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ProgressBar progressWalletLink;


    //Etherscan API key
    private static final String EtherScan_API = "FBQI1QYBH4HFYKZIUYZG16YIBQH8GMQITT";

    //Etherscan API base URL
    private static final String API_URL = "https://api.etherscan.io/api";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link_wallet);

        //Init views
        edtWalletAddress = findViewById(R.id.edtWalletAddress);
        txtWalletStatus = findViewById(R.id.txtWalletStatus);
        btnSaveWallet = findViewById(R.id.btnSaveWallet);
        switchExistingWallet = findViewById(R.id.switchExistingWallet);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        progressWalletLink = findViewById(R.id.progressWalletLink);


        //Check existing wallet status
        checkWalletStatus();

        //Click listener
        btnSaveWallet.setOnClickListener(v -> validateAndCheckWallet());
    }

    //Validate wallet format and decide flow
    private void validateAndCheckWallet() {
        String walletAddress = edtWalletAddress.getText().toString().trim();
        boolean isExistingWallet = switchExistingWallet.isChecked(); //true = existing wallet

        //Basic format validation
        if (!isValidWalletAddress(walletAddress)) {
            Toast.makeText(this, "Invalid wallet format. Please check.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isExistingWallet) {
            //Existing wallet to check with Etherscan
            new CheckWalletTask(walletAddress).execute();
        } else {
            //New wallet, skip check
            saveWalletAddress(walletAddress);
        }
    }

    //Check if wallet address matches basic Ethereum format
    private boolean isValidWalletAddress(String address) {
        if (address == null) return false;
        if (!address.startsWith("0x")) return false;
        if (address.length() != 42) return false;
        String hexPart = address.substring(2);
        return hexPart.matches("[0-9a-fA-F]+");
    }

    //AsyncTask to call Etherscan API
    private class CheckWalletTask extends AsyncTask<Void, Void, Boolean> {
        private String walletAddress;

        public CheckWalletTask(String walletAddress) {
            this.walletAddress = walletAddress;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                String urlStr = API_URL + "?module=account&action=balance&address=" + walletAddress + "&tag=latest&apikey=" + EtherScan_API;
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                String status = jsonResponse.getString("status");

                return "1".equals(status);
            } catch (Exception e) {
                Log.e("WalletCheck", "Error checking wallet balance", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean isValidWallet) {
            if (isValidWallet) {
                saveWalletAddress(walletAddress);
            } else {
                Toast.makeText(LinkWallet.this, "Wallet address not found on Ethereum network.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Save wallet address to Firestore
    private void saveWalletAddress(String walletAddress) {
        String uid = auth.getCurrentUser().getUid();

        Map<String, Object> walletData = new HashMap<>();
        walletData.put("walletAddress", walletAddress);
        walletData.put("walletStatus", "pending");
        walletData.put("linkedAt", System.currentTimeMillis());

        db.collection("users").document(uid)
                .update(walletData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Wallet linked! Activation pending...", Toast.LENGTH_LONG).show();
                    txtWalletStatus.setText("Pending Activation...");

                    progressWalletLink.setVisibility(View.VISIBLE);

                    //Delay and then update Firestore + UI
                    new Handler().postDelayed(() -> {
                        db.collection("users").document(uid)
                                .update("walletStatus", "linked")
                                .addOnSuccessListener(unused -> {
                                    txtWalletStatus.setText("Wallet Status: Linked");
                                    progressWalletLink.setVisibility(View.GONE);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("LinkWallet", "Failed to update wallet status", e);
                                });
                    }, 30000); //30 seconds delay
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to link wallet.", Toast.LENGTH_SHORT).show();
                    Log.e("LinkWallet", "Error saving wallet", e);
                });
    }

    //Load wallet status if user already linked before
    private void checkWalletStatus() {
        String uid = auth.getCurrentUser().getUid();
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String walletStatus = documentSnapshot.getString("walletStatus");
                        if (walletStatus != null) {
                            if (walletStatus.equals("linked")) {
                                txtWalletStatus.setText("Wallet Status: Linked");
                            } else {
                                txtWalletStatus.setText("Pending Activation");
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("LinkWallet", "Failed to load wallet status! Try Again.", e);
                });
    }
}
