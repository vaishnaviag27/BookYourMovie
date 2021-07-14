package com.ebooking.BookUrMovie.managements.repositories;

import com.ebooking.BookUrMovie.commons.models.ObsoleteSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ObsoleteSeatRepository extends JpaRepository<ObsoleteSeat, Integer> {

    ObsoleteSeat findBySeatId(int seatId);

    @Query(value="select * from obsolete_seat where booking_id = ?1", nativeQuery = true)
    List<ObsoleteSeat> getObsoleteSeatsByBookingId(int bookingId);
}
