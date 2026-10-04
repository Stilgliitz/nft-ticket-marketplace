package com.example.ticketapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ResellAdapter extends RecyclerView.Adapter<ResellAdapter.ResellViewHolder> {
    private List<ResaleTicket> resaleTickets;
    private Context context;

    public ResellAdapter(List<ResaleTicket> resaleTickets, Context context) {
        this.resaleTickets = resaleTickets;
        this.context = context;
    }

    @NonNull
    @Override
    public ResellViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.resale_ticket_item, parent, false);
        return new ResellViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResellViewHolder holder, int position) {
        ResaleTicket ticket = resaleTickets.get(position);

        holder.txtTicketId.setText("Ticket ID: " + ticket.getTicketId());
        holder.txtPrice.setText("Price: £" + ticket.getPrice());

        //Disable Buy button if sold
        if ("sold".equalsIgnoreCase(ticket.getStatus())) {
            holder.btnBuyNow.setEnabled(false);
            holder.btnBuyNow.setText("SOLD");
            holder.btnBuyNow.setBackgroundColor(Color.GRAY); //Greyed out for disable function
        } else {
            holder.btnBuyNow.setEnabled(true);
            holder.btnBuyNow.setText("BUY NOW");
            holder.btnBuyNow.setBackgroundColor(Color.parseColor("#1E88E5")); //Setting Colour to be different

            holder.btnBuyNow.setOnClickListener(v -> {
                Intent intent = new Intent(context, Payment2.class);
                intent.putExtra("ticketId", ticket.getTicketId());
                intent.putExtra("price", ticket.getPrice());
                intent.putExtra("sellerWallet", ticket.getSellerWallet());
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return resaleTickets.size();
    }

    public static class ResellViewHolder extends RecyclerView.ViewHolder {
        TextView txtTicketId, txtPrice;
        Button btnBuyNow;

        public ResellViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTicketId = itemView.findViewById(R.id.txtTicketId);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            btnBuyNow = itemView.findViewById(R.id.btnBuyNow);
        }
    }
}
