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
import java.sql.Time;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Slot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int slotId;

    private Time slotTime;

    private Date slotDate;

    private float slotPrice;

    private int slotTotalSeats;

    @ManyToOne
    private Movie movie;

    @ManyToOne
    private Theatre theatre;

    @OneToMany(mappedBy = "slot", cascade = CascadeType.ALL)
    private List<Seat> seats = new ArrayList<>();

    @OneToMany(mappedBy = "slot", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Booking> bookings = new ArrayList<>();

    Boolean isOutdated = false;

    public Slot(Time slotTime, Date slotDate, float slotPrice, int slotTotalSeats, Movie movie, Theatre theatre) {
        this.slotTime = slotTime;
        this.slotDate = slotDate;
        this.slotPrice = slotPrice;
        this.slotTotalSeats = slotTotalSeats;
        this.movie = movie;
        this.theatre = theatre;
    }
}
