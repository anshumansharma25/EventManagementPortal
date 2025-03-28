package com.Capstone.EventManagementPortal.service.impl;

import com.Capstone.EventManagementPortal.model.Booking;
import com.Capstone.EventManagementPortal.repository.BookingRepository;
import com.Capstone.EventManagementPortal.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public Booking createBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    @Override
    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    public void cancelBooking(Long id) {
        bookingRepository.deleteById(id);
    }
}
