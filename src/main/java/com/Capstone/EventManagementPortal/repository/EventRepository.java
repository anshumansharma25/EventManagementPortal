package com.Capstone.EventManagementPortal.repository;

import com.Capstone.EventManagementPortal.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganizer_Id(Long organizerId);  // Custom method to fetch events by organizer
    List<Event> findByLocation(String location);
}
