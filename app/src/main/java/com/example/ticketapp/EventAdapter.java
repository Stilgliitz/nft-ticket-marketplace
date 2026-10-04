package com.example.ticketapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private Context context;

    public interface clicksBuy {
        void buyClicked(Event event);
    }

    private final clicksBuy buyClicker;

    public EventAdapter(List<Event> eventList, Context context, clicksBuy listener) {
        this.eventList = eventList;
        this.context = context;
        this.buyClicker = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.txtEventName.setText(event.getEventName());
        holder.txtEventDate.setText("Date: " + event.getDate());
        holder.txtEventLocation.setText("Location: " + event.getLocation());
        holder.txtEventPrice.setText("Price: " + event.getPrice());
        holder.txtRemainingTickets.setText("Remaining Tickets: " + (event.getMaxTicketsPerEvent() - event.getTicketsSold()));

        //Load image
        if (event.getImage() != null && !event.getImage().isEmpty()) {
            Glide.with(context)
                    .load(event.getImage())
                    .placeholder(R.drawable.ticket)
                    .into(holder.imgEvent);
        } else {
            holder.imgEvent.setImageResource(R.drawable.ticket);
        }

        //Logic for event click
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            if (event.isOwnedBy(user.getUid())) {
                //If the user owns the ticket
                holder.txtRemainingTickets.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));

                //old method keep for later function change
                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(context, ViewTicket.class);
                    intent.putExtra("ticketId", event.getTicketId());  //Pass only the ticketId
                    context.startActivity(intent);
                });
            } else {
                //old method keep for later function change
                boolean isAvailable = event.isAvailable();
                holder.txtRemainingTickets.setTextColor(isAvailable ? Color.BLACK : Color.RED);

                //Clicking on the event open the payment tickets
                holder.itemView.setOnClickListener(v -> {
                    if (buyClicker != null && isAvailable) {
                        Intent intent = new Intent(context, Payment.class);
                        intent.putExtra("eventName", event.getEventName());  // Pass the eventName instead of eventId
                        intent.putExtra("ticketId", event.getTicketId());
                        intent.putExtra("sellerId", event.getSellerId());
                        intent.putExtra("price", Double.parseDouble(event.getPrice())); // Ticket price
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "This event is sold out", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }





    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public void updateEvents(List<Event> newEvents) {
        this.eventList = newEvents;
        notifyDataSetChanged();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView txtEventName, txtEventDate, txtEventLocation, txtEventPrice, txtRemainingTickets;
        ImageView imgEvent;

        public EventViewHolder(View itemView) {
            super(itemView);
            txtEventName = itemView.findViewById(R.id.txtEventName);
            txtEventDate = itemView.findViewById(R.id.txtEventDate);
            txtEventLocation = itemView.findViewById(R.id.txtEventLocation);
            txtEventPrice = itemView.findViewById(R.id.txtEventPrice);
            txtRemainingTickets = itemView.findViewById(R.id.txtRemainingTickets);
            imgEvent = itemView.findViewById(R.id.imgEvent);
        }
    }
}
