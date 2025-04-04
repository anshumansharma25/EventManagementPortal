package com.Capstone.EventManagementPortal.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][XXX]")
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    @Getter
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<Booking> bookings = new ArrayList<>();

    @Column(nullable = false)
    private int maxSlots;

    @Column(nullable = false)
    private int availableSlots;

    @Column(nullable = false)
    private String location;

    @ManyToOne
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @Column(name = "is_cancelled", nullable = false, columnDefinition = "boolean default false")
    private Boolean isCancelled = false;

    public Boolean isCancelled() {
        return isCancelled;
    }

    public void setDateTime(LocalDateTime dateTime) {
        if (dateTime.isBefore(LocalDateTime.now().plusHours(24))) {
            throw new IllegalArgumentException("Events must be scheduled at least 24 hours in advance");
        }
        this.dateTime = dateTime;
    }

    public void setCancelled(Boolean cancelled) {
        isCancelled = cancelled;
    }

    public void setMaxSlots(int maxSlots) {
        if (maxSlots <= 0) {
            throw new IllegalArgumentException("Max slots must be greater than zero");
        }
        this.maxSlots = maxSlots;
        this.availableSlots = maxSlots; // Default available slots to max slots
    }
}
