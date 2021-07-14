package com.ebooking.BookUrMovie.managements.controllers;

import com.ebooking.BookUrMovie.commons.exceptions.NotFoundException;
import com.ebooking.BookUrMovie.managements.services.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/seats")
public class SeatController {
    @Autowired
    SeatService seatService;

    @GetMapping("/{seatId}")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<Object> getSeatBySeatId(@PathVariable("seatId") int seatId) {
        try {
            return new ResponseEntity<>(seatService.getSeatBySeatId(seatId), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{seatId}/booking/available")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<Object> isSeatBooked(@PathVariable("seatId") int seatId) {
        try {
            return new ResponseEntity<>(seatService.isSeatBooked(seatId), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{seatId}/{bookingId}")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<String> updateSeatById(@PathVariable("seatId") int seatId,
                                                 @PathVariable("bookingId") int bookingId,
                                                 HttpServletRequest request) {
        try {
            return new ResponseEntity<>(seatService.updateSeatById(seatId, bookingId, request), HttpStatus.ACCEPTED);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        } catch (URISyntaxException exception) {
            return new ResponseEntity<>("Bookings Service is Down", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @DeleteMapping("/{seatId}/booking/available")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public void vacateSeatBySeatId(@PathVariable("seatId") int seatId) {
        seatService.vacateSeatBySeatId(seatId);
    }
}
