package com.Capstone.EventManagementPortal.controller;

import com.Capstone.EventManagementPortal.dto.BookingDTO;
import com.Capstone.EventManagementPortal.model.Booking;
import com.Capstone.EventManagementPortal.model.Event;
import com.Capstone.EventManagementPortal.model.Role;
import com.Capstone.EventManagementPortal.model.User;
import com.Capstone.EventManagementPortal.security.jwt.JwtUtil;
import com.Capstone.EventManagementPortal.service.BookingService;
import com.Capstone.EventManagementPortal.service.EventService;
import com.Capstone.EventManagementPortal.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;
    private final EventService eventService;  // ✅ Added EventService
    private final JwtUtil jwtUtil;

    public BookingController(BookingService bookingService, UserService userService, EventService eventService, JwtUtil jwtUtil) {
        this.bookingService = bookingService;
        this.userService = userService;
        this.eventService = eventService;
        this.jwtUtil = jwtUtil;
    }

    // ✅ Create Booking (Only Attendees)
    @PostMapping
    public ResponseEntity<BookingDTO> createBooking(@RequestBody BookingDTO bookingDTO, Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userService.getUserByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        if (user.getRole() != Role.ATTENDEE) {
            throw new RuntimeException("Only attendees can book events.");
        }

        // ✅ Corrected: Use eventService to fetch the event
        Event event = eventService.getEventById(bookingDTO.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found!"));

        Booking newBooking = bookingDTO.toEntity(user, event);
        newBooking = bookingService.createBooking(newBooking, userEmail);
        return ResponseEntity.ok(new BookingDTO(newBooking));
    }

    // ✅ Get Booking by ID
    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable Long id) {
        Booking booking = bookingService.getBookingById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found!"));
        return ResponseEntity.ok(new BookingDTO(booking));
    }

    // ✅ Get All Bookings (Only Admin)
    @GetMapping
    public ResponseEntity<List<BookingDTO>> getAllBookings(Authentication authentication) {
        jwtUtil.checkAdminAccess(authentication);
        List<BookingDTO> bookings = bookingService.getAllBookings()
                .stream()
                .map(BookingDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookings);
    }

    // ✅ Get Bookings by User ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingDTO>> getBookingsByUserId(@PathVariable Long userId) {
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
        String userEmail = authentication.getName();
        bookingService.cancelBooking(id, userEmail);
        return ResponseEntity.ok("Booking cancelled successfully!");
    }
}
