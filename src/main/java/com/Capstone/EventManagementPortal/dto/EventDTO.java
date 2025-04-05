package com.Capstone.EventManagementPortal.dto;

import com.Capstone.EventManagementPortal.model.Event;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    @Getter
    private LocalDateTime dateTime;
    @Getter
    private String formattedDateTime;
    @Getter
    private String location;
    @Getter
    private int maxSlots;
    @Getter
    private int availableSlots;
    private String organizerEmail;
    @Getter
    @JsonProperty("isCancelled")
    private boolean isCancelled;

    public EventDTO(Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.description = event.getDescription();
        this.category = event.getCategory();
        this.dateTime = event.getDateTime();
        this.location = event.getLocation();
        this.maxSlots = event.getMaxSlots();
        this.availableSlots = event.getAvailableSlots();
        this.organizerEmail = event.getOrganizer().getEmail();
        this.formattedDateTime = formatDateTime(event.getDateTime());
        this.isCancelled = event.getIsCancelled();
    }
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "Date not set";
        return dateTime.format(DateTimeFormatter.ofPattern("EEE, MMM d yyyy 'at' h:mm a"));
    }
    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
        this.formattedDateTime = formatDateTime(dateTime);
    }

}
