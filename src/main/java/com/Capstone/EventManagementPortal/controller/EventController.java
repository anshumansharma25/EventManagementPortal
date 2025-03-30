package com.Capstone.EventManagementPortal.controller;

import com.Capstone.EventManagementPortal.dto.EventDTO;
import com.Capstone.EventManagementPortal.model.Event;
import com.Capstone.EventManagementPortal.security.jwt.JwtUtil;
import com.Capstone.EventManagementPortal.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<EventDTO> createEvent(@RequestBody Event event, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        if (!jwtUtil.checkOrganizerAccess(userDetails)) {
            throw new RuntimeException("Only organizers can create events.");
        }
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
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEvent(@PathVariable Long id, Authentication authentication) {
        eventService.deleteEvent(id, authentication.getName());
        return ResponseEntity.ok("Event deleted successfully.");
    }

    // ✅ Get All Events
    @GetMapping
    public ResponseEntity<List<EventDTO>> getAllEvents() {
        List<EventDTO> events = eventService.getAllEvents().stream()
                .map(EventDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(events);
    }

    // ✅ Get Event by ID
    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEventById(@PathVariable Long id) {
        Optional<Event> event = eventService.getEventById(id);

        return event
                .map(value -> ResponseEntity.ok(new EventDTO(value)))
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }
}
