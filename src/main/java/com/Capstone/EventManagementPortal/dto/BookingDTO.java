package com.Capstone.EventManagementPortal.dto;

import com.Capstone.EventManagementPortal.model.Booking;
import com.Capstone.EventManagementPortal.model.BookingStatus;
import com.Capstone.EventManagementPortal.model.Event;
import com.Capstone.EventManagementPortal.model.User;

import java.time.LocalDateTime;
public class BookingDTO {
    private Long id;
    private Long eventId;
    private String eventTitle;
    private String userEmail;
    private BookingStatus bookingStatus;
    private LocalDateTime bookingTime;

    // ✅ No-arg constructor (Required for JSON deserialization)
    public BookingDTO() {}

    public BookingDTO(Booking booking) {
        this.id = booking.getId();
        this.eventId = booking.getEvent().getId();
        this.eventTitle = booking.getEvent().getTitle();
        this.userEmail = booking.getUser().getEmail();
        this.bookingStatus = booking.getBookingStatus();
        this.bookingTime = booking.getBookingTime();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public BookingStatus getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(BookingStatus bookingStatus) { this.bookingStatus = bookingStatus; }

    public LocalDateTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }

    public Booking toEntity(User user, Event event) {
        if (user == null || event == null) {
            throw new IllegalArgumentException("User or Event cannot be null");
        }
        Booking booking = new Booking();
        booking.setId(this.id);
        booking.setUser(user);
        booking.setEvent(event);
        booking.setBookingStatus(this.bookingStatus != null ? this.bookingStatus : BookingStatus.CONFIRMED);
        booking.setBookingTime(this.bookingTime != null ? this.bookingTime : LocalDateTime.now());
        return booking;
    }
}
