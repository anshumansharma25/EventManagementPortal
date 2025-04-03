package com.Capstone.EventManagementPortal.service;

import com.Capstone.EventManagementPortal.dto.EventDTO;
import com.Capstone.EventManagementPortal.model.Event;

import java.util.List;
import java.util.Optional;

public interface EventService {

    Event createEvent(Event event, String organizerEmail); // Ensure it takes 2 parameters

    Optional<Event> updateEvent(Long eventId, Event eventDetails, String organizerEmail);

    void deleteEvent(Long eventId, String organizerEmail);

    List<Event> getAllEvents(); // Ensure this returns a list of Event, not Optional<Event>

    Event getEventById(Long eventId);

    public List<EventDTO> getEventsByOrganizer(String email);

}
