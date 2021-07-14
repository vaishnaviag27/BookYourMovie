package com.ebooking.BookUrMovie.feedback.services;

import com.ebooking.BookUrMovie.commons.exceptions.NotFoundException;
import com.ebooking.BookUrMovie.commons.models.Booking;
import com.ebooking.BookUrMovie.commons.models.Movie;
import com.ebooking.BookUrMovie.commons.models.MovieRating;
import com.ebooking.BookUrMovie.commons.models.Theatre;
import com.ebooking.BookUrMovie.commons.models.TheatreRating;
import com.ebooking.BookUrMovie.commons.services.CommunicationService;
import com.ebooking.BookUrMovie.feedback.dto.MovieRatingDTO;
import com.ebooking.BookUrMovie.feedback.dto.TheatreRatingDTO;
import com.ebooking.BookUrMovie.feedback.repository.MovieRatingRepository;
import com.ebooking.BookUrMovie.feedback.repository.TheatreRatingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.mail.iap.ConnectionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownContentTypeException;

import javax.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;
import java.sql.Time;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Slf4j
public class FeedbackServices {

  @Value("${endpoint}")
  private String endPoint;

  @Autowired
  private MovieRatingRepository movieRatingRepository;

  @Autowired
  private TheatreRatingRepository theatreRatingRepository;

  private RestTemplate restTemplate;

  public String saveMovieRating(MovieRatingDTO movieRatingDto, HttpServletRequest request,
                                HttpMethod method)
      throws NotFoundException,
      URISyntaxException,
      IllegalArgumentException,
      HttpServerErrorException,
      ConnectionException {

    if (movieRatingDto.getRating() < MovieRating.MIN_MOVIE_RATING || movieRatingDto.getRating() >
        MovieRating.MAX_MOVIE_RATING)

      throw new IllegalArgumentException("Rating should be from 1 to 5. Please re-enter the rating");

    ObjectMapper mapper = new ObjectMapper();
    Booking booking = null;
    log.debug("Fetching booking");
    try {
      booking = mapper.convertValue(CommunicationService.RequestMirror(
          null,
          HttpMethod.GET,
          request,
          "/bookings/" + movieRatingDto.getBookingId()),
          Booking.class);
    } catch (HttpClientErrorException e) {
      throw new NotFoundException("No booking found with booking id - " +
          movieRatingDto.getBookingId());
    } catch (URISyntaxException e) {
      throw new ConnectionException("Unable to fetch booking");
    }

    if (booking.isCancelled()) {
      log.debug("Booking with booking id " + booking.getBookingId() + " was cancelled, Cannot " +
          "proceed with rating");
      throw new IllegalArgumentException("Cannot rate the movie as the booking was cancelled.");
    }

    MovieRating movieRatingFound = null;
    try {
      movieRatingFound = movieRatingRepository.findByBooking(booking).get();
    } catch (NoSuchElementException e) {
      log.debug("No rating found with the booking id " + booking.getBookingId() + ", hence movie"
          + " can be rated");
    }
    if (movieRatingFound != null) {
      log.debug("A rating of " + movieRatingFound.getRating() +
          "already exists with booking Id " + movieRatingDto.getBookingId());
      throw new IllegalArgumentException("Rating already exists");
    }

    LocalDate currentDate = LocalDate.now();
    LocalDate slotDate = convertToLocalDateViaSqlDate(booking.getSlot().getSlotDate());
    LocalTime currentTime = LocalTime.now();
    LocalTime slotTime = convertToLocalDateViaSqlTime(booking.getSlot().getSlotTime());

    System.out.println(currentDate.compareTo(slotDate));

    if (currentDate.compareTo(slotDate) < 0 || (currentDate.compareTo(slotDate) == 0 && currentTime
        .isBefore(slotTime)))
      throw new IllegalArgumentException("Cannot rate the movie before the show timings.");

    log.debug("Fetching movie");
    Movie movie = mapper.convertValue(CommunicationService.RequestMirror(
        null,
        HttpMethod.GET,
        request,
        "/movies/" + booking.getMovieName() + "/details"),
        Movie.class);

    int numOfRatings = movie.getMovieRatings().size();
    float avgRating = (movie.getMovieRating() * numOfRatings + movieRatingDto.getRating())
        / (numOfRatings + 1);

    log.info("Updating average movie rating");
    try {
      CommunicationService.RequestMirror(
          null,
          HttpMethod.PUT,
          request,
          "/movies/" + movie.getMovieId() + "/rating/" + avgRating);
    } catch (UnknownContentTypeException e) {
      log.debug("Updated movie rating.");
    }


    MovieRating movieRating = new MovieRating(booking, movie, movieRatingDto.getRating());
    movieRatingRepository.save(movieRating);
    return "Movie rating added successfully!";
  }

