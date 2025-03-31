package com.Capstone.EventManagementPortal.dto;

import com.Capstone.EventManagementPortal.model.Event;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Optional;

public class EventDTO {
    @Getter
    private Long id;
    @Getter
    private String title;
    @Getter
    private String description;
    @Getter
    private String category;
    private LocalDateTime dateTime;
    @Getter
    private String location;
    @Getter
    private int maxSlots;
    @Getter
    private int availableSlots;
    private String organizerEmail;

    public EventDTO(Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.description = event.getDescription();
        this.category = event.getCategory();
        this.dateTime = event.getDateTime();
        this.location = event.getLocation();
        this.maxSlots = event.getMaxSlots();
        this.availableSlots = event.getAvailableSlots();
        this.organizerEmail = event.getOrganizer().getEmail(); // Ensure organizer is not null
    }


}
