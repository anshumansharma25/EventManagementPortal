package com.Capstone.EventManagementPortal.exception;  // Adjust the package name accordingly

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
