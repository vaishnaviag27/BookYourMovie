package com.ebooking.BookUrMovie.managements.services;

import com.ebooking.BookUrMovie.commons.exceptions.BadDetailsException;
import com.ebooking.BookUrMovie.commons.exceptions.NotFoundException;
import com.ebooking.BookUrMovie.commons.models.Movie;
import com.ebooking.BookUrMovie.commons.models.ObsoleteSeat;
import com.ebooking.BookUrMovie.commons.models.Seat;
import com.ebooking.BookUrMovie.commons.models.Slot;
import com.ebooking.BookUrMovie.commons.models.StringResponse;
import com.ebooking.BookUrMovie.commons.models.Theatre;
import com.ebooking.BookUrMovie.commons.models.User;
import com.ebooking.BookUrMovie.commons.services.CommunicationService;
import com.ebooking.BookUrMovie.managements.repositories.ObsoleteSeatRepository;
import com.ebooking.BookUrMovie.managements.dtos.SlotDTO;
import com.ebooking.BookUrMovie.managements.repositories.MovieRepository;
import com.ebooking.BookUrMovie.managements.repositories.SeatRepository;
import com.ebooking.BookUrMovie.managements.repositories.SlotRepository;
import com.ebooking.BookUrMovie.managements.repositories.TheatreRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.UnknownContentTypeException;

