package com.example.ticketapp;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

public class Confirmation extends AppCompatActivity {

    private TextView txtConfirm, txtEvent, txtAmount, txtSeller;
    private Button btnDone;
    private ImageView imgQr;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        db = FirebaseFirestore.getInstance();

        //init views
        txtConfirm = findViewById(R.id.txtConfirm);
        txtEvent = findViewById(R.id.txtEvent);
        txtAmount = findViewById(R.id.txtAmount);
        txtSeller = findViewById(R.id.txtSeller);
        btnDone = findViewById(R.id.btnDone);
        imgQr = findViewById(R.id.imgQr);

        String ticketId = getIntent().getStringExtra("ticketId");

        //load payment data
        loadPayment(ticketId);

        btnDone.setOnClickListener(v -> finish());
    }

    private void loadPayment(String ticketId) {
        db.collection("Payments").whereEqualTo("ticketId", ticketId).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        DocumentSnapshot doc = snapshot.getDocuments().get(0);

                        String eventName = doc.getString("eventName");
                        String sellerId = doc.getString("sellerId");
                        Double price = doc.getDouble("amount");

                        txtConfirm.setText("Payment confirmed for Ticket #" + ticketId);
                        txtAmount.setText(String.format("£%.2f", price != null ? price : 0.0));

                        //load event name
                        db.collection("Events").whereEqualTo("EventName", eventName).get()
                                .addOnSuccessListener(eventSnap -> {
                                    if (!eventSnap.isEmpty()) {
                                        txtEvent.setText(eventSnap.getDocuments().get(0).getString("EventName"));
                                    }
                                });

                        //load seller name
                        db.collection("users").document(sellerId).get()
                                .addOnSuccessListener(userSnap -> {
                                    if (userSnap.exists()) {
                                        txtSeller.setText("Seller: " + userSnap.getString("name"));
                                    }
                                });

                        //generate QR code
                        generateQr(ticketId);
                    }
                });
    }

    private void generateQr(String ticketId) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            int size = 512;
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
            com.google.zxing.common.BitMatrix matrix = writer.encode(ticketId, BarcodeFormat.QR_CODE, size, size);

            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            imgQr.setImageBitmap(bitmap);

        } catch (WriterException e) {
            Toast.makeText(this, "QR generation failed", Toast.LENGTH_SHORT).show();
        }
    }
}
