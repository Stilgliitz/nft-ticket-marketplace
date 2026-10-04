package com.example.ticketapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Account extends AppCompatActivity {

    private ImageView imgProfile;
    private EditText edtName, edtEmail, edtPhone;
    private Button btnSave, btnChangePic;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseUser user;
    private Uri selectedImgUri;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        imgProfile = findViewById(R.id.imgProfile);
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        btnSave = findViewById(R.id.btnSave);
        btnChangePic = findViewById(R.id.btnChangePic);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference("profile_pics");

        loadUser();

        btnChangePic.setOnClickListener(v -> openGallery());
        btnSave.setOnClickListener(v -> saveUser());
    }

    //load user info
    private void loadUser() {
        if (user != null) {
            String userId = user.getUid();
            DocumentReference docRef = firestore.collection("users").document(userId);

            docRef.get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    edtName.setText(snapshot.getString("name"));
                    edtEmail.setText(user.getEmail());
                    edtPhone.setText(snapshot.getString("phone"));

                    String profileUrl = snapshot.getString("profileUrl");
                    if (profileUrl != null && !profileUrl.isEmpty()) {
                        Glide.with(this).load(profileUrl).into(imgProfile);
                    }
                } else {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to load user", Toast.LENGTH_SHORT).show();
            });
        }
    }

    //pick profile pic
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImage.launch(intent);
    }

    private ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImgUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImgUri);
                        imgProfile.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    //save user changes
    private void saveUser() {
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();

        if (user != null) {
            String userId = user.getUid();
            DocumentReference docRef = firestore.collection("users").document(userId);

            docRef.update("name", name, "phone", phone)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    });

            if (!email.equals(user.getEmail())) {
                user.updateEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Email updated", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Failed email update", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            if (selectedImgUri != null) {
                StorageReference fileRef = storageRef.child(userId + ".jpg");
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImgUri);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] imgData = baos.toByteArray();

                    UploadTask uploadTask = fileRef.putBytes(imgData);
                    uploadTask.addOnSuccessListener(taskSnapshot -> {
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            docRef.update("profileUrl", uri.toString());
                        });
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });
                } catch (IOException e) {
                    Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
