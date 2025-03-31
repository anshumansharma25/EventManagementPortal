package com.Capstone.EventManagementPortal.exception;  // Adjust package accordingly

public class EventNotFoundException extends RuntimeException {
    public EventNotFoundException(String message) {
        super(message);
    }
}
