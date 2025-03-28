package com.Capstone.EventManagementPortal.repository;

import com.Capstone.EventManagementPortal.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);  // Get all registrations by user
    List<Booking> findByEventId(Long eventId); // Get all users registered for an event
}