;
import javax.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class SlotService {

    @Autowired
    SlotRepository slotRepository;

    @Autowired
    MovieRepository movieRepository;

    @Autowired
    TheatreRepository theatreRepository;

    @Autowired
    SeatRepository seatRepository;

    @Autowired
    ObsoleteSeatRepository obsoleteSeatRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    public List<Slot> getAllSlots() throws NotFoundException {
        log.debug("Fetching all Slots");
        List<Slot> allSlots = slotRepository.findAll();
        if (allSlots.isEmpty()) {
            throw new NotFoundException("No Slots Found");
        }
        return allSlots;
    }

    public Slot getBySlotId(int slotId) throws NotFoundException {
        log.debug("Fetching Slot by Id: {}", slotId);
        Slot slot = slotRepository.findBySlotId(slotId);
        if (slot == null) {
            throw new NotFoundException("No Slot found with Slot Id: " + slotId);
        }
        return slot;
    }

    public String addSlot(SlotDTO newSlotDetails) throws BadDetailsException {
        log.debug("Adding a new Slot");
        String slotDetailsValidatorResponse = validateSlotDetails(newSlotDetails, true);
        if (!slotDetailsValidatorResponse.equals("valid")) {
            log.error("Slot Details Are invalid, Reason: {}", slotDetailsValidatorResponse);
            throw new BadDetailsException("Slot details are invalid. Reason: " + slotDetailsValidatorResponse);
        }
        Movie movie = movieRepository.findByMovieName(newSlotDetails.getMovieName());
        if (movie == null) {
            log.error("Slot Movie not found: {} while adding a new slot", newSlotDetails.getMovieName());
            throw new BadDetailsException(
                String.format("No movie found with the name: %s while adding a new slot ",
                    newSlotDetails.getMovieName()));
        }
        Theatre theatre = theatreRepository.findByTheatreName(newSlotDetails.getTheatreName());
        if (theatre == null) {
            log.error("Slot Theatre not found: {} while adding a new slot.", newSlotDetails.getTheatreName());
            throw new BadDetailsException(
                String.format("No Theatre found with the name: %s while adding a new slot ",
                    newSlotDetails.getTheatreName()));
        }
        Slot slot = new Slot(
            newSlotDetails.getSlotTime(),
            newSlotDetails.getSlotDate(),
            newSlotDetails.getSlotPrice(),
            newSlotDetails.getNumberOfSeats(),
            movie,
            theatre);
        slotRepository.save(slot);
        List<Seat> seats = new ArrayList<>();
        for (int seatNumber = 1; seatNumber <= newSlotDetails.getNumberOfSeats(); seatNumber++) {
            Seat newSeat = new Seat(seatNumber, slot);
            seatRepository.save(newSeat);
            seats.add(newSeat);
        }
        slot.setSeats(seats);
        log.debug("Added seats for new slot");
        slotRepository.save(slot);
        log.debug("Added a new slot with Movie: {} and Theatre: {}", movie.getMovieName(), theatre.getTheatreName());
        return String.format("Added a new slot with id: %d, for movie: %s at theatre: %s",
            slot.getSlotId(),
            movie.getMovieName(),
            theatre.getTheatreName());
    }

    @Transactional
    public String deleteBySlotId(int slotId, HttpServletRequest request) throws NotFoundException, IllegalArgumentException {
        Slot slot = slotRepository.findBySlotId(slotId);
        if (slot == null) {
            log.error("No slot found with Id: {} while deleting slot", slotId);
            throw new NotFoundException(
                String.format("No slot found with id: %s while deleting slot", slotId));
        }
        Date currentDate = new Date();
        if(currentDate.compareTo(slot.getSlotDate()) > 0){
            throw new IllegalArgumentException(
                String.format("Cannot delete a slot that has passed. Current Date: %s is greater than slot date: %s",
                    currentDate, slot.getSlotDate())
            );
        }
        log.debug("Initiating Refund process while deleting slot with Id: {}", slotId);
        List<String> result = new ArrayList<>();
        if(!slot.getBookings().isEmpty()){
            slot.getBookings().forEach(booking -> {
                try {
                    StringResponse s = objectMapper.convertValue(CommunicationService.RequestMirror(
                        null,
                        HttpMethod.DELETE,
                        request,
                        "/bookings/" + booking.getBookingId()), new TypeReference<StringResponse>(){});
                    result.add(s.getName());
                } catch (URISyntaxException e) {
                    log.error("Cannot connect to Bookings Service to delete a booking");
                } catch (HttpClientErrorException e) {
                    log.error("Booking Id not found: {} while deleting a booking", booking.getBookingId());
                }
            });
        }
        slotRepository.deleteById(slotId);
        log.debug("Slot deleted with Id: {}", slotId);
        return String.format("Deleted slot with id: " + slotId + "\n" + result);
    }

    public List<Slot> findSlotByMovieNameAndTheatreName(String movieName, String theatreName) throws NotFoundException {
        log.debug("Finding Slots with Movie: {} and Theatre: {}", movieName, theatreName);
        Movie movie = movieRepository.findByMovieName(movieName);
        if (movie == null) {
            log.error("Movie: {} not found while finding slots by movie: {} and theatre: {} ",
                movieName, movieName, theatreName);
            throw new NotFoundException(
                String.format("Movie: %s not found while finding slots by movie: %s and theatre: %s ",
                    movieName, movieName, theatreName));
        }
        Theatre theatre = theatreRepository.findByTheatreName(theatreName);
        if (theatre == null) {
            log.error("Theatre: {} not found while finding slots by movie: {} and theatre: {} ",
                theatreName, movieName, theatreName);
            throw new NotFoundException(
                String.format("Theatre: %s not found while finding slots by movie: %s and theatre: %s ",
                    theatreName, movieName, theatreName));
        }
        List<Slot> foundSlots;
        foundSlots = slotRepository.findSlotByMovieIdAndTheatreId(movie.getMovieId(), theatre.getTheatreId());
        if (foundSlots == null || foundSlots.isEmpty()) {
            log.error("No slots found with Movie: {} and Theatre: {}", movieName, theatreName);
            throw new NotFoundException(String.format("Cannot find Slots with Movie: %s and Theatre: %s", movieName, theatreName));
        }
        return filterUnreservedSeats(foundSlots);
    }

    public List<Slot> getSlotsByMovieName(String movieName, HttpServletRequest request) throws NotFoundException {
        Boolean movieExists = movieRepository.existsByMovieName(movieName);
        if (!movieExists) {
            log.error("No slot found for movie: " + movieName);
            throw new NotFoundException("No slot found for movie: " + movieName);
        }
        User user = null;
        try {
            user = objectMapper.convertValue(CommunicationService.RequestMirror
                    (null, HttpMethod.GET, request, "/users/profile"),
                new TypeReference<User>() {
                });
        } catch (URISyntaxException e) {
            log.error("Cannot connect to Accounts Service");
        } catch (HttpClientErrorException e) {
            log.error("User Not found for fetching Location");
        }
        if (user == null) {
            log.error("User not found while fetching slots for movie: {} by user location.", movieName);
            throw new NotFoundException(
                String.format("User not found while fetching slots for movie: %s by user location", movieName));
        }
        List<Slot> foundSlots = slotRepository.getSlotsByMovieNameAndLocation(movieName, user.getUserLocation());
        if (foundSlots.isEmpty()) {
            log.error("No slots Found At location: {} for movie: {}", user.getUserLocation(), movieName);
            throw new NotFoundException(
                String.format("No slots Found At location: %s for movie: %s", user.getUserLocation(), movieName));
        }
        return filterUnreservedSeats(foundSlots);
    }

    public List<Slot> getAllSlotsByLocation(HttpServletRequest request) throws NotFoundException {
        User user = null;
        try {
            user = objectMapper.convertValue(CommunicationService.RequestMirror
                    (null, HttpMethod.GET, request, "/users/profile"),
                new TypeReference<User>() {
                });
        } catch (URISyntaxException e) {
            log.error("Cannot connect to Accounts Service");
        } catch (HttpClientErrorException e) {
            log.error("User Not found for fetching Location");
        }
        if (user == null) {
            log.error("User not found while fetching slots by user location.");
            throw new NotFoundException("User not found while fetching slots by user location");
        }
        List<Slot> foundSlots = slotRepository.getSlotsByLocation(user.getUserLocation());
        if (foundSlots.isEmpty()) {
            log.error("No slots Found At location: {}", user.getUserLocation());
            throw new NotFoundException(
                String.format("No slots Found At location: %s", user.getUserLocation()));
        }
        return filterUnreservedSeats(foundSlots);
    }

    private List<Slot> filterUnreservedSeats(List<Slot> slots) {
        for (Slot slot : slots) {
            if (!slot.getSeats().isEmpty()) {
                List<Seat> unReservedSeats = new ArrayList<>();
                for (Seat seat : slot.getSeats()) {
                    if (seat.getBooking() == null) {
                        unReservedSeats.add(seat);
                    }
                }
                slot.setSeats(unReservedSeats);
            }
        }
        return slots;
    }

    private String validateSlotDetails(SlotDTO slotDetails, boolean isNewSlot) {
        Date currentDate = new Date();
        if (isNewSlot) {
            if (slotDetails.getMovieName() == null) {
                return "Movie Name cannot be null in a Slot!";
            }
            if (slotDetails.getTheatreName() == null) {
                return "Theatre Name cannot be null in a Slot!";
            }
            if (slotDetails.getSlotDate() == null) {
                return "Slot Date Cannot be empty!";
            }
            if (slotDetails.getSlotTime() == null) {
                return "Slot Time cannot be empty!";
            }
        }
        if (slotDetails.getSlotDate() != null && slotDetails.getSlotDate().compareTo(currentDate) <= 0) {
            return "Slot Date Cannot be set before the current Date!";
        }
        if (slotDetails.getMovieName() != null && slotDetails.getMovieName().length() <= 2) {
            return "Movie name must be at least 2 characters long";
        }
        if (slotDetails.getTheatreName() != null && slotDetails.getTheatreName().length() <= 2) {
            return "Theatre name should be at least 3 chacters long";
        }
        if (slotDetails.getSlotPrice() <= 0.0f) {
            return "Seat Price Cannot be less than or equal to 0";
        }
        if (slotDetails.getNumberOfSeats() <= 0) {
            return "Number of seats in a slot cannot be less than or equal to 0";
        }
        return "valid";
    }

    @Transactional
    public String findAndDeleteOutdatedSlots() {
        Date currentDate = new Date();
        log.debug("Deleting seats from outdated slots before date: {}", currentDate);
        List<Slot> outdatedSlots = slotRepository.findOutdatedSlots();
        if (outdatedSlots == null || outdatedSlots.isEmpty()) {
            log.warn("No outdated slots before date: {}", currentDate);
            return String.format("No Outdated Slots before date: %s", currentDate);
        }

        outdatedSlots.stream().forEach(slot -> {
            int slotId = slot.getSlotId();
            Date slotDate = slot.getSlotDate();
            Time slotTime = slot.getSlotTime();
            String movieName = slot.getMovie().getMovieName();
            String theatreName = slot.getTheatre().getTheatreName();
            List<ObsoleteSeat> obsoleteSeats = new ArrayList<>();
            slot.getSeats().stream().forEach(seat -> {
                int bookingId = seat.getBooking() != null ? seat.getBooking().getBookingId() : 0;
                ObsoleteSeat obsoleteSeat = new ObsoleteSeat(
                    seat.getSeatId(),
                    seat.getSeatNumber(),
                    bookingId,
                    slotId,
                    (java.sql.Date) slotDate,
                    slotTime,
                    movieName,
                    theatreName);
                obsoleteSeats.add(obsoleteSeat);
            });
            obsoleteSeatRepository.saveAll(obsoleteSeats);
            log.debug("Saved Obsolete Seats with Slot Id: {} to obsolete seats", slotId);
            seatRepository.deleteSeatsBySlotId(slotId);
            log.debug("Deleted Seats with Slot Id: {} from slot", slotId);
            slot.setIsOutdated(true);
            slotRepository.save(slot);
        });
        return String.format("Deleted seats from Outdated Slots before date: %s", currentDate);
    }
}
