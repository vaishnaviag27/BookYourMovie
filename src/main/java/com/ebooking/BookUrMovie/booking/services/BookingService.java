package com.ebooking.BookUrMovie.booking.services;

import com.ebooking.BookUrMovie.booking.dto.BookingDTO;
import com.ebooking.BookUrMovie.booking.repositories.BookingRepository;
import com.ebooking.BookUrMovie.commons.exceptions.BadDetailsException;
import com.ebooking.BookUrMovie.commons.exceptions.NotFoundException;
import com.ebooking.BookUrMovie.commons.models.Booking;
import com.ebooking.BookUrMovie.commons.models.Seat;
import com.ebooking.BookUrMovie.commons.models.Slot;
import com.ebooking.BookUrMovie.commons.models.StringResponse;
import com.ebooking.BookUrMovie.commons.models.User;
import com.ebooking.BookUrMovie.commons.services.CommunicationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownContentTypeException;

;
import javax.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;
import java.security.Principal;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class BookingService {

    @Value("${endpoint}")
    private String endPoint;

    @Autowired
    private BookingRepository bookingRepository;

    private RestTemplate restTemplate;

    public Booking getBookingsByBookingId(int bookingId) throws NotFoundException {
        log.debug("Fetching booking by id: " + bookingId);
        Booking booking = bookingRepository.findByBookingId(bookingId);
        if (booking == null) {
            log.error("Booking not found with id: " + bookingId);
            throw new NotFoundException("No booking with id: " + bookingId);
        }
        booking.getSlot().setSeats(null);
        return booking;
    }

    public User getUserByBookingId(int bookingId) throws NotFoundException {
        log.debug("Fetching booking by id: " + bookingId);
        Booking booking = bookingRepository.findByBookingId(bookingId);
        if (booking == null) {
            log.error("Booking not found with id: " + bookingId);
            throw new NotFoundException("No booking with id: " + bookingId);
        }
        return booking.getUser();
    }

    public List<Booking> getAllBookings() throws NotFoundException {
        log.debug("Fetching all bookings");
        List<Booking> bookings = bookingRepository.findAll();
        if (bookings.isEmpty()) {
            log.error("Bookings not found");
            throw new NotFoundException("No bookings found");
        }
        for (Booking booking : bookings) {
            booking.getSlot().setSeats(null);
        }
        return bookings;
    }

    public String addBooking(BookingDTO bookingDTO, Principal principal, HttpServletRequest request)
        throws NotFoundException, BadDetailsException, URISyntaxException {

        log.debug("adding booking for slotid : " + bookingDTO.getSlotId());
        if (bookingDTO.getSeatNumberList().isEmpty()) {
            log.error("Seat List empty");
            throw new NullPointerException("Seat List empty");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        User user = null;
        try {
            user = objectMapper.convertValue(CommunicationService.RequestMirror
                    (null, HttpMethod.GET, request, "/users/email"),
                new TypeReference<User>() {
                });
        } catch (URISyntaxException e) {
            log.error("Cannot connect to Accounts Service");
        } catch (HttpClientErrorException e) {
            log.error("No user found with email: " + principal.getName());
            throw new NotFoundException("User not found with email: " + principal.getName());
        }

        Slot slot = null;
        try {
            slot = objectMapper.convertValue(CommunicationService.RequestMirror
                    (null, HttpMethod.GET, request, "/slots/" + bookingDTO.getSlotId()),
                new TypeReference<Slot>() {
                });
        } catch (URISyntaxException e) {
            log.error("Cannot connect to Management Service");
        } catch (HttpClientErrorException e) {
            log.error("No slot found with id: " + bookingDTO.getSlotId());
            throw new NotFoundException("Slot not found with id: " + bookingDTO.getSlotId());
        }

        int maxSeatNumberInSlot = slot.getSeats().get(slot.getSeats().size() - 1).getSeatNumber();
        for (int seatNo : bookingDTO.getSeatNumberList()) {
            if (seatNo < 1 && seatNo > maxSeatNumberInSlot) {
                throw new NotFoundException("Seat no " + seatNo + " does not exist.");
            }
        }

        LocalDate currentDate = LocalDate.now();
        LocalDate slotDate = convertToLocalDateViaSqlDate(slot.getSlotDate());
        LocalTime currentTime = LocalTime.now();
        LocalTime slotTime = convertToLocalDateViaSqlTime(slot.getSlotTime());

        if (currentDate.isAfter(slotDate) ||
            (currentDate.isEqual(slotDate) && currentTime.isAfter(slotTime))) {
            log.error("Can't book seat as the slot with id: " + slot.getSlotId() + " has already passed.");
            throw new BadDetailsException("Can't book seat as the slot with id: "
                + slot.getSlotId() + " has already passed.");
        }

        /**
         * for storing all seats in a slot
         */
        List<Integer> seatsInSlot = new ArrayList<>();

        /**
         * for storing mapping between seatno and seat model
         */
        Map<Integer, Seat> seatMapper = new HashMap<>();

        for (Seat seat : slot.getSeats()) {
            seatMapper.put(seat.getSeatNumber(), seat);
            Boolean isSeatBooked = null;
            try {
                isSeatBooked =
                    objectMapper.convertValue(
                        CommunicationService.RequestMirror(null,
                            HttpMethod.GET,
                            request,
                            "/seats/" + seat.getSeatId() + "/booking/available"),
                        new TypeReference<Boolean>() {
                        });
            } catch (URISyntaxException e) {
                log.error("Cannot connect to Management Service");
            } catch (HttpClientErrorException e) {
                log.error("Something went wrong while checking if seat " + bookingDTO.getSlotId() + " is empty or not");
            }

            if (isSeatBooked != null && !isSeatBooked)
                seatsInSlot.add(seat.getSeatNumber());
        }
        List<Integer> bookedSeats = new ArrayList<>();
        for (int seatNo : bookingDTO.getSeatNumberList()) {
            if (!seatsInSlot.contains(seatNo)) {
                bookedSeats.add(seatNo);
            }
        }

        if (!bookedSeats.isEmpty()) {
            log.error("Seat no: " + Arrays.toString(bookedSeats.toArray()) + " already booked.");
            throw new NotFoundException("Seat nos: " + Arrays.toString(bookedSeats.toArray()) + " already booked.");
        }

        int seatCount = bookingDTO.getSeatNumberList().size();
        float bookingAmount = slot.getSlotPrice() * seatCount;
        Booking booking = new Booking(
            slot,
            user,
            slot.getMovie().getMovieName(),
            slot.getTheatre().getTheatreName(),
            bookingAmount,
            seatCount
        );
        bookingRepository.save(booking);

        for (int seatNo : bookingDTO.getSeatNumberList()) {
            Seat seat = seatMapper.get(seatNo);
            try {
                CommunicationService.RequestMirror(
                    null,
                    HttpMethod.PUT,
                    request,
                    "/seats/" + seat.getSeatId() + "/" + booking.getBookingId());
            } catch (UnknownContentTypeException e) {
                log.debug("Seat Getting Updated");
            }
        }

        /**
         * pushing booking id into kafka
         */
        try {
            CommunicationService.RequestMirror(
                null,
                HttpMethod.POST,
                request,
                "/kafka/" + booking.getBookingId());
        } catch (UnknownContentTypeException e) {
            log.error("Something went wrong while producing to kafka.");
        }
        return "Booking confirmed for slot id: " + slot.getSlotId() + "!\nYour booking id is " + booking.getBookingId()
            + " for movie: " + booking.getMovieName()
            + " at " + booking.getTheatreName()
            + " on " + slot.getSlotDate()
            + " at " + slot.getSlotTime()
            + " for seats: " + bookingDTO.getSeatNumberList()
            +"\nTicket sent to your email: " + user.getUserEmail();
    }

    public StringResponse cancelBooking(int bookingId, Principal principal, HttpServletRequest request)
        throws NotFoundException, URISyntaxException {
        log.debug("Cancelling booking with id : " + bookingId);
        Booking booking = bookingRepository.findByBookingId(bookingId);
        if (booking == null) {
            log.error("Booking not found with id: " + bookingId);
            throw new NotFoundException("No booking with id: " + bookingId);
        }
        if (!booking.getUser().getUserEmail().equals(principal.getName()) &&
            !principal.getName().equals("admin@bym.com")) {
            throw new IllegalArgumentException(
                "You can't delete this booking as this booking id : " + bookingId + " doesn't belong to you.");
        }
        if (booking.isCancelled()) {
            throw new IllegalArgumentException("Booking with id: " + bookingId + " already cancelled.");
        }
        LocalDate currentDate = LocalDate.now();
        LocalDate slotDate = convertToLocalDateViaSqlDate(booking.getSlot().getSlotDate());
        LocalTime currentTime = LocalTime.now();
        LocalTime slotTime = convertToLocalDateViaSqlTime(booking.getSlot().getSlotTime());

        if (currentDate.isAfter(slotDate) ||
            (currentDate.isEqual(slotDate) && currentTime.isAfter(slotTime))) {
            log.error("Can't book seat as slot already passed.");
            throw new IllegalArgumentException("Can't book seat as slot already passed.");
        }

        slotDate = slotDate.minusDays(1);
        if ((currentDate.isAfter(slotDate) ||
            (currentDate.isEqual(slotDate) && currentTime.isAfter(slotTime))) &&
            !principal.getName().equals("admin@bym.com")) {
            log.error("Can't cancel booking as less than 24 hrs left for the show.");
            throw new IllegalArgumentException("Can't cancel booking as less than 24 hrs left for the show.");
        }
        for (Seat seat : booking.getSeatList()) {
            CommunicationService.RequestMirror(null,
                HttpMethod.DELETE,
                request,
                "/seats/" + seat.getSeatId() + "/booking/available");
        }

        booking.setCancelled(true);
        bookingRepository.save(booking);
        return new StringResponse("Cancelled booking with id : " + booking.getBookingId()
            + " for " + booking.getMovieName() + " at " + booking.getTheatreName()
            + " on " + booking.getSlot().getSlotDate()
            + " at " + booking.getSlot().getSlotTime()
            + ". Refund initiated of Rs." + booking.getBookingAmount()
            + " to " + booking.getUser().getUserName() +"\n");
    }

    public LocalDate convertToLocalDateViaSqlDate(Date dateToConvert) {
        return new java.sql.Date(dateToConvert.getTime()).toLocalDate();
    }

    public LocalTime convertToLocalDateViaSqlTime(Time timeToConvert) {
        return new java.sql.Time(timeToConvert.getTime()).toLocalTime();
    }
}
