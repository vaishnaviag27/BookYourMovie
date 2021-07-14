package com.ebooking.BookUrMovie.managements.controllers;

import com.ebooking.BookUrMovie.commons.exceptions.BadDetailsException;
import com.ebooking.BookUrMovie.commons.exceptions.NotFoundException;
import com.ebooking.BookUrMovie.commons.models.AddOn;
import com.ebooking.BookUrMovie.managements.services.AddonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

;

@RestController
@RequestMapping("/addons")
@Slf4j
public class AddonController {
    @Autowired
    AddonService addonService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<Object> getAllAddons() {
        try {
            return new ResponseEntity<>(addonService.listAllAddon(), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{addonName}")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<Object> getAddonByAddonName(@PathVariable("addonName") String addonName) {
        try {
            return new ResponseEntity<>(addonService.getAddonById(addonName), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> addAddOn(@RequestBody AddOn newAddon) {
        try {
            return new ResponseEntity<>(addonService.addAddon(newAddon), HttpStatus.ACCEPTED);
        } catch (BadDetailsException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @PutMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> updateAddOn(@RequestBody AddOn updatedAddon) {
        try {
            return new ResponseEntity<>(addonService.updateAddon(updatedAddon), HttpStatus.ACCEPTED);
        } catch (BadDetailsException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{addonName}/available")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> updateAddonAvailability(@PathVariable("addonName") String addonName) {
        try {
            return new ResponseEntity<>(addonService.disableOrEnableAddon(addonName), HttpStatus.ACCEPTED);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(),HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{addonName}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> deleteAddon(@PathVariable("addonName") String addonName) {
        try {
            return new ResponseEntity<>(addonService.deleteAddOn(addonName), HttpStatus.ACCEPTED);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(),HttpStatus.NOT_FOUND);
        }
    }
}
