package com.Capstone.EventManagementPortal.service;

import com.Capstone.EventManagementPortal.model.Booking;
import java.util.List;
import java.util.Optional;

public interface BookingService {
    public Booking createBooking(Booking booking, String userEmail);
    Optional<Booking> getBookingById(Long id);
    List<Booking> getAllBookings();
    List<Booking> getBookingsByUserId(Long userId);  // ✅ Added method
    List<Booking> getBookingsByEventId(Long eventId); // ✅ Added method
    void cancelBooking(Long bookingId, String userEmail);
    boolean isUserBookingOwner(Long userId, String email);

}
