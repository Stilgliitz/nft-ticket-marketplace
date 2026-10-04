package com.example.ticketapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Marketplace extends AppCompatActivity {

    private RecyclerView recyclerMarketplace;
    private EditText edtSearchMarketplace;
    private ResellAdapter resellAdapter;
    private List<ResaleTicket> allResaleTickets = new ArrayList<>();
    private List<ResaleTicket> filteredResaleTickets = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marketplace);

        recyclerMarketplace = findViewById(R.id.recyclerViewMarketplace);
        edtSearchMarketplace = findViewById(R.id.edtSearchMarketplace);

        recyclerMarketplace.setLayoutManager(new LinearLayoutManager(this));
        resellAdapter = new ResellAdapter(filteredResaleTickets, this);
        recyclerMarketplace.setAdapter(resellAdapter);

        loadResaleTickets();

        edtSearchMarketplace.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterResaleTickets(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    //load tickets from firebase
    private void loadResaleTickets() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("ResaleTickets")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allResaleTickets.clear();
                    filteredResaleTickets.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        ResaleTicket ticket = doc.toObject(ResaleTicket.class);
                        if (ticket != null && !"sold".equalsIgnoreCase(ticket.getStatus())) {
                            allResaleTickets.add(ticket);
                        }
                    }
                    filteredResaleTickets.addAll(allResaleTickets);
                    resellAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load resale tickets", Toast.LENGTH_SHORT).show();
                });
    }

    //search tickets
    private void filterResaleTickets(String searchQuery) {
        filteredResaleTickets.clear();
        if (searchQuery.isEmpty()) {
            filteredResaleTickets.addAll(allResaleTickets);
        } else {
            for (ResaleTicket ticket : allResaleTickets) {
                if (ticket.getTicketId().toLowerCase().contains(searchQuery.toLowerCase())
                        || ticket.getEventName().toLowerCase().contains(searchQuery.toLowerCase())
                        || ticket.getSellerName().toLowerCase().contains(searchQuery.toLowerCase())) {
                    filteredResaleTickets.add(ticket);
                }
            }
        }
        resellAdapter.notifyDataSetChanged();
    }
}
