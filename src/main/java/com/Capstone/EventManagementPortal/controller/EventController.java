package com.Capstone.EventManagementPortal.controller;

import com.Capstone.EventManagementPortal.dto.EventDTO;
import com.Capstone.EventManagementPortal.model.Event;
import com.Capstone.EventManagementPortal.security.jwt.JwtUtil;
import com.Capstone.EventManagementPortal.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final JwtUtil jwtUtil;

    public EventController(EventService eventService, JwtUtil jwtUtil) {
        this.eventService = eventService;
        this.jwtUtil = jwtUtil;
    }

    // ✅ Create Event (Only Organizers)
    @PostMapping("/create")
    public @ResponseBody  ResponseEntity<EventDTO> createEvent(@RequestBody Event event, Authentication authentication) throws AccessDeniedException {
        jwtUtil.checkOrganizerAccess(authentication); // ✅ Fixed
        Event createdEvent = eventService.createEvent(event, authentication.getName());
        return ResponseEntity.ok(new EventDTO(createdEvent));
    }



    // ✅ Update Event (Only Organizer of the Event)
    @PutMapping("/{id}")
    public ResponseEntity<EventDTO> updateEvent(@PathVariable Long id, @RequestBody Event eventDetails, Authentication authentication) {
        Optional<Event> updatedEvent = eventService.updateEvent(id, eventDetails, authentication.getName());

        return updatedEvent
                .map(event -> ResponseEntity.ok(new EventDTO(event)))
                .orElseThrow(() -> new RuntimeException("Event not found or update failed"));
    }

    // ✅ Delete Event (Only Organizer of the Event)
//    @DeleteMapping("/{id}")
//    public ResponseEntity<String> deleteEvent(@PathVariable Long id, Authentication authentication) {
//        eventService.deleteEvent(id, authentication.getName());
//        return ResponseEntity.ok("Event deleted successfully.");
//    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<String> cancelEvent(
            @PathVariable Long eventId,
            Authentication authentication
    ) {
        eventService.cancelEvent(eventId, authentication.getName());
        return ResponseEntity.ok("Event and all associated bookings cancelled");
    }


    @GetMapping
    public ResponseEntity<List<EventDTO>> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        List<EventDTO> eventDTOs = events.stream()
                .map(EventDTO::new)  // This will use your constructor
                .collect(Collectors.toList());
        return ResponseEntity.ok(eventDTOs);
    }

    @PreAuthorize("hasAuthority('ORGANIZER')")
    @GetMapping("/organizer")
    public ResponseEntity<List<EventDTO>> getOrganizerEvents(Authentication authentication) {
        String organizerEmail = authentication.getName(); // Get logged-in organizer's email
        List<EventDTO> events = eventService.getEventsByOrganizer(organizerEmail);
        return ResponseEntity.ok(events);
    }


    // ✅ Get Event by ID
    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEventById(@PathVariable Long id) {
        Optional<Event> eventOpt = Optional.ofNullable(eventService.getEventById(id));
        if (eventOpt.isPresent()) {
            Event event = eventOpt.get();
            return ResponseEntity.ok(new EventDTO(event));
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        }

    }
}
