package com.Capstone.EventManagementPortal.controller;

import com.Capstone.EventManagementPortal.dto.BookingDTO;
import com.Capstone.EventManagementPortal.exception.UserNotFoundException;
import com.Capstone.EventManagementPortal.model.Booking;
import com.Capstone.EventManagementPortal.model.Event;
import com.Capstone.EventManagementPortal.model.User;
import com.Capstone.EventManagementPortal.security.jwt.JwtUtil;
import com.Capstone.EventManagementPortal.service.BookingService;
import com.Capstone.EventManagementPortal.service.EventService;
import com.Capstone.EventManagementPortal.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final EventService eventService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public BookingController(BookingService bookingService, EventService eventService, JwtUtil jwtUtil, UserRepository userRepository) {
        this.bookingService = bookingService;
        this.eventService = eventService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    // ✅ Create Booking (Only Attendees)
    @PostMapping
    public ResponseEntity<BookingDTO> createBooking(@RequestBody BookingDTO bookingDTO, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Booking savedBooking = bookingService.createBooking(bookingDTO, user.getId());
        BookingDTO responseDTO = new BookingDTO(savedBooking);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    // ✅ Get Booking by ID (Only the owner of the booking can view it)
    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable Long id, Authentication authentication) {
        String requesterEmail = jwtUtil.extractUsername(authentication);
        Booking booking = bookingService.getBookingById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found!"));

        // Only the owner of the booking can view it
        if (!booking.getUser().getEmail().equals(requesterEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to view this booking.");
        }

        return ResponseEntity.ok(new BookingDTO(booking));
    }

    // ✅ Get Bookings by User ID (Only the user can view their own bookings)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingDTO>> getBookingsByUserId(@PathVariable Long userId, Authentication authentication) {
        String requesterEmail = jwtUtil.extractUsername(authentication);

        System.out.println("🔍 API Request - userId: " + userId);
        System.out.println("🔍 JWT Extracted Email: " + requesterEmail);

        // Check if the authenticated user owns the bookings
        if (!bookingService.isUserBookingOwner(userId, requesterEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to view these bookings.");
        }

        List<BookingDTO> bookings = bookingService.getBookingsByUserId(userId)
                .stream()
                .map(BookingDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookings);
    }


    // ✅ Get Bookings by Event ID (Only the Organizer of the event can view its bookings)
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<BookingDTO>> getBookingsByEventId(@PathVariable Long eventId, Authentication authentication) {
        String organizerEmail = jwtUtil.extractUsername(authentication);
        Event event = eventService.getEventById(eventId);

        // Only the organizer of the event can view its bookings
        if (!event.getOrganizer().getEmail().equals(organizerEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to view bookings for this event.");
        }

        List<BookingDTO> bookings = bookingService.getBookingsByEventId(eventId)
                .stream()
                .map(BookingDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookings);
    }

    // ✅ Cancel Booking (Only the user who booked can cancel)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> cancelBooking(@PathVariable Long id, Authentication authentication) {
        String userEmail = jwtUtil.extractUsername(authentication);
        bookingService.cancelBooking(id, userEmail);
        return ResponseEntity.ok("Booking cancelled successfully!");
    }
}
