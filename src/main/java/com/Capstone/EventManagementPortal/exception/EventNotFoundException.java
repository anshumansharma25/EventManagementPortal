package com.Capstone.EventManagementPortal.exception;  // Adjust package accordingly

public class EventNotFoundException extends RuntimeException {
    public EventNotFoundException(String eventId) {
        super("Event not found with id: " + eventId); // Convert Long to String
    }
}
