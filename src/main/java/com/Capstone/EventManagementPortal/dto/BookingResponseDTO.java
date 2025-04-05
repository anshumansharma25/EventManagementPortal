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
    @Setter
    @Getter
    private String eventDescription;
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
        this.isEventCancelled = booking.getEvent().getIsCancelled();
        this.statusDisplay = isEventCancelled ? "EVENT CANCELLED" : "CONFIRMED";
    }
    public boolean isEventCancelled() {
        return isEventCancelled;
    }

}