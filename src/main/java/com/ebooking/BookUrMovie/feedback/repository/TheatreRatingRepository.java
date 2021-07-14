package com.ebooking.BookUrMovie.feedback.repository;

import com.ebooking.BookUrMovie.commons.models.Booking;
import com.ebooking.BookUrMovie.commons.models.TheatreRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TheatreRatingRepository extends JpaRepository<TheatreRating, Integer> {
  Optional<TheatreRating> findByBooking(Booking booking);
}
