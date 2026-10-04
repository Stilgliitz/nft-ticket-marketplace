package com.example.ticketapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements EventAdapter.clicksBuy {

    private static final String TAG = "MainActivity";
    private TextView txtWelcome, txtMintStatus;
    private Button btnLogout, btnBrowseTickets, btnMyTickets, btnMintTicket;
    private RecyclerView recyclerEvents;
    private FirebaseAuth authFirebase;
    private FirebaseFirestore db;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private boolean showingMyTickets = false;
    private EditText edtWalletAddress;
    private ImageView imgUserProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authFirebase = FirebaseAuth.getInstance();  //Make sure user is logged in before anything
        FirebaseUser currentUser = authFirebase.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Please log in to access the app", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, Login.class));
            finish(); //Prevent access to this activity
            return;
        }

        setContentView(R.layout.activity_main);

        authFirebase = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        txtWelcome = findViewById(R.id.txtWelcome);
        btnLogout = findViewById(R.id.btnLogout);
        btnBrowseTickets = findViewById(R.id.btnBrowseTickets);
        btnMyTickets = findViewById(R.id.btnMyTickets);
        recyclerEvents = findViewById(R.id.recyclerEvents);
        imgUserProfile = findViewById(R.id.imgUserProfile);
        btnMintTicket = findViewById(R.id.btnMintTicket);
        edtWalletAddress = findViewById(R.id.edtWalletAddress);
        txtMintStatus = findViewById(R.id.txtMintStatus);

        Spinner spinnerLocation = findViewById(R.id.spinnerLocation);
        Spinner spinnerPrice = findViewById(R.id.spinnerPrice);

        AdapterView.OnItemSelectedListener Filter = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        spinnerLocation.setOnItemSelectedListener(Filter);
        spinnerPrice.setOnItemSelectedListener(Filter);


        setupRecycler();
        loadEvents(false);
        updateLogin();
        handleLogic();
    }

    private void setupRecycler() {
        recyclerEvents.setLayoutManager(new LinearLayoutManager(this));
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList, this, this);
        recyclerEvents.setAdapter(eventAdapter);
    }

    private void handleLogic() {
        btnLogout.setOnClickListener(v -> handleLogout());
        btnBrowseTickets.setOnClickListener(v -> toggleEvents(false));
        btnMyTickets.setOnClickListener(v -> toggleEvents(true));
        imgUserProfile.setOnClickListener(v -> startActivity(new Intent(this, Settings.class)));

        btnMintTicket.setOnClickListener(v -> {
            String walletAddress = edtWalletAddress.getText().toString().trim();
            if (walletAddress.isEmpty()) {
                Toast.makeText(MainActivity.this, "Enter a wallet address", Toast.LENGTH_SHORT).show();
                return;
            }

            BigInteger ticketId = new BigInteger("1"); //temp test ticket ID for testing
            String dummyTokenUri = "https://example.com/metadata/" + ticketId;

            txtMintStatus.setVisibility(View.VISIBLE);
            txtMintStatus.setText("Minting ticket... please wait");

            //callback function
            MintTicketTask.MintCallback mintCallback = new MintTicketTask.MintCallback() {
                @Override
                public void onMintResult(String result) {
                    txtMintStatus.setText("Success! " + result);
                    Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
                    Log.d("MintCallback", "🧾 " + result);
                }
            };

            //mint ticket function
            new MintTicketTask(walletAddress, dummyTokenUri, mintCallback).execute();
        });
        //Check and prompt OwnerID reveal on first login
        checkOwnerIdReveal();

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
            txtActualOwnerId.setText(ownerIdVal);  //display actual OwnerID
            btnGotIt.setEnabled(false);  //disable the button after reveal
            txtOwnerIdMessage.setText("Please write this down. You’ll need it later!");  // update message

            getSharedPreferences("TicketAppPrefs", MODE_PRIVATE)
                    .edit().putBoolean("ownerIdShown", true).apply();
        });


        builder.setView(view);
        builder.setTitle("Your OwnerID (Only Shown Once)");
        builder.setCancelable(false);
        builder.setPositiveButton("Got it", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void handleLogout() {
        if (authFirebase.getCurrentUser() == null) {
            startActivity(new Intent(this, Login.class));
        } else {
            authFirebase.signOut();
            Toast.makeText(this, "User Logged Out", Toast.LENGTH_SHORT).show();
            updateLogin();
            loadEvents(false);
        }
    }

    private void toggleEvents(boolean showMyTickets) {
        showingMyTickets = showMyTickets;
        loadEvents(showMyTickets);
    }

    private void updateLogin() {
        FirebaseUser user = authFirebase.getCurrentUser();
        btnLogout.setText(user == null ? "Login" : "Logout");

        if (user != null) {
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String name = documentSnapshot.getString("name");
                        txtWelcome.setText("Welcome, " + (name != null ? name : "User") + "!");
                    })
                    .addOnFailureListener(e -> txtWelcome.setText("Welcome to TicketApp"));
        } else {
            txtWelcome.setText("Welcome to TicketApp");
        }
    }

    private void loadEvents(boolean filterByUser) {
        eventList.clear();  // Clear the existing list before adding new items
        FirebaseUser user = authFirebase.getCurrentUser();
        String currentUserId = user != null ? user.getUid() : null;

        db.collection("Events").get().addOnSuccessListener(eventSnapshots -> {
            for (QueryDocumentSnapshot eventDoc : eventSnapshots) {
                String eventName = eventDoc.getString("EventName");

                Log.d(TAG, "🔍 Fetching tickets for event: " + eventName);

                db.collection("Events").document(eventDoc.getId())
                        .collection("Tickets")
                        .get()
                        .addOnSuccessListener(ticketSnapshots -> {
                            for (QueryDocumentSnapshot ticketDoc : ticketSnapshots) {
                                if (!ticketDoc.exists()) continue;

                                String ticketId = ticketDoc.getId(); // Get the actual ticketId from Firestore
                                String sellerId = ticketDoc.getString("sellerId");
                                Log.d(TAG, "👤 Ticket sellerId: " + sellerId);

                                // Get the number of tickets sold and the maximum tickets per event
                                int ticketsSold = eventDoc.getLong("ticketsSold") != null ? eventDoc.getLong("ticketsSold").intValue() : 0;
                                int maxTickets = eventDoc.getLong("maxTicketsPerEvent") != null ? eventDoc.getLong("maxTicketsPerEvent").intValue() : 256; // default to 256 if not available
                                int remainingTickets = maxTickets - ticketsSold;  // Calculate remaining tickets
                                String status = (remainingTickets <= 0) ? "Sold Out" : ticketDoc.getString("status");

                                // Filter by user if needed
                                if (filterByUser && currentUserId != null) {
                                    isTicketOwnedByUser(currentUserId, ticketDoc, isOwned -> {
                                        if (!isOwned) {
                                            Log.d(TAG, "🎫 Skipping ticket, not owned by current user.");
                                            return;  // Skip if the ticket is not owned by the current user
                                        }

                                        // Create the Event object with the remaining tickets and other info
                                        Event event = new Event.Builder()
                                                .setEventName(eventDoc.getString("EventName"))
                                                .setDate(eventDoc.getString("date"))
                                                .setLocation(eventDoc.getString("location"))
                                                .setImage(eventDoc.getString("Image"))
                                                .setTicketId(ticketId)  // Use the Firestore document ID as the ticketId
                                                .setPrice(ticketDoc.getString("Price"))
                                                .setStatus(status)  // Add status as "Sold Out" or "Available"
                                                .setSellerId(sellerId)
                                                .setPaymentId(ticketDoc.getString("paymentId"))
                                                .setQrCodeUrl(ticketDoc.getString("qrCodeUrl"))
                                                .setTicketsSold(ticketsSold)  // Set ticketsSold field
                                                .setMaxTicketsPerEvent(maxTickets)  // Set maxTicketsPerEvent
                                                .build();

                                        eventList.add(event);
                                        eventAdapter.updateEvents(eventList);  // Ensure update is called
                                    });
                                } else {
                                    // Create the Event object with the remaining tickets and other info
                                    Event event = new Event.Builder()
                                            .setEventName(eventDoc.getString("EventName"))
                                            .setDate(eventDoc.getString("date"))
                                            .setLocation(eventDoc.getString("location"))
                                            .setImage(eventDoc.getString("Image"))
                                            .setTicketId(ticketId)  // Use the Firestore document ID as the ticketId
                                            .setPrice(ticketDoc.getString("Price"))
                                            .setStatus(status)  // Add status as "Sold Out" or "Available"
                                            .setSellerId(sellerId)
                                            .setPaymentId(ticketDoc.getString("paymentId"))
                                            .setQrCodeUrl(ticketDoc.getString("qrCodeUrl"))
                                            .setTicketsSold(ticketsSold)  // Set ticketsSold field
                                            .setMaxTicketsPerEvent(maxTickets)  // Set maxTicketsPerEvent
                                            .build();

                                    eventList.add(event);
                                    eventAdapter.updateEvents(eventList);  // Ensure update is called
                                }
                            }
                        }).addOnFailureListener(e -> {
                            Log.e(TAG, "❌ Failed to fetch tickets for event", e);
                        });
            }
        }).addOnFailureListener(e -> Log.e(TAG, "❌ Error loading events", e));
    }


    private void isTicketOwnedByUser(String userId, QueryDocumentSnapshot ticketDoc, OnTicketOwnedCallback callback) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(userDoc -> {
                    List<String> ownedTicketIds = (List<String>) userDoc.get("ticketsOwned");
                    if (ownedTicketIds != null && ownedTicketIds.contains(ticketDoc.getId())) {
                        Log.d(TAG, "Ticket is owned by the current user.");
                        callback.onOwned(true);  // Invoke the callback with true if the ticket is owned
                    } else {
                        callback.onOwned(false);  // Invoke the callback with false if the ticket is not owned
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to check ticket ownership", e);
                    callback.onOwned(false);  // Assume false in case of failure
                });
    }

    // Callback interface for async result
    public interface OnTicketOwnedCallback {
        void onOwned(boolean isOwned);
    }

    @Override
    public void buyClicked(Event event) {
        FirebaseUser user = authFirebase.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the event is sold out
        if (event.getStatus().equals("Sold Out")) {
            Toast.makeText(this, "This event is sold out", Toast.LENGTH_SHORT).show();
            return;
        }

        if (event.isOwnedBy(user.getUid())) {  // If the current user already owns the ticket
            // Change the button text to "View Details" and open the ViewTicket activity
            Intent viewIntent = new Intent(this, ViewTicket.class);
            viewIntent.putExtra("eventName", event.getEventName());
            viewIntent.putExtra("ticketId", event.getTicketId());
            viewIntent.putExtra("price", event.getPrice());
            viewIntent.putExtra("date", event.getDate());
            viewIntent.putExtra("location", event.getLocation());
            viewIntent.putExtra("qrCodeUrl", event.getQrCodeUrl());
            startActivity(viewIntent);
        } else {
            // Proceed with payment
            Intent paymentIntent = new Intent(this, Payment.class);
            paymentIntent.putExtra("ticketId", event.getTicketId());
            paymentIntent.putExtra("eventName", event.getEventName());
            paymentIntent.putExtra("sellerId", event.getSellerId());
            paymentIntent.putExtra("price", Double.parseDouble(event.getPrice()));
            startActivity(paymentIntent);
        }
    }
}
