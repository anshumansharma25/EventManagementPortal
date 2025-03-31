package com.Capstone.EventManagementPortal.controller;

import com.Capstone.EventManagementPortal.dto.BookingDTO;
import com.Capstone.EventManagementPortal.model.Booking;
import com.Capstone.EventManagementPortal.model.Event;
import com.Capstone.EventManagementPortal.model.Role;
import com.Capstone.EventManagementPortal.security.jwt.JwtUtil;
import com.Capstone.EventManagementPortal.service.BookingService;
import com.Capstone.EventManagementPortal.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final EventService eventService;
    private final JwtUtil jwtUtil;

    public BookingController(BookingService bookingService, EventService eventService, JwtUtil jwtUtil) {
        this.bookingService = bookingService;
        this.eventService = eventService;
        this.jwtUtil = jwtUtil;
    }

    // ✅ Create Booking (Only Attendees)
    @PostMapping
    public ResponseEntity<BookingDTO> createBooking(@RequestBody BookingDTO bookingDTO, Authentication authentication) {
        String userEmail = jwtUtil.extractUsername(authentication);  // ✅ Extract user email
        String token = authentication.getCredentials().toString();  // ✅ Extract token
        String roleString = jwtUtil.extractRoleFromToken(token);  // ✅ Extract role from token
        Role userRole = Role.valueOf(roleString);  // ✅ Convert to Role enum


        if (userRole != Role.ATTENDEE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only attendees can book events.");
        }

        Event event = eventService.getEventById(bookingDTO.getEventId());

        Booking newBooking = bookingDTO.toEntity(null, event);
        newBooking = bookingService.createBooking(newBooking, userEmail);
        return ResponseEntity.ok(new BookingDTO(newBooking));
    }

    // ✅ Get Booking by ID
    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable Long id) {
        Booking booking = bookingService.getBookingById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found!"));
        return ResponseEntity.ok(new BookingDTO(booking));
    }

    // ✅ Get All Bookings (Only Admin)
    @GetMapping
    public ResponseEntity<List<BookingDTO>> getAllBookings(Authentication authentication) throws AccessDeniedException {
        jwtUtil.checkAdminAccess(authentication);
        List<BookingDTO> bookings = bookingService.getAllBookings()
                .stream()
                .map(BookingDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookings);
    }

    // ✅ Get Bookings by User ID (Only Admin or the User)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingDTO>> getBookingsByUserId(@PathVariable Long userId, Authentication authentication) {
        String requesterEmail = jwtUtil.extractUsername(authentication);
        String token = authentication.getCredentials().toString();  // ✅ Extract token from Authentication
        String roleString = jwtUtil.extractRoleFromToken(token);  // ✅ Get role from token
        Role requesterRole = Role.valueOf(roleString);  // ✅ Convert string to Role Enum


        if (requesterRole != Role.ADMIN && !bookingService.isUserBookingOwner(userId, requesterEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to view this user's bookings.");
        }

        List<BookingDTO> bookings = bookingService.getBookingsByUserId(userId)
                .stream()
                .map(BookingDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookings);
    }

    // ✅ Get Bookings by Event ID
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<BookingDTO>> getBookingsByEventId(@PathVariable Long eventId) {
        List<BookingDTO> bookings = bookingService.getBookingsByEventId(eventId)
                .stream()
                .map(BookingDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookings);
    }

    // ✅ Cancel Booking (Only the user who booked can cancel)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> cancelBooking(@PathVariable Long id, Authentication authentication) {
        String userEmail = jwtUtil.extractUsername(authentication); // ✅ Extracts email
        bookingService.cancelBooking(id, userEmail);
        return ResponseEntity.ok("Booking cancelled successfully!");
    }
}
