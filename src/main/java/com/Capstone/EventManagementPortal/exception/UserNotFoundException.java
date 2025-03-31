package com.Capstone.EventManagementPortal.exception;  // Adjust package accordingly

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
