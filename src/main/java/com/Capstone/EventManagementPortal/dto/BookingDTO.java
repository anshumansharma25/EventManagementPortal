package com.Capstone.EventManagementPortal.dto;

import com.Capstone.EventManagementPortal.model.Booking;
import com.Capstone.EventManagementPortal.model.Event;
import com.Capstone.EventManagementPortal.model.User;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BookingDTO {

    private Long id;

    @NotNull
    private Long eventId;

    @NotNull
    private Long userId;

    private String userName;
    private String userEmail;

    private LocalDateTime bookingTime;

    // Default Constructor
    public BookingDTO() {}

    // Constructor from Entity
    public BookingDTO(Booking booking) {
        this.id = booking.getId();
        this.eventId = booking.getEvent().getId();
        this.userId = booking.getUser().getId();
        this.userName = booking.getUser().getName();
        this.userEmail = booking.getUser().getEmail();
        this.bookingTime = booking.getBookingTime();
    }

    public BookingDTO(Long id, Long id1, Long id2) {
        this.id = id;
    }

    // Convert DTO to Booking entity
    public Booking toEntity(Event event, User user) {
        Booking booking = new Booking();
        booking.setEvent(event);
        booking.setUser(user);
        return booking;
    }
}
