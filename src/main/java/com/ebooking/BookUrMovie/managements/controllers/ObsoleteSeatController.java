package com.ebooking.BookUrMovie.managements.controllers;

import com.ebooking.BookUrMovie.commons.exceptions.NotFoundException;
import com.ebooking.BookUrMovie.managements.services.ObsoleteSeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/obsolete-seats")
public class ObsoleteSeatController {

    @Autowired
    ObsoleteSeatService obsoleteSeatService;

    @GetMapping("/{seatId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Object> getObsoleteSeatBySeatId(@PathVariable("seatId") int seatId) {
        try {
            return new ResponseEntity<>(obsoleteSeatService.getObsoleteSeatsBySeatId(seatId), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Object> getObsoleteSeatsByBookingId(@PathVariable("bookingId") int bookingId) {
        try {
            return new ResponseEntity<>(obsoleteSeatService.getObsoleteSeatsByBookingId(bookingId), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
