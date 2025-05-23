package com.Capstone.EventManagementPortal.service.impl;

import com.Capstone.EventManagementPortal.dto.EventDTO;
import com.Capstone.EventManagementPortal.dto.EventUpdateDTO;
import com.Capstone.EventManagementPortal.exception.EventNotFoundException;
import com.Capstone.EventManagementPortal.exception.ResourceNotFoundException;
import com.Capstone.EventManagementPortal.exception.UnauthorizedException;
import com.Capstone.EventManagementPortal.exception.UserNotFoundException;
import org.springframework.security.access.AccessDeniedException;
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

    @Transactional
    @Override
    public Event updateEvent(Long eventId, EventUpdateDTO updateDTO, String organizerEmail) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));


        if (!event.getOrganizer().getEmail().equals(organizerEmail)) {
            throw new AccessDeniedException("Only the event organizer can update this event");
        }

        // Get current bookings count safely
        int currentBookings = event.getBookings() != null ? event.getBookings().size() : 0;

        // Validate max slots
        if (updateDTO.getMaxSlots() < currentBookings) {
            throw new IllegalStateException("Cannot reduce max slots below current bookings (" +
                    currentBookings + ")");
        }

        // Calculate available slots
        int availableSlots = updateDTO.getMaxSlots() - currentBookings;

        // Update fields
        event.setTitle(updateDTO.getTitle());
        event.setDescription(updateDTO.getDescription());
        event.setCategory(updateDTO.getCategory());
        event.setDateTime(updateDTO.getDateTime());
        event.setMaxSlots(updateDTO.getMaxSlots());
        event.setAvailableSlots(availableSlots);
        event.setLocation(updateDTO.getLocation());

        return eventRepository.save(event);
    }

    @Transactional
    @Override
    public void cancelEvent(Long eventId, String userEmail) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Debug organizer check
        System.out.println("Current user: " + userEmail);
        System.out.println("Event organizer: " + event.getOrganizer().getEmail());

        if (!event.getOrganizer().getEmail().equalsIgnoreCase(userEmail)) {
            throw new RuntimeException("Only organizer can cancel event");
        }

        // Update event
        event.setCancelled(true);
        event.setAvailableSlots(0);
        eventRepository.save(event);

        // Update bookings using enum
        bookingRepository.updateBookingsForCancelledEvent(
                eventId,
                BookingStatus.Event_cancelled  // Pass enum value directly
        );
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