  public String saveTheatreRating(TheatreRatingDTO theatreRatingDto, HttpServletRequest request,
                                  HttpMethod method)
      throws Exception {

    if (theatreRatingDto.getRating() < TheatreRating.MIN_THEATRE_RATING ||
        theatreRatingDto.getRating() >
            TheatreRating.MAX_THEATRE_RATING)
      throw new IllegalArgumentException("Rating should be from 1 to 5. Please re-enter the rating");

    ObjectMapper mapper = new ObjectMapper();
    Booking booking = null;
    log.debug("Fetching booking");
    try {
      booking = mapper.convertValue(CommunicationService.RequestMirror(
          null,
          HttpMethod.GET,
          request,
          "/bookings/" + theatreRatingDto.getBookingId()),
          Booking.class);
    } catch (HttpClientErrorException e) {
      throw new NotFoundException("No booking found with booking id - " +
          theatreRatingDto.getBookingId());
    } catch (URISyntaxException e) {
      throw new ConnectionException("Unable to fetch booking");
    }

    if (booking.isCancelled()) {
      log.debug("Booking with booking id " + booking.getBookingId() + " was cancelled, Cannot " +
          "proceed with rating");
      throw new IllegalArgumentException("Cannot rate the theatre as the booking was cancelled.");
    }

    TheatreRating theatreRatingFound = null;
    try {
      theatreRatingFound = theatreRatingRepository.findByBooking(booking).get();
    } catch (NoSuchElementException e) {
      log.debug("No rating found with the booking id " + booking.getBookingId() + ", hence theatre"
          + " can be rated");
    }
    if (theatreRatingFound != null) {
      log.debug("A rating of " + theatreRatingFound.getRating() +
          "already exists with booking Id " + theatreRatingDto.getBookingId());
      throw new IllegalArgumentException("Rating already exists");
    }

    LocalDate currentDate = LocalDate.now();
    LocalDate slotDate = convertToLocalDateViaSqlDate(booking.getSlot().getSlotDate());
    LocalTime currentTime = LocalTime.now();
    LocalTime slotTime = convertToLocalDateViaSqlTime(booking.getSlot().getSlotTime());

    System.out.println(currentDate.compareTo(slotDate));

    if (currentDate.compareTo(slotDate) < 0 || (currentDate.compareTo(slotDate) == 0 && currentTime
        .isBefore(slotTime)))
      throw new IllegalArgumentException("Cannot rate the theatre before the show timings.");

    log.debug("Fetching theatre");
    Theatre theatre = mapper.convertValue(CommunicationService.RequestMirror(
        null,
        HttpMethod.GET,
        request,
        "/theatres/" + booking.getTheatreName() + "/details"),
        Theatre.class);
    int numOfRatings = theatre.getTheatreRatings().size();
    float avgRating = (theatre.getTheatreRating() * numOfRatings + theatreRatingDto.getRating())
        / (numOfRatings + 1);

    log.info("Updating average theatre rating");
    try {
      CommunicationService.RequestMirror(
          null,
          HttpMethod.PUT,
          request,
          "/theatres/" + theatre.getTheatreId() + "/rating/" + avgRating);
    } catch (UnknownContentTypeException e) {
      log.debug("Updated theatre rating");
    }

    TheatreRating theatreRating = new TheatreRating(booking, theatre, theatreRatingDto.getRating());
    theatreRatingRepository.save(theatreRating);

    return "Theatre rating added successfully!";
  }

  public List<MovieRating> findAllMovieRating() throws NotFoundException {
    log.info("Fetching all movie ratings");
    List<MovieRating> movieRatings = movieRatingRepository.findAll();
    if (movieRatings.isEmpty())
      throw new NotFoundException("No movie ratings found");
    return movieRatings;
  }

  public List<TheatreRating> findAllTheatreRating() throws NotFoundException {
    log.info("Fetching all theatre ratings");
    List<TheatreRating> theatreRatings = theatreRatingRepository.findAll();
    if (theatreRatings.isEmpty())
      throw new NotFoundException("No theatre ratings found");
    return theatreRatings;
  }

  public MovieRating findByMovieRatingId(int id) throws NotFoundException {
    log.debug("Fetching rating by movie rating id " + id);
    Optional<MovieRating> result = movieRatingRepository.findById(id);
    if (result.isPresent())
      return result.get();
    else
      throw new NotFoundException("Did not find movie rating with id - " + id);
  }

  public TheatreRating findByTheatreRatingId(int id) throws NotFoundException {
    log.debug("Fetching rating by theatre rating id " + id);
    Optional<TheatreRating> result = theatreRatingRepository.findById(id);
    if (result.isPresent())
      return result.get();
    else
      throw new NotFoundException("Did not find theatre rating with id - " + id);
  }

  public LocalDate convertToLocalDateViaSqlDate(Date dateToConvert) {
    return new java.sql.Date(dateToConvert.getTime()).toLocalDate();
  }

  public LocalTime convertToLocalDateViaSqlTime(Time timeToConvert) {
    return new java.sql.Time(timeToConvert.getTime()).toLocalTime();
  }

}
