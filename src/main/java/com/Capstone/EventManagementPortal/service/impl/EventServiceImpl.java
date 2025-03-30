package com.Capstone.EventManagementPortal.service.impl;

import com.Capstone.EventManagementPortal.dto.EventDTO;
import com.Capstone.EventManagementPortal.model.Event;
import com.Capstone.EventManagementPortal.model.User;
import com.Capstone.EventManagementPortal.repository.EventRepository;
import com.Capstone.EventManagementPortal.repository.UserRepository;
import com.Capstone.EventManagementPortal.service.EventService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventServiceImpl(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Event createEvent(Event event, String organizerEmail) {
        User organizer = userRepository.findByEmail(organizerEmail)
                .orElseThrow(() -> new RuntimeException("Organizer not found"));
        event.setOrganizer(organizer);
        return eventRepository.save(event);
    }

    @Override
    public Optional<Event> updateEvent(Long eventId, Event eventDetails, String organizerEmail) {
        return eventRepository.findById(eventId).map(event -> {
            if (!event.getOrganizer().getEmail().equals(organizerEmail)) {
                throw new RuntimeException("Unauthorized: Only the event organizer can update this event.");
            }

            event.setTitle(eventDetails.getTitle());
            event.setDescription(eventDetails.getDescription());
            event.setCategory(eventDetails.getCategory());
            event.setDateTime(eventDetails.getDateTime());
            event.setMaxSlots(eventDetails.getMaxSlots());
            event.setAvailableSlots(eventDetails.getAvailableSlots());
            event.setLocation(eventDetails.getLocation());

            return eventRepository.save(event);
        });
    }

    @Override
    public void deleteEvent(Long eventId, String organizerEmail) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.getOrganizer().getEmail().equals(organizerEmail)) {
            throw new RuntimeException("Unauthorized: Only the event organizer can delete this event.");
        }

        eventRepository.delete(event);
    }

    @Override
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Override
    public Optional<Event> getEventById(Long eventId) {
        return eventRepository.findById(eventId);
    }
}
