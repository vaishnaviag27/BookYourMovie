package com.ebooking.BookUrMovie.feedback.controller;

import com.ebooking.BookUrMovie.commons.exceptions.NotFoundException;
import com.ebooking.BookUrMovie.feedback.dto.MovieRatingDTO;
import com.ebooking.BookUrMovie.feedback.dto.TheatreRatingDTO;
import com.ebooking.BookUrMovie.feedback.services.FeedbackServices;
import com.sun.mail.iap.ConnectionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;

@Slf4j
@RestController
public class FeedbackController {

  @Autowired
  private FeedbackServices feedbackServices;

  @PostMapping("/movie-ratings")
  @PreAuthorize("hasRole('ROLE_USER')")
  public ResponseEntity<String> addMovieRating(@RequestBody MovieRatingDTO movieRatingDto,
                                               HttpServletRequest request, HttpMethod method) {
    try {
      return new ResponseEntity<>(feedbackServices.saveMovieRating(movieRatingDto, request, method),
          HttpStatus.OK);
    } catch (NotFoundException e) {
      log.error("Exception Occurred", e);
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    } catch (IllegalArgumentException e) {
      log.error("Exception occurred", e);
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    } catch (ConnectionException | URISyntaxException e) {
      log.error("Exception occurred", e);
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }
  }

  @GetMapping("/movie-ratings")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<Object> findAllMovieRating() {
    try {
      return new ResponseEntity<>(feedbackServices.findAllMovieRating(), HttpStatus.OK);
    } catch (NotFoundException e) {
      log.error("Exception Occurred", e);
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }
  }

  @GetMapping("/movie-ratings/{id}")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<Object> getMovieRating(@PathVariable int id) {
    try {
      return new ResponseEntity<>(feedbackServices.findByMovieRatingId(id), HttpStatus.OK);
    } catch (NotFoundException e) {
      log.error("Exception Occurred", e);
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }
  }

  @PostMapping("/theatre-ratings")
  @PreAuthorize("hasRole('ROLE_USER')")
  public ResponseEntity<String> addTheatreRating(@RequestBody TheatreRatingDTO theatreRatingDto,
                                                 HttpServletRequest request, HttpMethod method)
      throws Exception {
    try {
      return new ResponseEntity<>(feedbackServices.saveTheatreRating(theatreRatingDto, request,
          method), HttpStatus.OK);
    } catch (NotFoundException e) {
      log.error("Exception Occurred", e);
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    } catch (IllegalArgumentException e) {
      log.error("Exception occurred", e);
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    } catch (ConnectionException | URISyntaxException e) {
      log.error("Exception occurred", e);
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }
  }

  @GetMapping("/theatre-ratings")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<Object> findAllTheatreRating() {
    try {
      return new ResponseEntity<>(feedbackServices.findAllTheatreRating(), HttpStatus.OK);
    } catch (NotFoundException e) {
      log.error("Exception Occurred", e);
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

  }

  @GetMapping("/theatre-ratings/{id}")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<Object> getTheatreRating(@PathVariable int id) {
    try {
      return new ResponseEntity<>(feedbackServices.findByTheatreRatingId(id), HttpStatus.OK);
    } catch (NotFoundException e) {
      log.error("Exception Occurred", e);
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }
  }

}
