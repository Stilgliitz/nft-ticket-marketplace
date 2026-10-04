package com.example.ticketapp;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class ViewTicket extends AppCompatActivity {

    private ImageView imgQrCode;
    private TextView txtTicketEvent, txtTicketDate, txtTicketLocation, txtTicketPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_ticket); //Load the ticket viewing layout

        //Initialise the views
        imgQrCode = findViewById(R.id.imgQrCode);
        txtTicketEvent = findViewById(R.id.txtTicketEvent);
        txtTicketDate = findViewById(R.id.txtTicketDate);
        txtTicketLocation = findViewById(R.id.txtTicketLocation);
        txtTicketPrice = findViewById(R.id.txtTicketPrice);

        //Receive ticket data from the previous activity
        String ticketId = getIntent().getStringExtra("ticketId");
        String eventName = getIntent().getStringExtra("eventName");
        String date = getIntent().getStringExtra("date");
        String location = getIntent().getStringExtra("location");
        String price = getIntent().getStringExtra("price");
        String qrCodeUrl = getIntent().getStringExtra("qrCodeUrl");

        //Check if any data is missing
        if (ticketId == null || eventName == null || date == null || location == null || price == null || qrCodeUrl == null) {
            Toast.makeText(this, "Missing ticket information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //Set the data to the TextViews
        txtTicketEvent.setText(eventName);
        txtTicketDate.setText("Date: " + date);
        txtTicketLocation.setText("Location: " + location);
        txtTicketPrice.setText("Price: £" + price);

        //Load the QR code into the ImageView using Glide
        Glide.with(this).load(qrCodeUrl).into(imgQrCode);
    }
}
