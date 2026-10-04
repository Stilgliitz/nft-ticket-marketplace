package com.example.ticketapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    private EditText edtLoginEmail, edtLoginPassword;
    private Button btnLogin;
    private TextView txtRegisterPage;
    private FirebaseAuth authFirebase;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authFirebase = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = authFirebase.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            startActivity(new Intent(Login.this, MainActivity.class));
            finish();
        }

        edtLoginEmail = findViewById(R.id.edtLoginEmail);
        edtLoginPassword = findViewById(R.id.edtLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtRegisterPage = findViewById(R.id.txtRegisterPage);

        btnLogin.setOnClickListener(v -> loginUser());
        txtRegisterPage.setOnClickListener(v -> startActivity(new Intent(Login.this, Register.class)));
    }

    private void loginUser() {
        //get email and password
        String email = edtLoginEmail.getText().toString().trim();
        String password = edtLoginPassword.getText().toString().trim();

        //check empty fields
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        //sign in user
        authFirebase.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = authFirebase.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            //fetch ownerID
                            db.collection("users").document(user.getUid())
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            String ownerID = documentSnapshot.getString("ownerID");
                                            if (ownerID != null) {
                                                Toast.makeText(Login.this, "Welcome " + user.getEmail() + "\nOwnerID: " + ownerID, Toast.LENGTH_LONG).show();
                                            }
                                            checkOwnerIdReveal(); //call custom dialog
                                            startActivity(new Intent(Login.this, MainActivity.class));
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(Login.this, "Login successful but failed to fetch Owner ID", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(Login.this, MainActivity.class));
                                        finish();
                                    });
                        } else {
                            Toast.makeText(this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                            authFirebase.signOut();
                        }
                    } else {
                        Toast.makeText(this, "Username Or Password Is incorrect, Please Try Again. " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    //Custom dialog to reveal ownerID securely
    private void checkOwnerIdReveal() {
        SharedPreferences prefs = getSharedPreferences("TicketAppPrefs", MODE_PRIVATE);
        boolean hasRevealed = prefs.getBoolean("ownerIdShown", false);

        if (!hasRevealed) {
            FirebaseUser currentUser = authFirebase.getCurrentUser();
            if (currentUser != null) {
                db.collection("users").document(currentUser.getUid()).get()
                        .addOnSuccessListener(doc -> {
                            String ownerId = doc.getString("ownerID");
                            if (ownerId != null && !ownerId.isEmpty()) {
                                showOwnerIdDialog(ownerId);
                            }
                        });
            }
        }
    }

    private void showOwnerIdDialog(String ownerIdVal) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_ownerid, null);

        TextView txtOwnerIdTitle = view.findViewById(R.id.txtOwnerIdTitle);
        TextView txtOwnerIdMessage = view.findViewById(R.id.txtOwnerIdMessage);
        TextView txtActualOwnerId = view.findViewById(R.id.txtActualOwnerId);
        Button btnGotIt = view.findViewById(R.id.btnGotIt);

        //hide the actual OwnerID initially
        txtActualOwnerId.setText("**********");

        //handle reveal button logic
        btnGotIt.setOnClickListener(v -> {
            txtActualOwnerId.setText(ownerIdVal);
            btnGotIt.setEnabled(false);
            txtOwnerIdMessage.setText("Please write this down. You’ll need it later!");

            getSharedPreferences("TicketAppPrefs", MODE_PRIVATE)
                    .edit().putBoolean("ownerIdShown", true).apply();
        });

        builder.setView(view);
        builder.setTitle("Your OwnerID (Only Shown Once)");
        builder.setCancelable(false);
        builder.setPositiveButton("Got it", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
