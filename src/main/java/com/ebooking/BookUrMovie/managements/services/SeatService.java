package com.ebooking.BookUrMovie.managements.services;

import com.ebooking.BookUrMovie.commons.exceptions.NotFoundException;
import com.ebooking.BookUrMovie.commons.models.Booking;
import com.ebooking.BookUrMovie.commons.models.Seat;
import com.ebooking.BookUrMovie.commons.services.CommunicationService;
import com.ebooking.BookUrMovie.managements.repositories.SeatRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

import javax.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;

@Service
@Slf4j
public class SeatService {

    @Autowired
    SeatRepository seatRepository;

    public Seat getSeatBySeatId(int seatId) throws NotFoundException {
        log.debug("Fetching Seat by Id: {}", seatId);
        Seat foundSeat = seatRepository.findBySeatId(seatId);
        if (foundSeat == null) {
            log.error("Seat not found with Id: {}", seatId);
            throw new NotFoundException("Seat not found with Id: " + seatId);
        }
        return foundSeat;
    }

    public String updateSeatById(int seatId, int bookingId, HttpServletRequest request) throws NotFoundException, URISyntaxException {
        log.debug("Updating Seat with Id: {} with booking Id: {}", seatId, bookingId);
        ObjectMapper mapper = new ObjectMapper();
        Booking foundBooking;
        try {
            foundBooking = mapper.convertValue(CommunicationService.RequestMirror(
                null,
                HttpMethod.GET,
                request,
                "/bookings/" + bookingId),
                Booking.class);
        } catch (HttpServerErrorException exception) {
            log.error("No Booking found with Id: {}", bookingId);
            throw new NotFoundException("Booking not found with Id: " + bookingId);
        }
        Seat seat = getSeatBySeatId(seatId);
        seat.setBooking(foundBooking);
        seatRepository.save(seat);
        log.debug("Updated Seat with Id: {} with booking Id: {} successfully", seatId, bookingId);
        return "Seat List has been Updated finally!";
    }

    public boolean isSeatBooked(int seatId) throws NotFoundException {
        log.debug("Checking Seat avaiability for seatId: {}", seatId);
        Seat foundSeat = getSeatBySeatId(seatId);
        if (foundSeat.getBooking() != null) {
            log.debug("Seat with Id: {} is not available.", seatId);
            return true;
        }
        log.debug("Seat with Id: {} is available.", seatId);
        return false;
    }

    public void vacateSeatBySeatId(int seatId) {
        log.debug("Vacating Seat with Id: {}", seatId);
        try {
            Seat foundSeat = getSeatBySeatId(seatId);
            foundSeat.setBooking(null);
            seatRepository.save(foundSeat);
            log.debug("Vacated Seat with Id: {}", seatId);
        } catch (NotFoundException exception) {
            log.error("Seat with id: " + seatId + " not found.");
        }
    }
}
