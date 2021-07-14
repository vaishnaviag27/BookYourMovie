package com.ebooking.BookUrMovie.managements.services;

import com.ebooking.BookUrMovie.commons.exceptions.NotFoundException;
import com.ebooking.BookUrMovie.commons.models.ObsoleteSeat;
import com.ebooking.BookUrMovie.managements.repositories.ObsoleteSeatRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ObsoleteSeatService {

    @Autowired
    private ObsoleteSeatRepository obsoleteSeatRepository;

    public ObsoleteSeat getObsoleteSeatsBySeatId(int seatId) throws NotFoundException {
        log.debug("Fetching Obsolete Seat By Seat Id: {}", seatId);
        ObsoleteSeat foundObsoleteSeat = obsoleteSeatRepository.findBySeatId(seatId);
        if (foundObsoleteSeat == null) {
            log.error("Obsolete Seat not found with id: {}", seatId);
            throw new NotFoundException("Obsolete Seat not found with id: " + seatId);
        }
        return foundObsoleteSeat;
    }

    public List<ObsoleteSeat> getObsoleteSeatsByBookingId(int bookingId) throws NotFoundException {
        log.debug("Fetching Obsolete Seats By Booking Id: {}", bookingId);
        List<ObsoleteSeat> obsoleteSeatList = obsoleteSeatRepository.getObsoleteSeatsByBookingId(bookingId);
        if (obsoleteSeatList.isEmpty()) {
            log.error("Obsolete Seats not found for Booking id: {}", bookingId);
            throw new NotFoundException("Obsolete Seats not found for Booking Id: " + bookingId);
        }
        return obsoleteSeatList;
    }
}
