package com.Capstone.EventManagementPortal.service;

import com.Capstone.EventManagementPortal.model.Event;
import java.util.List;
import java.util.Optional;

public interface EventService {
    Event createEvent(Event event);
    Optional<Event> getEventById(Long id);
    List<Event> getAllEvents();
    Event updateEvent(Long id, Event event);
    void deleteEvent(Long id);
    List<Event> getEventsByLocation(String location);

}
