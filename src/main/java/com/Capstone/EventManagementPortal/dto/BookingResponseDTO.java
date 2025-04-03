package com.Capstone.EventManagementPortal.dto;

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
}