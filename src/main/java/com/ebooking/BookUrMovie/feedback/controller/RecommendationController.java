package com.ebooking.BookUrMovie.feedback.controller;

import com.ebooking.BookUrMovie.commons.exceptions.NotFoundException;
import com.ebooking.BookUrMovie.feedback.services.RecommendationService;
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
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URISyntaxException;

@Slf4j
@RestController
public class RecommendationController {

  @Autowired
  private RecommendationService service;

  @PostMapping("/create-index")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<String> createIndex(HttpServletRequest request, HttpMethod method) {
    try {
      return new ResponseEntity<>(service.createIndex(request, method), HttpStatus.OK);
    } catch (NotFoundException e) {
      log.error("Exception occurred ", e);
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    } catch (ConnectionException | URISyntaxException e) {
      log.error("Exception occurred ", e);
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    } catch (IOException e) {
      log.error("Exception occurred ", e);
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  @GetMapping("/recommendations/{movieName}")
  @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
  public ResponseEntity<Object> recommendMovies(@PathVariable String movieName, HttpServletRequest request, HttpMethod method) {
    try {
      return new ResponseEntity<>(service.recommendMovies(movieName, request, method), HttpStatus.OK);
    } catch (IOException | URISyntaxException e) {
      log.error("Exception occurred " + e.getMessage());
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }
}
