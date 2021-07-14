package com.ebooking.BookUrMovie.notifications.controller;

import com.ebooking.BookUrMovie.commons.exceptions.NotFoundException;
import com.ebooking.BookUrMovie.notifications.services.NotificationServices;
import com.sun.mail.iap.ConnectionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;

@RestController
@Slf4j
public class NotificationController {

  @Autowired
  private NotificationServices services;

  @Autowired
  HttpServletResponse response;

  @GetMapping(value = "/email/{bookingId}")
  public ResponseEntity<Object> sendEmail(@PathVariable("bookingId") int id) {
    try {
      return new ResponseEntity<>(services.sendEmail(id), HttpStatus.OK);
    } catch (NotFoundException exp) {
      return new ResponseEntity<>(exp.getMessage(), HttpStatus.NOT_FOUND);
    } catch (IllegalArgumentException | ConnectionException exception) {
      return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    } catch (IOException ioException) {
      return new ResponseEntity<>(ioException.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
    } catch (MailAuthenticationException exp) {
      return new ResponseEntity<>("Can't send email because username or password incorrect",
          HttpStatus.UNAUTHORIZED);
    }
  }

  @GetMapping("/invoice/{bookingId}")
  @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
  public ResponseEntity<String> generateInvoice(@PathVariable("bookingId") int id,
                                                Principal principal,
                                                HttpServletRequest request) throws IOException {
    try {
      return new ResponseEntity<>(services.generateInvoice(id, principal, request, response), HttpStatus.OK);
    } catch (NotFoundException exception) {
      return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    } catch (ConnectionException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    } catch (IllegalArgumentException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.OK);
    }
  }
}
