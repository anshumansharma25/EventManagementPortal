package com.Capstone.EventManagementPortal.repository;

import com.Capstone.EventManagementPortal.model.Booking;
import com.Capstone.EventManagementPortal.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);  // Get all registrations by user
    List<Booking> findByEventId(Long eventId); // Get all users registered for an event
    @Modifying
    @Query("UPDATE Booking b SET b.status = :status WHERE b.event.id = :eventId")
    void updateBookingsForCancelledEvent(
            @Param("eventId") Long eventId,
            @Param("status") BookingStatus status  // Use enum type directly
    );
}
