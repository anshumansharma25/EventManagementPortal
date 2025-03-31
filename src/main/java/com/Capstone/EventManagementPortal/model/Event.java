package com.Capstone.EventManagementPortal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // ✅ Enables builder pattern
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String category; // e.g., Conference, Workshop

    @Column(nullable = false)
    @Future(message = "Event date must be in the future")
    private LocalDateTime dateTime;

    @Column(nullable = false)
    private int maxSlots;

    @Column(nullable = false)
    private int availableSlots;

    @Column(nullable = false)
    private String location;

    @ManyToOne
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    // ✅ Ensure date validation is applied when setting event date
    public void setDateTime(LocalDateTime dateTime) {
        if (dateTime.isBefore(LocalDateTime.now().plusHours(24))) {
            throw new IllegalArgumentException("Events must be scheduled at least 24 hours in advance");
        }
        this.dateTime = dateTime;
    }

    // ✅ Ensure availableSlots is initialized correctly
    public void setMaxSlots(int maxSlots) {
        if (maxSlots <= 0) {
            throw new IllegalArgumentException("Max slots must be greater than zero");
        }
        this.maxSlots = maxSlots;
        this.availableSlots = maxSlots; // Default available slots to max slots
    }
}
