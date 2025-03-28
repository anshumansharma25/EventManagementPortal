package com.Capstone.EventManagementPortal.service.impl;

import com.Capstone.EventManagementPortal.model.Event;
import com.Capstone.EventManagementPortal.model.Role;
import com.Capstone.EventManagementPortal.repository.EventRepository;
import com.Capstone.EventManagementPortal.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public Event createEvent(Event event) {
        if (!event.getOrganizer().getRole().equals(Role.ORGANIZER)) {
            throw new RuntimeException("Only organizers can create events!");
        }
        return eventRepository.save(event);
    }

    @Override
    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    @Override
    public List<Event> getEventsByLocation(String location) {
        return eventRepository.findByLocation(location);
    }


    @Override
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Override
    public Event updateEvent(Long id, Event event) {
        return eventRepository.findById(id)
                .map(existingEvent -> {
                    existingEvent.setTitle(event.getTitle());
                    existingEvent.setDescription(event.getDescription());
                    existingEvent.setCategory(event.getCategory());
                    existingEvent.setDateTime(event.getDateTime());
                    existingEvent.setMaxSlots(event.getMaxSlots());
                    existingEvent.setAvailableSlots(event.getAvailableSlots());
                    existingEvent.setLocation(event.getLocation());
                    return eventRepository.save(existingEvent);
                })
                .orElseThrow(() -> new RuntimeException("Event not found!"));
    }


    @Override
    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }
}
