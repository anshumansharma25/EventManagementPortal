package com.Capstone.EventManagementPortal.service;

import com.Capstone.EventManagementPortal.model.Booking;
import java.util.List;
import java.util.Optional;

public interface BookingService {
    Booking createBooking(Booking booking);
    Optional<Booking> getBookingById(Long id);
    List<Booking> getAllBookings();
    void cancelBooking(Long id);
}
