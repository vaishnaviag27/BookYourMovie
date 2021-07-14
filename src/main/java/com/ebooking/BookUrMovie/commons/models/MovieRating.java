package com.ebooking.BookUrMovie.commons.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
@Data
@NoArgsConstructor
public class MovieRating {

  public static final int MAX_MOVIE_RATING = 5;

  public static final int MIN_MOVIE_RATING = 1;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int movieRatingId;

  @OneToOne
  @JsonIgnore
  private Booking booking;

  @ManyToOne
  @JsonIgnore
  private Movie movie;

  private int rating;

  public MovieRating(Booking booking, Movie movie, int rating) {
    this.booking = booking;
    this.movie = movie;
    this.rating = rating;
  }
}
