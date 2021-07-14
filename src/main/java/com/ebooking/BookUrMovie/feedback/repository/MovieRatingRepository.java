package com.ebooking.BookUrMovie.feedback.repository;

import com.ebooking.BookUrMovie.commons.models.Booking;
import com.ebooking.BookUrMovie.commons.models.MovieRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MovieRatingRepository extends JpaRepository<MovieRating, Integer> {
  Optional<MovieRating> findByBooking(Booking booking);
}
