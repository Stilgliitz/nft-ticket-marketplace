package com.example.ticketapp;

import androidx.annotation.NonNull;

public class Event {
    //Event fields
    private String eventName;
    private String date;
    private String location;
    private String image;
    private int ticketsSold;  //Tracks sold tickets

    //Ticket fields
    private String ticketId;
    private String price;
    private String status;
    private String paymentId;
    private String ownerID;
    private String sellerId;
    private String sellerName;


    private String qrCodeUrl;


    private int maxTicketsPerEvent;  //Maximum tickets allowed for the event

    public Event() {}

    //Constructor
    public Event(String eventName, String date, String location, String image,
                 String ticketId, String price, String status, String paymentId,
                 String ownerID, String sellerId, String sellerName, String qrCodeUrl,
                 int ticketsSold, int maxTicketsPerEvent) {
        this.eventName = eventName;
        this.date = date;
        this.location = location;
        this.image = image;
        this.ticketId = ticketId;
        this.price = price;
        this.status = status;
        this.paymentId = paymentId;
        this.ownerID = ownerID;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.qrCodeUrl = qrCodeUrl;
        this.ticketsSold = ticketsSold;
        this.maxTicketsPerEvent = maxTicketsPerEvent;
    }

    //Null-safe Getters and Setters
    @NonNull public String getEventName() { return eventName != null ? eventName : "Unnamed Event"; }
    @NonNull public String getDate() { return date != null ? date : "Date TBA"; }
    @NonNull public String getLocation() { return location != null ? location : "Location TBA"; }
    @NonNull public String getImage() { return image != null ? image : ""; }
    @NonNull public String getTicketId() { return ticketId != null ? ticketId : ""; }
    @NonNull public String getPrice() { return price != null ? price : "0"; }
    @NonNull public String getStatus() { return status != null ? status : "available"; }
    @NonNull public String getPaymentId() { return paymentId != null ? paymentId : ""; }
    @NonNull public String getOwnerID() { return ownerID != null ? ownerID : ""; }
    @NonNull public String getSellerId() { return sellerId != null ? sellerId : ""; }
    @NonNull public String getSellerName() { return sellerName != null ? sellerName : "Unknown Seller"; }
    @NonNull public String getQrCodeUrl() { return qrCodeUrl != null ? qrCodeUrl : ""; }
    public int getTicketsSold() { return ticketsSold; }
    public int getMaxTicketsPerEvent() { return maxTicketsPerEvent; }

    //Check if event tickets are still available
    public boolean isAvailable() {
        return ticketsSold < maxTicketsPerEvent; // Checks against maxTicketsPerEvent
    }

    public boolean isOwnedBy(String userId) {
        return userId != null && userId.equals(ownerID);  // Check if event is owned by this user
    }

    //Increment ticketsSold
    public void incrementTicketsSold() {
        this.ticketsSold++;
    }

    //Builder pattern for creating Event objects
    public static class Builder {
        private final Event event;

        public Builder() {
            event = new Event();
        }

        public Builder setEventName(String eventName) {
            event.eventName = eventName;
            return this;
        }

        public Builder setDate(String date) {
            event.date = date;
            return this;
        }

        public Builder setLocation(String location) {
            event.location = location;
            return this;
        }

        public Builder setImage(String image) {
            event.image = image;
            return this;
        }

        public Builder setTicketId(String ticketId) {
            event.ticketId = ticketId;
            return this;
        }

        public Builder setPrice(String price) {
            event.price = price;
            return this;
        }

        public Builder setStatus(String status) {
            event.status = status;
            return this;
        }

        public Builder setPaymentId(String paymentId) {
            event.paymentId = paymentId;
            return this;
        }

        public Builder setOwnerID(String ownerID) {
            event.ownerID = ownerID;
            return this;
        }

        public Builder setSellerId(String sellerId) {
            event.sellerId = sellerId;
            return this;
        }

        public Builder setSellerName(String sellerName) {
            event.sellerName = sellerName;
            return this;
        }

        public Builder setQrCodeUrl(String qrCodeUrl) {
            event.qrCodeUrl = qrCodeUrl;
            return this;
        }

        public Builder setTicketsSold(int ticketsSold) {
            event.ticketsSold = ticketsSold;
            return this;
        }

        public Builder setMaxTicketsPerEvent(int maxTicketsPerEvent) {
            event.maxTicketsPerEvent = maxTicketsPerEvent;
            return this;
        }

        public Event build() {
            return event;
        }
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventName='" + eventName + '\'' +
                ", date='" + date + '\'' +
                ", location='" + location + '\'' +
                ", image='" + image + '\'' +
                ", ticketId='" + ticketId + '\'' +
                ", price='" + price + '\'' +
                ", status='" + status + '\'' +
                ", paymentId='" + paymentId + '\'' +
                ", ownerID='" + ownerID + '\'' +
                ", sellerId='" + sellerId + '\'' +
                ", sellerName='" + sellerName + '\'' +
                ", qrCodeUrl='" + qrCodeUrl + '\'' +
                ", ticketsSold=" + ticketsSold +
                ", maxTicketsPerEvent=" + maxTicketsPerEvent +
                '}';
    }
}
