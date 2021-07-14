package com.ebooking.BookUrMovie.booking.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProducerService {
  private static final String TOPIC = "email-notification";

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;

  public void sendMessage(String bookingId) {
    log.debug("Producing booking id : " + bookingId);
    this.kafkaTemplate.send(TOPIC, bookingId);
  }
}
