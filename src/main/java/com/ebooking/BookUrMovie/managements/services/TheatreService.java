package com.ebooking.BookUrMovie.managements.services;

import com.ebooking.BookUrMovie.commons.exceptions.BadDetailsException;
import com.ebooking.BookUrMovie.commons.exceptions.NotFoundException;
import com.ebooking.BookUrMovie.commons.models.Slot;
import com.ebooking.BookUrMovie.commons.models.Theatre;
import com.ebooking.BookUrMovie.commons.models.User;
import com.ebooking.BookUrMovie.commons.services.CommunicationService;
import com.ebooking.BookUrMovie.managements.repositories.TheatreRepository;
import com.ebooking.BookUrMovie.managements.utils.NonNullObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

;
import javax.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;
import java.util.List;

@Service
@Slf4j
public class TheatreService {
    @Autowired
    TheatreRepository theatreRepository;

    public List<Theatre> listTheatres() throws NotFoundException {
        log.debug("Fetching all theatres");
        List<Theatre> theatres = theatreRepository.findAll();
        if (theatres.isEmpty()) {
            log.error("Theatres not found");
            throw new NotFoundException("Theatres not found");
        }
        return theatres;
    }

    public List<Theatre> listTheatresByLocation(HttpServletRequest request) throws NotFoundException {
        log.debug("Fetching all theatres by Location");
        ObjectMapper objectMapper = new ObjectMapper();
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
        List<Theatre> foundTheatres = theatreRepository.getTheatresByLocation(user.getUserLocation());
        if (foundTheatres.isEmpty()) {
            log.error("Theatres not found");
            throw new NotFoundException("Theatres not found");
        }
        return foundTheatres;
    }

    public Theatre findByTheatreId(int id) throws NotFoundException {
        log.debug("Fetching theatre by id: {}", id);
        Theatre theatre = theatreRepository.findByTheatreId(id);
        if (theatre == null) {
            log.error("Theatre not found with Id: {}", id);
            throw new NotFoundException("Theatre not found with Id: " + id);
        }
        return theatre;
    }

    public Theatre findByTheatreName(String name) throws NotFoundException {
        log.debug("Fetching theatre by name: {}", name);
        Theatre theatre = theatreRepository.findByTheatreName(name);
        if (theatre == null) {
            log.error("Theatre not found with name: {}", name);
            throw new NotFoundException("Theatre not found with name: " + name);
        }
        return theatre;
    }

    public String addTheatre(Theatre theatre) throws BadDetailsException, IllegalArgumentException {
        log.debug("Adding a theatre with name: {}", theatre.getTheatreName());
        if (theatreRepository.existsByTheatreName(theatre.getTheatreName())) {
            log.error("Theatre you are trying to add already exists: {}", theatre.getTheatreName());
            throw new IllegalArgumentException("Theatre you are trying to add already exists: " + theatre.getTheatreName());
        }
        String theatreDetailsValidatorResponse = theatreDetailsValidator(theatre, true);
        if (!theatreDetailsValidatorResponse.equals("valid")) {
            log.error("Invalid Theatre Details for theatre {}. Reason: {}",theatre.getTheatreName(), theatreDetailsValidatorResponse);
            throw new BadDetailsException(String.format("Invalid Theatre Details for theatre %s. Reason: %s",
                theatre.getTheatreName(),theatreDetailsValidatorResponse));
        }
        theatreRepository.save(theatre);
        log.debug("Added theatre successfully: {}", theatre.getTheatreName());
        return "Added Theatre with theatre name: " + theatre.getTheatreName();
    }

    public String updateTheatre(Theatre updatedTheatre) throws NotFoundException, BadDetailsException {
        log.debug("Updating a theatre with Id: {}", updatedTheatre.getTheatreId());
        Theatre foundTheatre = theatreRepository.findByTheatreId(updatedTheatre.getTheatreId());
        if (foundTheatre == null) {
            log.error("Theatre does not exist with Id: {}", updatedTheatre.getTheatreId());
            throw new NotFoundException("Theatre does not exist with Id: " + updatedTheatre.getTheatreId());
        }
        String theatreDetailsValidatorResponse = theatreDetailsValidator(updatedTheatre, false);
        if (!theatreDetailsValidatorResponse.equals("valid")) {
            log.error("Update details invalid for theatre with Id {}. Reason: {}", updatedTheatre.getTheatreId(), theatreDetailsValidatorResponse);
            throw new BadDetailsException(String.format("Update Details invalid for theatre with Id: %s. Reason: %s",
                updatedTheatre.getTheatreId(), theatreDetailsValidatorResponse));
        }
        updatedTheatre.setTheatreName(foundTheatre.getTheatreName());
        updatedTheatre.setTheatreRating(foundTheatre.getTheatreRating());
        NonNullObjectMapper.copyNonNullProperties(updatedTheatre, foundTheatre);
        theatreRepository.save(foundTheatre);
        log.debug("Updated Theatre details with theatre Id: {}", updatedTheatre.getTheatreId());
        return String.format("Theatre with theatre Id %s updated!", updatedTheatre.getTheatreId());
    }

    public String updateTheatreRating(int theatreId, float theatreRating) throws NotFoundException {
        if (theatreRepository.existsById(theatreId)) {
            theatreRepository.updateTheatreRating(theatreId, theatreRating);
            return "Updated Theatre Rating for theatre with Id: " + theatreId;
        } else {
            log.error("Theatre Not found with Id: {}", theatreId);
            throw new NotFoundException("Theatre not found with Id: " + theatreId);
        }
    }

    public List<Slot> findSlotsByTheatreName(String theatreName) throws NotFoundException {
        log.debug("Finding Slots by theatre Name: {}", theatreName);
        Theatre foundTheatre = findByTheatreName(theatreName);
        foundTheatre.setTheatreRatings(null);
        return foundTheatre.getSlots();
    }

    private String theatreDetailsValidator(Theatre theatre, boolean isFirstTime) {
        log.debug("Validating Theatre Details");
        if (isFirstTime) {
            if (theatre.getTheatreName() == null) {
                return "Please provide a Theatre Name!";
            }
            if (theatre.getTheatreLocation() == null) {
                return "Please Provide a Theatre Location!";
            }
            if (theatre.getTheatreRating() != 0.00f) {
                return "Initially Theatre Rating should be Zero!";
            }
        }

        if (theatre.getTheatreName() != null && theatre.getTheatreName().length() == 0) {
            return "Theatre Name Required";
        }
        if (theatre.getTheatreLocation() != null && theatre.getTheatreLocation().length() == 0) {
            return "theatre Location is required";
        }
        if (theatre.getTheatreRating() != 0.00f && (theatre.getTheatreRating() < 1 || theatre.getTheatreRating() > 5)) {
            return "Theatre Rating should be between 1 and 5";
        }
        return "valid";
    }

}

