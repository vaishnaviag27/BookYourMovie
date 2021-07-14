package com.ebooking.BookUrMovie.managements.controllers;

import com.ebooking.BookUrMovie.commons.exceptions.BadDetailsException;
import com.ebooking.BookUrMovie.commons.exceptions.NotFoundException;
import com.ebooking.BookUrMovie.commons.models.Movie;
import com.ebooking.BookUrMovie.managements.services.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/movies")
public class MovieController {

    @Autowired
    MovieService movieService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<Object> getAllMovies() {
        try {
            return new ResponseEntity<>(movieService.findAllMovies(), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/es/{name}")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<Object> getAllMoviesES(@PathVariable("name") String movieName) {
        try {
            return new ResponseEntity<>(movieService.findByMovieNameES(movieName), HttpStatus.OK);
        } catch (IOException ioException) {
            return new ResponseEntity<>(ioException.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{movieId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Object> getMovieById(@PathVariable("movieId") int movieId) {
        try {
            return new ResponseEntity<>(movieService.findByMovieId(movieId), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{movieName}/details")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<Object> getMovieByName(@PathVariable("movieName") String movieName) {
        try {
            return new ResponseEntity<>(movieService.findByMovieName(movieName), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> addMovie(@RequestBody Movie newMovie) {
        try {
            return new ResponseEntity<>(movieService.addMovie(newMovie), HttpStatus.ACCEPTED);
        } catch (BadDetailsException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @PostMapping("/transfer/es")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Object> addExistingMovies() {
        try {
            return new ResponseEntity<>(movieService.addExistingMovies(), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> updateMovie(@RequestBody Movie updatedMovie) {
        try {
            return new ResponseEntity<>(movieService.updateMovie(updatedMovie), HttpStatus.OK);
        } catch (BadDetailsException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{movieId}/rating/{rating}")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<String> updateMovieRating(@PathVariable("movieId") int movieId, @PathVariable("rating") float movieRating) {
        try {
            return new ResponseEntity<>(movieService.updateMovieRating(movieId, movieRating), HttpStatus.OK);
        } catch (NotFoundException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
