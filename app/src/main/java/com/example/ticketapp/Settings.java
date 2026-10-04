package com.example.ticketapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Settings extends AppCompatActivity {

    private FirebaseAuth authFirebase;
    private FirebaseFirestore db;
    private RecyclerView recyclerMyTickets;
    private List<Ticket> myTicketsList;
    private TicketAdapter myTicketsAdapter;

    private TextView txtName, txtEmail;
    private ImageView imgProfilePic;
    private ConstraintLayout accountLayout, notificationsLayout, ordersLayout, preferencesLayout, privacySupportLayout, linkWalletLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        imgProfilePic = findViewById(R.id.imgPic);
        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        accountLayout = findViewById(R.id.accountLayout);
        notificationsLayout = findViewById(R.id.notificationsLayout);
        ordersLayout = findViewById(R.id.ordersLayout);
        preferencesLayout = findViewById(R.id.preferencesLayout);
        privacySupportLayout = findViewById(R.id.privacySupportLayout);
        linkWalletLayout = findViewById(R.id.linkWalletLayout);

        recyclerMyTickets = findViewById(R.id.recyclerMyTickets);

        authFirebase = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupMyTicketsRecycler();
        loadUserProfile();
        loadUserTickets();

        accountLayout.setOnClickListener(view -> startActivity(new Intent(this, Account.class)));
        notificationsLayout.setOnClickListener(view -> Toast.makeText(this, "Notifications page coming soon!", Toast.LENGTH_SHORT).show());
        ordersLayout.setOnClickListener(view -> Toast.makeText(this, "Orders page coming soon!", Toast.LENGTH_SHORT).show());
        preferencesLayout.setOnClickListener(view -> startActivity(new Intent(this, SellNTransfer.class)));
        privacySupportLayout.setOnClickListener(view -> startActivity(new Intent(this, Marketplace.class)));
        linkWalletLayout.setOnClickListener(view -> startActivity(new Intent(this, LinkWallet.class)));
    }

    private void setupMyTicketsRecycler() {
        recyclerMyTickets.setLayoutManager(new LinearLayoutManager(this));
        myTicketsList = new ArrayList<>();
        myTicketsAdapter = new TicketAdapter(myTicketsList, this);
        recyclerMyTickets.setAdapter(myTicketsAdapter);
    }

    private void loadUserProfile() {
        FirebaseUser user = authFirebase.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DocumentReference userRef = db.collection("users").document(userId);
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("name");
                    String email = user.getEmail();
                    String profileUrl = documentSnapshot.getString("profileUrl");

                    txtName.setText(name);
                    txtEmail.setText(email);

                    if (profileUrl != null && !profileUrl.isEmpty()) {
                        Glide.with(this).load(profileUrl).into(imgProfilePic);
                    }
                } else {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void loadUserTickets() {
        FirebaseUser user = authFirebase.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> ticketsOwned = (List<String>) documentSnapshot.get("ticketsOwned");
                    if (ticketsOwned != null && !ticketsOwned.isEmpty()) {
                        fetchTickets(ticketsOwned);
                    } else {
                        Toast.makeText(this, "You don't have any tickets", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load tickets", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchTickets(List<String> ticketIds) {
        for (String ticketId : ticketIds) {
            db.collection("Events").get().addOnSuccessListener(eventSnapshots -> {
                for (QueryDocumentSnapshot eventDoc : eventSnapshots) {
                    db.collection("Events").document(eventDoc.getId())
                            .collection("Tickets")
                            .document(ticketId)
                            .get()
                            .addOnSuccessListener(ticketSnapshot -> {
                                if (ticketSnapshot.exists()) {
                                    Ticket ticket = new Ticket(
                                            ticketId,
                                            ticketSnapshot.getString("ownerID"),
                                            ticketSnapshot.getString("price"),
                                            ticketSnapshot.getString("status"),
                                            ticketSnapshot.getString("paymentId"),
                                            ticketSnapshot.getString("sellerId"),
                                            eventDoc.getString("EventName"),
                                            eventDoc.getString("date"),
                                            eventDoc.getString("location"),
                                            ticketSnapshot.getString("qrCodeUrl")
                                    );

                                    myTicketsList.add(ticket);
                                    myTicketsAdapter.notifyDataSetChanged();
                                }
                            }).addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to fetch ticket data", Toast.LENGTH_SHORT).show();
                            });
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
