package com.Capstone.EventManagementPortal.controller;

import com.Capstone.EventManagementPortal.model.Event;
import com.Capstone.EventManagementPortal.model.Role;
import com.Capstone.EventManagementPortal.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // 1️⃣ Create a new event
    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody Event event) {
        // Ensure the user creating the event is an ORGANIZER
        if (!event.getOrganizer().getRole().equals(Role.ORGANIZER)) {
            return ResponseEntity.badRequest().body("Only organizers can create events!");
        }
        Event createdEvent = eventService.createEvent(event);
        return ResponseEntity.ok(createdEvent);
    }


    // 2️⃣ Get all events
    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    // 3️⃣ Get event by ID
    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        Optional<Event> event = eventService.getEventById(id);
        return event.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 4️⃣ Get events by location
    @GetMapping("/location/{location}")
    public ResponseEntity<List<Event>> getEventsByLocation(@PathVariable String location) {
        return ResponseEntity.ok(eventService.getEventsByLocation(location));
    }

    // 5️⃣ Update an event
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @RequestBody Event eventDetails) {
        Optional<Event> existingEvent = eventService.getEventById(id);
        if (existingEvent.isEmpty()) {
            return ResponseEntity.status(404).body("Event not found!");
        }
        Event updatedEvent = eventService.updateEvent(id, eventDetails);
        return ResponseEntity.ok(updatedEvent);
    }

    // 6️⃣ Delete an event
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        Optional<Event> event = eventService.getEventById(id);

        if (event.isEmpty()) {
            return ResponseEntity.status(404).body("Event not found!");
        }

        // Ensure only the event organizer can delete the event
        if (!event.get().getOrganizer().getRole().equals(Role.ORGANIZER)) {
            return ResponseEntity.status(403).body("You can only delete events you organized!");
        }

        eventService.deleteEvent(id);
        return ResponseEntity.ok("Event deleted successfully!");
    }

}
