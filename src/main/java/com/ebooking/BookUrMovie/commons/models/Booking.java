package com.ebooking.BookUrMovie.commons.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor

public class Booking {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int bookingId;

  @ManyToOne
  @JsonIgnore
  private User user;

  private String movieName;

  private String theatreName;

  private float bookingAmount;

  private int seatCount;

  private boolean isCancelled = false;

  @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
  private List<Seat> seatList = new ArrayList<>();

  @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
  private List<BookedAddon> addonList = new ArrayList<>();

  @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
  private MovieRating movieRating;

  @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
  private TheatreRating theatreRating;

  @ManyToOne
  private Slot slot;

  public Booking(
      Slot slot,
      User user,
      String movieName,
      String theatreName,
      float bookingAmount,
      int seatCount
  ) {
    this.slot = slot;
    this.user = user;
    this.movieName = movieName;
    this.theatreName = theatreName;
    this.bookingAmount = bookingAmount;
    this.seatCount = seatCount;
  }
}
