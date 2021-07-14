package com.ebooking.BookUrMovie.commons.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
@Data
@NoArgsConstructor
public class BookedAddon {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int bookedAddonId;

  @ManyToOne
  @JsonIgnore
  private Booking booking;

  @ManyToOne
  @JsonIgnore
  private AddOn addOn;

  private int addonQuantity;

  private float totalAmount;

  public BookedAddon(Booking booking, AddOn addOn, int addonQuantity, float totalAmount) {
    this.booking = booking;
    this.addOn = addOn;
    this.addonQuantity = addonQuantity;
    this.totalAmount = totalAmount;
  }
}
