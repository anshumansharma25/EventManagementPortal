package com.Capstone.EventManagementPortal.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false)
    private BookingStatus status = BookingStatus.Confirmed;

    @Column(name = "booking_time", nullable = false)
    private LocalDateTime bookingTime;

    @Column(name = "is_cancelled", nullable = false)
    private boolean isCancelled = false; // ✅ Default to false


    @PrePersist
    public void prePersist() {
        if (bookingTime == null) {
            bookingTime = LocalDateTime.now();
        }
        if (status == null) {
            status = BookingStatus.Confirmed;
        }
    }


    public BookingStatus getStatus() {
        return status != null ? status : BookingStatus.Confirmed;
    }

    public void setStatus(BookingStatus status) {
        this.status = status != null ? status : BookingStatus.Confirmed;
    }

}
