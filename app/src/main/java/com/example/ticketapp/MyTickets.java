package com.example.ticketapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyTickets extends AppCompatActivity {

    private RecyclerView recyclerTickets;
    private FirebaseAuth authFirebase;
    private FirebaseFirestore db;
    private TicketAdapter ticketAdapter;
    private List<Ticket> ticketList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tickets);


        recyclerTickets = findViewById(R.id.recyclerTickets);
        authFirebase = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //Set up RecyclerView
        recyclerTickets.setLayoutManager(new LinearLayoutManager(this));
        ticketList = new ArrayList<>();
        ticketAdapter = new TicketAdapter(ticketList, this);
        recyclerTickets.setAdapter(ticketAdapter);

        //Load tickets
        loadMyTickets();
    }

    //Load the current user's tickets
    private void loadMyTickets() {
        FirebaseUser user = authFirebase.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login to see your tickets!", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = user.getUid();

        //Fetch the user's owned ticket IDs
        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> ticketsOwned = (List<String>) documentSnapshot.get("ticketsOwned");
                        if (ticketsOwned != null && !ticketsOwned.isEmpty()) {
                            fetchTickets(ticketsOwned);
                        } else {
                            Toast.makeText(this, "You currently own no tickets.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load tickets!", Toast.LENGTH_SHORT).show();
                });
    }

    //Fetch actual ticket documents based on owned ticket IDs
    private void fetchTickets(List<String> ticketIds) {
        db.collection("Events").get().addOnSuccessListener(eventSnapshots -> {
            for (QueryDocumentSnapshot eventDoc : eventSnapshots) {
                for (String ticketId : ticketIds) {
                    db.collection("Events").document(eventDoc.getId())
                            .collection("Tickets").document(ticketId)
                            .get().addOnSuccessListener(ticketSnapshot -> {
                                if (ticketSnapshot.exists()) {
                                    //Manually map fields because auto-mapping sometimes fails
                                    Ticket ticket = new Ticket();
                                    ticket.setTicketId(ticketSnapshot.getString("ticketId"));
                                    ticket.setOwnerId(ticketSnapshot.getString("ownerID")); // ⭐ CRITICAL
                                    ticket.setPrice(ticketSnapshot.getString("price"));
                                    ticket.setStatus(ticketSnapshot.getString("status"));
                                    ticket.setPaymentId(ticketSnapshot.getString("paymentId"));
                                    ticket.setSellerId(ticketSnapshot.getString("sellerId"));
                                    ticket.setEventName(ticketSnapshot.getString("eventName"));
                                    ticket.setDate(ticketSnapshot.getString("date"));
                                    ticket.setLocation(ticketSnapshot.getString("location"));
                                    ticket.setQrCodeUrl(ticketSnapshot.getString("qrCodeUrl"));

                                    //Add ticket to list and refresh adapter
                                    ticketList.add(ticket);
                                    ticketAdapter.notifyDataSetChanged();
                                }
                            }).addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to load ticket data.", Toast.LENGTH_SHORT).show();
                            });
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load event data.", Toast.LENGTH_SHORT).show();
        });
    }
}
