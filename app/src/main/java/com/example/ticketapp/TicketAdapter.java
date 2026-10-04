package com.example.ticketapp;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private List<Ticket> ticketList;
    private Context context;

    //Constructor
    public TicketAdapter(List<Ticket> ticketList, Context context) {
        this.ticketList = ticketList;
        this.context = context;
    }

    //Create ViewHolder
    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    //Bind data to ViewHolder
    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Ticket ticket = ticketList.get(position);

        holder.txtTicketEvent.setText(ticket.getEventName());
        holder.txtTicketDate.setText("Date: " + ticket.getDate());
        holder.txtTicketLocation.setText("Location: " + ticket.getLocation());

        if (ticket.getQrCodeUrl() != null && !ticket.getQrCodeUrl().isEmpty()) {
            Glide.with(context)
                    .load(ticket.getQrCodeUrl())
                    .into(holder.imgQrCode);
        }

        //Animation
        holder.itemView.setAnimation(AnimationUtils.loadAnimation(context, R.anim.recycler_item_animation));

        //Click to show ticket details
        holder.itemView.setOnClickListener(v -> showTicketDialog(ticket));
    }

    //Show Ticket Dialog
    private void showTicketDialog(Ticket ticket) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_ticket_details, null);

        TextView txtEvent = dialogView.findViewById(R.id.txtTicketEvent);
        TextView txtDate = dialogView.findViewById(R.id.txtTicketDate);
        TextView txtLocation = dialogView.findViewById(R.id.txtTicketLocation);
        TextView txtPrice = dialogView.findViewById(R.id.txtTicketPrice);
        ImageView imgQrCode = dialogView.findViewById(R.id.imgQrCode);

        txtEvent.setText(ticket.getEventName());
        txtDate.setText("Date: " + ticket.getDate());
        txtLocation.setText("Location: " + ticket.getLocation());
        txtPrice.setText("Price: £" + ticket.getPrice());

        if (ticket.getQrCodeUrl() != null && !ticket.getQrCodeUrl().isEmpty()) {
            Glide.with(context).load(ticket.getQrCodeUrl()).into(imgQrCode);
        }

        //Dialog to view ticket
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Ticket Details")
                .setView(dialogView)
                .setCancelable(true)
                .setPositiveButton("OK", null)
                .setNegativeButton("Request Details", (dialog, which) -> {
                    showRequestDetailsDialog(ticket);
                })
                .show();
    }

    //Show Request Details Dialog
    private void showRequestDetailsDialog(Ticket ticket) {
        String ownerId = (ticket.getOwnerId() != null) ? ticket.getOwnerId() : "N/A";
        String ticketId = (ticket.getTicketId() != null) ? ticket.getTicketId() : "N/A";

        String message = "Owner ID: " + ownerId + "\n\nTicket ID: " + ticketId;

        new AlertDialog.Builder(context)
                .setTitle("Your Ticket Details")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .setCancelable(true)
                .show();
    }

    //Get ticket count
    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    //ViewHolder class
    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView txtTicketEvent, txtTicketDate, txtTicketLocation;
        ImageView imgQrCode;

        public TicketViewHolder(View itemView) {
            super(itemView);
            txtTicketEvent = itemView.findViewById(R.id.txtTicketEvent);
            txtTicketDate = itemView.findViewById(R.id.txtTicketDate);
            txtTicketLocation = itemView.findViewById(R.id.txtTicketLocation);
            imgQrCode = itemView.findViewById(R.id.imgQrCode);
        }
    }
}
