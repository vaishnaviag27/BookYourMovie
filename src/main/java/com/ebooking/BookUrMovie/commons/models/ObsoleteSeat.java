package com.ebooking.BookUrMovie.commons.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Date;
import java.sql.Time;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObsoleteSeat {

    @Id
    int seatId;

    int seatNumber;

    int bookingId;

    int slotId;

    Date slotDate;

    Time slotTime;

    String movieName;

    String theatreName;
}
