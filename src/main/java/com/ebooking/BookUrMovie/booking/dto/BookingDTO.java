package com.ebooking.BookUrMovie.booking.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BookingDTO {
  private boolean isCancelled = false;

  private int slotId;

  private List<Integer> seatNumberList = new ArrayList<>();

  private List<Integer> addonList = new ArrayList<>();
}
