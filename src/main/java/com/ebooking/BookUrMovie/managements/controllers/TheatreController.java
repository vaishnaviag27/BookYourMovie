package com.ebooking.BookUrMovie.managements.controllers;

import com.ebooking.BookUrMovie.commons.exceptions.BadDetailsException;
import com.ebooking.BookUrMovie.commons.exceptions.NotFoundException;
import com.ebooking.BookUrMovie.commons.models.Theatre;
import com.ebooking.BookUrMovie.managements.services.TheatreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/theatres")
public class TheatreController {
  @Autowired
  TheatreService theatreService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Object> getAllTheatres() {
        try {
            return new ResponseEntity<>(theatreService.listTheatres(), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<Object> getAllTheatresByLocation(HttpServletRequest request) {
        try {
            return new ResponseEntity<>(theatreService.listTheatresByLocation(request), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }


    @GetMapping("/{theatreId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Object> getTheatreById(@PathVariable("theatreId") int theatreId) {
        try {
            return new ResponseEntity<>(theatreService.findByTheatreId(theatreId), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{theatreName}/details")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<Object> getTheatreByName(@PathVariable("theatreName") String theatreName) {
        try {
            return new ResponseEntity<>(theatreService.findByTheatreName(theatreName), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{theatreName}/slots")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Object> getTheatreSlots(@PathVariable("theatreName") String theatreName) {
        try {
            return new ResponseEntity<>(theatreService.findSlotsByTheatreName(theatreName), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> addTheatre(@RequestBody Theatre newTheatre) {
        try {
            return new ResponseEntity<>(theatreService.addTheatre(newTheatre), HttpStatus.ACCEPTED);
        } catch (BadDetailsException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @PutMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> updateTheatre(@RequestBody Theatre updatedTheatre) {
        try {
            return new ResponseEntity<>(theatreService.updateTheatre(updatedTheatre), HttpStatus.OK);
        } catch (BadDetailsException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{theatreId}/rating/{rating}")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<String> updateTheatreRating(@PathVariable("theatreId") int theatreId, @PathVariable("rating") float theatreRating) {
        try {
            return new ResponseEntity<>(theatreService.updateTheatreRating(theatreId, theatreRating), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
