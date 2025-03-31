package com.Capstone.EventManagementPortal.service.impl;

import com.Capstone.EventManagementPortal.model.*;
import com.Capstone.EventManagementPortal.repository.BookingRepository;
import com.Capstone.EventManagementPortal.repository.EventRepository;
import com.Capstone.EventManagementPortal.repository.UserRepository;
import com.Capstone.EventManagementPortal.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository, UserRepository userRepository,
                              EventRepository eventRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    // ✅ 1️⃣ Create a booking (Only ATTENDEES can book)
    @Override
    public Booking createBooking(Booking booking, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        Event event = eventRepository.findById(booking.getEvent().getId())
                .orElseThrow(() -> new RuntimeException("Event not found!"));

        if (event.getAvailableSlots() <= 0) {
            throw new RuntimeException("No available slots for this event.");
        }

        if (!user.getRole().equals(Role.ATTENDEE)) {
            throw new RuntimeException("Only attendees can book events.");
        }

        // Create and save booking
        booking.setUser(user);
        booking.setEvent(event);
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setBookingTime(LocalDateTime.now());

        // Reduce available slots for the event
        event.setAvailableSlots(event.getAvailableSlots() - 1);

        // ✅ Directly save the updated event (instead of calling updateEvent)
        eventRepository.save(event);

        return bookingRepository.save(booking);
    }

    // ✅ 2️⃣ Get a booking by ID
    @Override
    public Optional<Booking> getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId);
    }

    // ✅ 3️⃣ Get all bookings
    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // ✅ 4️⃣ Get bookings by User ID
    @Override
    public List<Booking> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    // ✅ 5️⃣ Get bookings by Event ID
    @Override
    public List<Booking> getBookingsByEventId(Long eventId) {
        return bookingRepository.findByEventId(eventId);
    }

    @Override
    public boolean isUserBookingOwner(Long userId, String email) {
        return userRepository.findById(userId)
                .map(user -> user.getEmail().equals(email))
                .orElse(false);
    }


    // ✅ 6️⃣ Cancel a booking (Only the user who booked can cancel)
    @Override
    public void cancelBooking(Long bookingId, String userEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found!"));

        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("You can only cancel your own bookings.");
        }

        // Free up the slot in the event
        Event event = booking.getEvent();
        event.setAvailableSlots(event.getAvailableSlots() + 1);

        // ✅ Directly save the updated event (instead of calling updateEvent)
        eventRepository.save(event);

        // Delete the booking
        bookingRepository.delete(booking);



    }
}
