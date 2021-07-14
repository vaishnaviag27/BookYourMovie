package com.ebooking.BookUrMovie.booking.repositories;

import com.ebooking.BookUrMovie.commons.models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    Booking findByBookingId(int id);
}
