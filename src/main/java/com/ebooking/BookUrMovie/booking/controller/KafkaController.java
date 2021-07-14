package com.ebooking.BookUrMovie.booking.controller;

import com.ebooking.BookUrMovie.booking.services.ProducerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class KafkaController {
  private final ProducerService producer;

  KafkaController(ProducerService producer) {
    this.producer = producer;
  }

  private RestTemplate restTemplate;

  @PostMapping("/kafka/{id}")
  @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
  public void sendIdToKafka(@PathVariable("id") String id) {
      producer.sendMessage(id);
  }
}
