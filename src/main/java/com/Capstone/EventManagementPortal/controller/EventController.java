package com.Capstone.EventManagementPortal.controller;

import com.Capstone.EventManagementPortal.dto.EventDTO;
import com.Capstone.EventManagementPortal.dto.EventUpdateDTO;
import com.Capstone.EventManagementPortal.exception.EventNotFoundException;
import com.Capstone.EventManagementPortal.model.Event;
import com.Capstone.EventManagementPortal.security.jwt.JwtUtil;
import com.Capstone.EventManagementPortal.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;
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
    public ResponseEntity<?> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventUpdateDTO eventDetails,
            Authentication authentication) {

        try {
            Event updatedEvent = eventService.updateEvent(id, eventDetails, authentication.getName());
            return ResponseEntity.ok(new EventDTO(updatedEvent));
        } catch (EventNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
    // ✅ Delete Event (Only Organizer of the Event)
//    @DeleteMapping("/{id}")
//    public ResponseEntity<String> deleteEvent(@PathVariable Long id, Authentication authentication) {
//        eventService.deleteEvent(id, authentication.getName());
//        return ResponseEntity.ok("Event deleted successfully.");
//    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @DeleteMapping("/{eventId}")
    public ResponseEntity<?> cancelEvent(
            @PathVariable Long eventId,
            Authentication authentication) {

        try {
            String userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
            eventService.cancelEvent(eventId, userEmail);

            return ResponseEntity.ok(Map.of(
                    "message", "Event cancelled successfully",
                    "eventId", eventId,
                    "isCancelled", true
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Cancellation failed",
                            "message", e.getMessage()
                    ));
        }
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
