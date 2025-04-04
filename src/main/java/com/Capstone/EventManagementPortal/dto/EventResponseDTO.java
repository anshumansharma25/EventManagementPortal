package com.Capstone.EventManagementPortal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResponseDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime dateTime;
    private String location;
    private Integer availableSlots;
    private Integer maxSlots;
    private String category;
    private boolean isCancelled;
}