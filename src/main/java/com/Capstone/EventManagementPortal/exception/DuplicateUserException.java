package com.Capstone.EventManagementPortal.exception;  // Adjust the package name accordingly

public class DuplicateUserException extends RuntimeException {
    public DuplicateUserException() {
        super("Email or username already taken");
    }
}
