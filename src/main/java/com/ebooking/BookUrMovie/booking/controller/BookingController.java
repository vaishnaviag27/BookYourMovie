package com.ebooking.BookUrMovie.booking.controller;

import com.ebooking.BookUrMovie.booking.dto.BookingDTO;
import com.ebooking.BookUrMovie.booking.services.BookingService;
import com.ebooking.BookUrMovie.commons.exceptions.BadDetailsException;
import com.ebooking.BookUrMovie.commons.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

;
import javax.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;
import java.security.Principal;

@RestController
@Slf4j
@RequestMapping("/bookings")
public class BookingController {
    @Autowired
    private BookingService service;

    // WIP - make separate for user and admin
    @GetMapping("/{bookingId}")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<Object> getBookingsByBookingId(@PathVariable("bookingId") int id) {
        try {
            return new ResponseEntity<>(service.getBookingsByBookingId(id), HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/user/{bookingId}")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<Object> getUserByBookingId(@PathVariable("bookingId") int id) {
        try {
            return new ResponseEntity<>(service.getUserByBookingId(id), HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Object> getAllBookings() {
        try {
            return new ResponseEntity<>(service.getAllBookings(), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<Object> addBooking(@RequestBody BookingDTO newBooking, Principal principal, HttpServletRequest request) {
        try {
            return new ResponseEntity<>(service.addBooking(newBooking, principal, request), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        } catch (BadDetailsException exp) {
            return new ResponseEntity<>(exp.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (NullPointerException pointerException) {
            return new ResponseEntity<>(pointerException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (URISyntaxException e) {
            return new ResponseEntity<>("Something went wrong while adding booking", HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{bookingId}")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<Object> cancelBooking(@PathVariable("bookingId") int bookingId,
                                                Principal principal,
                                                HttpServletRequest request) {
        try {
            return new ResponseEntity<>(service.cancelBooking(bookingId, principal, request), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException exp) {
            return new ResponseEntity<>(exp.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (URISyntaxException e) {
            log.error("Something went wrong", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
