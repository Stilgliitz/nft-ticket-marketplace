package com.example.ticketapp;

public class ResaleTicket {
    private String ticketId;
    private String price;
    private String eventName;
    private String location;
    private String date;
    private String sellerName;
    private String sellerWallet;
    private String ownerId;
    private String status;

    public ResaleTicket() {}

    public ResaleTicket(String ticketId, String price, String eventName, String location, String date,
                        String sellerName, String sellerWallet, String ownerId, String status) {
        this.ticketId = ticketId;
        this.price = price;
        this.eventName = eventName;
        this.location = location;
        this.date = date;
        this.sellerName = sellerName;
        this.sellerWallet = sellerWallet;
        this.ownerId = ownerId;
        this.status = status;
    }

    //Getters
    public String getTicketId() { return ticketId; }
    public String getPrice() { return price; }
    public String getEventName() { return eventName; }
    public String getLocation() { return location; }
    public String getDate() { return date; }
    public String getSellerName() { return sellerName; }
    public String getSellerWallet() { return sellerWallet; }
    public String getOwnerId() { return ownerId; }
    public String getStatus() { return status; }
}
