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
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int seatId;

    @ManyToOne
    @JsonIgnore
    private Booking booking;

    private int seatNumber;

    @ManyToOne
    @JsonIgnore
    private Slot slot;

    public Seat(int seatNumber, Slot slot) {
        this.seatNumber = seatNumber;
        this.slot = slot;
    }
}
