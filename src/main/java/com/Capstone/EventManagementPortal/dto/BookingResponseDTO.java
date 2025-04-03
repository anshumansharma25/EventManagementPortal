package com.Capstone.EventManagementPortal.dto;

import com.Capstone.EventManagementPortal.model.Booking;
import com.Capstone.EventManagementPortal.model.BookingStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BookingResponseDTO {
    private Long id;
    private Long eventId;
    private String eventTitle;
    private LocalDateTime eventDate;
    private String location;
    private LocalDateTime bookingTime;
    private BookingStatus bookingStatus;
    private boolean isEventCancelled;
    @Getter
    private String statusDisplay;

    public BookingResponseDTO() {
    }

    public BookingResponseDTO(Booking booking) {
        // ... other mappings ...
        this.isEventCancelled = booking.getStatus() == BookingStatus.Event_cancelled;
        this.statusDisplay =  booking.getStatus().toString();
    }
    public boolean isEventCancelled() {
        return isEventCancelled;
    }

}