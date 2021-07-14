package com.ebooking.BookUrMovie.notifications.services;

import com.ebooking.BookUrMovie.notifications.controller.NotificationController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;

@Service
@Slf4j
public class ConsumerService {
    @Value("${endpoint}")
    private String endPoint;

    @Autowired
    NotificationController notificationController;

    @KafkaListener(topics = "email-notification", groupId = "group_id")
    public void consumer(String id) {
        int bookingId = Integer.parseInt(id);
        log.debug("Consumed booking id: " + bookingId);
        RequestAttributes attribs = RequestContextHolder.getRequestAttributes();
        if (attribs instanceof NativeWebRequest) {
            HttpServletRequest request = (HttpServletRequest) ((NativeWebRequest) attribs).getNativeRequest();
        }
        notificationController.sendEmail(bookingId);
        log.debug("Email sent through after consuming booking id: " + bookingId + " from kafka");
    }
}
