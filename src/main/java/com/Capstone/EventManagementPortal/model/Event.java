package com.Capstone.EventManagementPortal.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
}
