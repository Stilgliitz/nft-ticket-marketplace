package com.example.ticketapp;

public class EventTicket {
    private Event event;
    private Ticket ticket;

    public EventTicket(Event event, Ticket ticket) {
        this.event = event;
        this.ticket = ticket;
    }

    public Event getEvent() {
        return event;
    }

    public Ticket getTicket() {
        return ticket;
    }
}
