package com.ebooking.BookUrMovie.managements.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Date;
import java.sql.Time;

@Data
@AllArgsConstructor
public class SlotDTO {

    private Time slotTime;

    private Date slotDate;

    private float slotPrice;

    private int numberOfSeats;

    private String movieName;

    private String theatreName;

}
