package com.example.ticketapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    private EditText edtRegisterName, edtRegisterEmail, edtRegisterPassword;
    private Button btnRegister;
    private TextView txtLoginPage;
    private FirebaseAuth authFirebase;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authFirebase = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        edtRegisterName = findViewById(R.id.edtRegisterName);
        edtRegisterEmail = findViewById(R.id.edtRegisterEmail);
        edtRegisterPassword = findViewById(R.id.edtRegisterPassword);
        btnRegister = findViewById(R.id.btnRegister);
        txtLoginPage = findViewById(R.id.txtLoginPage);

        btnRegister.setOnClickListener(v -> registerUser());
        txtLoginPage.setOnClickListener(v -> startActivity(new Intent(Register.this, Login.class)));
    }

    private void registerUser() {
        String name = edtRegisterName.getText().toString().trim();
        String email = edtRegisterEmail.getText().toString().trim();
        String password = edtRegisterPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        authFirebase.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = authFirebase.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification()
                                    .addOnCompleteListener(verifyTask -> {
                                        if (verifyTask.isSuccessful()) {
                                            saveUserData(user, name, email);
                                            Toast.makeText(this, "Registered! Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                                            authFirebase.signOut();
                                            startActivity(new Intent(Register.this, Login.class));
                                            finish();
                                        } else {
                                            Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserData(FirebaseUser user, String name, String email) {
        String userId = user.getUid();
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("ownerID", "Owner" + (userId.hashCode() % 1000));
        userData.put("ticketsOwned", new ArrayList<String>());

        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
