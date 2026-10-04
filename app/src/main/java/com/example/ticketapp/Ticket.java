package com.example.ticketapp;

import androidx.annotation.NonNull;

public class Ticket {
    private String ticketId;
    private String ownerId;
    private String price;
    private String status;
    private String paymentId;
    private String sellerId;
    private String eventName;
    private String date;
    private String location;
    private String qrCodeUrl;

    public Ticket() {}

    //Constructor for creating a full ticket
    public Ticket(String ticketId, String ownerId, String price, String status,
                  String paymentId, String sellerId, String eventName, String date, String location, String qrCodeUrl) {
        this.ticketId = ticketId;
        this.ownerId = ownerId;
        this.price = price;
        this.status = status;
        this.paymentId = paymentId;
        this.sellerId = sellerId;
        this.eventName = eventName;
        this.date = date;
        this.location = location;
        this.qrCodeUrl = qrCodeUrl;
    }

    //Getter methods
    @NonNull public String getTicketId() { return ticketId != null ? ticketId : ""; }
    @NonNull public String getOwnerId() { return ownerId != null ? ownerId : ""; }
    @NonNull public String getPrice() { return price != null ? price : "0"; }
    @NonNull public String getStatus() { return status != null ? status : "available"; }
    @NonNull public String getPaymentId() { return paymentId != null ? paymentId : ""; }
    @NonNull public String getSellerId() { return sellerId != null ? sellerId : ""; }
    @NonNull public String getEventName() { return eventName != null ? eventName : "Unknown Event"; }
    @NonNull public String getDate() { return date != null ? date : "Unknown Date"; }
    @NonNull public String getLocation() { return location != null ? location : "Unknown Location"; }
    @NonNull public String getQrCodeUrl() { return qrCodeUrl != null ? qrCodeUrl : ""; }

    //Setter methods
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public void setPrice(String price) { this.price = price; }
    public void setStatus(String status) { this.status = status; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public void setDate(String date) { this.date = date; }
    public void setLocation(String location) { this.location = location; }
    public void setQrCodeUrl(String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }

    //Helper methods
    public boolean isAvailable() {
        return "available".equalsIgnoreCase(status);
    }

    public boolean isOwnedBy(String uid) {
        return uid != null && uid.equals(ownerId);
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "ticketId='" + ticketId + '\'' +
                ", ownerId='" + ownerId + '\'' +
                ", price='" + price + '\'' +
                ", status='" + status + '\'' +
                ", paymentId='" + paymentId + '\'' +
                ", sellerId='" + sellerId + '\'' +
                ", eventName='" + eventName + '\'' +
                ", date='" + date + '\'' +
                ", location='" + location + '\'' +
                ", qrCodeUrl='" + qrCodeUrl + '\'' +
                '}';
    }
}
