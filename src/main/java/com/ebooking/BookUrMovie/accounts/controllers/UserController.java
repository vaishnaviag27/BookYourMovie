package com.ebooking.BookUrMovie.accounts.controllers;


import com.ebooking.BookUrMovie.accounts.services.UserServices;
import com.ebooking.BookUrMovie.accounts.models.UserDetailsDTO;
import com.ebooking.BookUrMovie.commons.exceptions.BadDetailsException;
import com.ebooking.BookUrMovie.commons.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

;
import java.security.Principal;

@RestController
public class UserController {

    @Autowired
    UserServices userServices;

    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@RequestBody UserDetailsDTO newUser) {
        try {
            return new ResponseEntity<>(userServices.addUser(newUser), HttpStatus.ACCEPTED);
        } catch (BadDetailsException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @GetMapping("/home")
    public ResponseEntity<String> homePage(Principal principal) {
        try{
            String userName = userServices.getUserSummaryByEmail(principal.getName()).getUserName();
            String greetings = String.format("<h1>Welcome to BookUrMovie.com, %s!</h1>", userName);
            return new ResponseEntity(greetings, HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/access-denied")
    public ResponseEntity<String> accessDeniedMessage(){
        return new ResponseEntity<>("<h1>Access Denied</h1>", HttpStatus.FORBIDDEN);
    }

    @PutMapping("/users")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<String> updateUserDetails(@RequestBody UserDetailsDTO updateDetails, Principal principal) {
        if (updateDetails.getEmail().equals(principal.getName())) {
            try {
                return new ResponseEntity<>(userServices.updateUser(updateDetails), HttpStatus.ACCEPTED);
            } catch (NotFoundException exception) {
                return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
            } catch (BadDetailsException exception) {
                return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("User Email doesn't Match!", HttpStatus.UNAUTHORIZED);
        }
    }

    @PutMapping("/users/location/{userLocation}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Object> updateUserLocation(@PathVariable("userLocation") String userLocation, Principal principal){
        try {
            return new ResponseEntity<>(userServices.updateUserLocation(principal.getName(), userLocation), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        } catch(BadDetailsException exception){
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Object> getAllUsers() {
        try {
            return new ResponseEntity<>(userServices.getAllUsers(), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/users/{userId}/bookings")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Object> getUserBookingsForAdmin(@PathVariable("userId") int userId) {
        try {
            return new ResponseEntity<>(userServices.getUserBookingsById(userId), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/users/bookings")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Object> getUserBookings(Principal principal) {
        try {
            return new ResponseEntity<>(userServices.getUserBookingsByUserEmail(principal.getName()), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/users/{userId}/details")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<Object> getUserById(@PathVariable("userId") int userId) {
        try {
            return new ResponseEntity<>(userServices.getUserById(userId), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/users/profile")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<Object> getUserSummaryById(Principal principal) {
        try {
            return new ResponseEntity<>(userServices.getUserSummaryByEmail(principal.getName()), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/users/email")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Object> getUserByEmail(Principal principal) {
        try {
            return new ResponseEntity<>(userServices.getUserByEmailUser(principal), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/users/email/admin/{userEmail}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Object> getUserByEmailAdmin(@PathVariable("userEmail") String userEmail) {
        try {
            return new ResponseEntity<>(userServices.getUserByEmail(userEmail), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
