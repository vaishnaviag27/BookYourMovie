package com.ebooking.BookUrMovie.managements.controllers;

import com.ebooking.BookUrMovie.commons.exceptions.BadDetailsException;
import com.ebooking.BookUrMovie.commons.exceptions.NotFoundException;
import com.ebooking.BookUrMovie.commons.models.Slot;
import com.ebooking.BookUrMovie.managements.dtos.SlotDTO;
import com.ebooking.BookUrMovie.managements.services.SlotService;
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
import java.util.List;

@RestController
@RequestMapping("/slots")
public class SlotController {

    @Autowired
    SlotService slotService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Object> getAllSlots() {
        try {
            return new ResponseEntity<>(slotService.getAllSlots(), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<Object> getAllSlotsByLocation(HttpServletRequest request) {
        try {
            return new ResponseEntity<>(slotService.getAllSlotsByLocation(request), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{slotId}")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<Object> getSlot(@PathVariable("slotId") int slotId) {
        try {
            return new ResponseEntity<>(slotService.getBySlotId(slotId), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/movies/{movieName}")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<Object> getSlotsByMovieName(@PathVariable("movieName") String movieName, HttpServletRequest request) {
        try {
            return new ResponseEntity<>(slotService.getSlotsByMovieName(movieName, request), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{movieName}/{theatreName}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Object> getSlotByMovieNameAndTheatreName(@PathVariable("movieName") String movieName,
                                                                   @PathVariable("theatreName") String theatreName) {
        List<Slot> foundSlots;
        try {
            foundSlots = slotService.findSlotByMovieNameAndTheatreName(movieName, theatreName);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(foundSlots, HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> addSlot(@RequestBody SlotDTO newSlotDTO) {
        try {
            return new ResponseEntity<>(slotService.addSlot(newSlotDTO), HttpStatus.OK);
        } catch (BadDetailsException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{slotId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Object> deleteSlot(@PathVariable("slotId") int slotId, HttpServletRequest request) {
        try {
            return new ResponseEntity<>(slotService.deleteBySlotId(slotId, request), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        } catch(IllegalArgumentException exception){
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete-outdated")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String invalidateSlots() {
        return slotService.findAndDeleteOutdatedSlots();
    }
}
