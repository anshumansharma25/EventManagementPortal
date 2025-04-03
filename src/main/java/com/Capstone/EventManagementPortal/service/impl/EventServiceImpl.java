package com.Capstone.EventManagementPortal.service.impl;

import com.Capstone.EventManagementPortal.dto.EventDTO;
import com.Capstone.EventManagementPortal.exception.EventNotFoundException;
import com.Capstone.EventManagementPortal.exception.UnauthorizedException;
import com.Capstone.EventManagementPortal.exception.UserNotFoundException;
import com.Capstone.EventManagementPortal.model.Booking;
import com.Capstone.EventManagementPortal.model.BookingStatus;
import com.Capstone.EventManagementPortal.model.Event;
import com.Capstone.EventManagementPortal.model.User;
import com.Capstone.EventManagementPortal.repository.BookingRepository;
import com.Capstone.EventManagementPortal.repository.EventRepository;
import com.Capstone.EventManagementPortal.repository.UserRepository;
import com.Capstone.EventManagementPortal.service.EventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public EventServiceImpl(EventRepository eventRepository, UserRepository userRepository, BookingRepository bookingRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
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

    @Transactional
    @Override
    public void cancelEvent(Long eventId, String organizerEmail) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        if (!event.getOrganizer().getEmail().equals(organizerEmail)) {
            throw new UnauthorizedException("Only the organizer can cancel this event");
        }

        // Mark event as cancelled
        event.setCancelled(true);
        eventRepository.save(event);

        bookingRepository.findByEventId(eventId).forEach(booking -> {
            booking.setStatus(BookingStatus.Event_cancelled);
            booking.setCancelled(true);
            bookingRepository.save(booking);
        });
    }

    @Override
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Override
    public List<EventDTO> getEventsByOrganizer(String email) {
        User organizer = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Organizer not found"));

        List<Event> events = eventRepository.findByOrganizer_Id(organizer.getId()); // Fetch events
        return events.stream()
                .map(EventDTO::new) // Convert Entity to DTO
                .collect(Collectors.toList());
    }



    @Override
    public Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));
    }


}
