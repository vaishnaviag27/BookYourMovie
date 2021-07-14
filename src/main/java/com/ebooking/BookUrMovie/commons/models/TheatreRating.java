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
public class TheatreRating {

  public static final int MAX_THEATRE_RATING = 5;

  public static final int MIN_THEATRE_RATING = 1;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int theatreRatingId;

  @OneToOne
  @JsonIgnore
  private Booking booking;

  @ManyToOne
  @JsonIgnore
  private Theatre theatre;

  private int rating;

  public TheatreRating(Booking booking, Theatre theatre, int rating) {
    this.booking = booking;
    this.theatre = theatre;
    this.rating = rating;
  }
}
