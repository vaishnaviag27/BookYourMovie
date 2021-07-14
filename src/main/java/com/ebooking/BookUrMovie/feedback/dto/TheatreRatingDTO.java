package com.ebooking.BookUrMovie.feedback.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TheatreRatingDTO {

  private int bookingId;

  private int rating;
}
