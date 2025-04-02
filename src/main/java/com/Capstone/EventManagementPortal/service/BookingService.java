package com.Capstone.EventManagementPortal.service;

import com.Capstone.EventManagementPortal.dto.BookingDTO;
import com.Capstone.EventManagementPortal.model.Booking;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Optional;

public interface BookingService {
    public Booking createBooking(@NotNull BookingDTO bookingDTO, Long userId);
    Optional<Booking> getBookingById(Long id);
    List<Booking> getAllBookings();
    List<Booking> getBookingsByUserId(Long userId);  // ✅ Added method
    List<Booking> getBookingsByEventId(Long eventId); // ✅ Added method
    void cancelBooking(Long bookingId, String userEmail);
    boolean isUserBookingOwner(Long userId, String email);

}
