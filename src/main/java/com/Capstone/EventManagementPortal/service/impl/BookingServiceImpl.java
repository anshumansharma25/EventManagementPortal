package com.Capstone.EventManagementPortal.service.impl;

import com.Capstone.EventManagementPortal.dto.BookingDTO;
import com.Capstone.EventManagementPortal.exception.EventNotFoundException;
import com.Capstone.EventManagementPortal.exception.UserNotFoundException;
import com.Capstone.EventManagementPortal.model.*;
import com.Capstone.EventManagementPortal.repository.BookingRepository;
import com.Capstone.EventManagementPortal.repository.EventRepository;
import com.Capstone.EventManagementPortal.repository.UserRepository;
import com.Capstone.EventManagementPortal.service.BookingService;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;



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

    @Transactional
    public Booking createBooking(@NotNull BookingDTO bookingDTO, Long userId) {
        Event event = eventRepository.findById(bookingDTO.getEventId())
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (event.getAvailableSlots() <= 0) {
            throw new IllegalStateException("No available slots for this event");
        }

        Booking booking = new Booking();
        booking.setEvent(event);
        booking.setUser(user);
        booking.setBookingStatus(BookingStatus.Confirmed);
        booking.setCancelled(false);
        booking.setBookingTime(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);

        event.setAvailableSlots(event.getAvailableSlots() - 1);
        eventRepository.save(event);

        return savedBooking;
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
        System.out.println("🔍 Checking ownership for userId: " + userId);

        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            System.out.println("✅ Retrieved User ID: " + user.getId());
            System.out.println("✅ Retrieved User Email: " + user.getEmail());

            return user.getEmail().equals(email);
        } else {
            System.out.println("❌ User with ID " + userId + " not found in DB!");
            return false;
        }
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
