package com.ebooking.BookUrMovie.managements.services;

import com.ebooking.BookUrMovie.commons.exceptions.BadDetailsException;
import com.ebooking.BookUrMovie.commons.exceptions.NotFoundException;
import com.ebooking.BookUrMovie.commons.models.Movie;

import com.ebooking.BookUrMovie.managements.repositories.MovieRepository;
import com.ebooking.BookUrMovie.managements.utils.NonNullObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

;

import com.ebooking.BookUrMovie.commons.models.MovieES;
import com.ebooking.BookUrMovie.commons.configurations.ElasticSearchConfiguration;
import com.ebooking.BookUrMovie.managements.repositories.MovieESRepository;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.QueryBuilders;

import java.io.IOException;
import java.util.ArrayList;

import java.util.List;

@Service
@Slf4j
public class MovieService {

    @Autowired
    ElasticSearchConfiguration elasticConfiguration;

    @Autowired
    MovieRepository movieRepository;

    @Autowired
    MovieESRepository movieESRepository;

    public Movie findByMovieName(String name) throws NotFoundException {
        log.debug("Fetching a movie with name: {}", name);
        Movie foundMovie = movieRepository.findByMovieName(name);
        if (foundMovie == null) {
            log.error("Movie not found with name: {}", name);
            throw new NotFoundException("Movie not found with name: " + name);
        }
        return foundMovie;
    }

    public List<Movie> findByMovieNameES(String name) throws IOException, NotFoundException {
        List<Movie> movies = new ArrayList<>();
        /**
         *  fuzziness AUTO
         *  0..2 characters - must match exactly
         *  3..5 characters - one edit allowed
         *  > 5 characters - two edit allowed
         */
        MatchQueryBuilder searchByMovieName =
            QueryBuilders.matchQuery("movieName", name).fuzziness(Fuzziness.AUTO);
        Iterable<MovieES> movieES = this.movieESRepository.search(searchByMovieName);
        for (MovieES movie : movieES) {
            movies.add(movieRepository.findByMovieName(movie.getMovieName()));
        }
        if(movies.isEmpty()) {
            throw new NotFoundException("No movie with " + name + " exist.");
        }
        return movies;
    }

    public Movie findByMovieId(int id) throws NotFoundException {
        log.debug("Fetching a movie with Id: {}", id);
        Movie foundMovie = movieRepository.findByMovieId(id);
        if (foundMovie == null) {
            log.error("Movie not found with id: {}", id);
            throw new NotFoundException("Movie not found with Id: " + id);
        }
        return foundMovie;
    }


    public String addExistingMovies() throws NotFoundException {
        List<Movie> movies = findAllMovies();
        if (movies.isEmpty()) {
            log.error("no movies found!");
            throw new NotFoundException("no movies found!");
        }
        int count = 0;
        for (Movie movie : movies) {
            movieESRepository.save(new MovieES(movie.getMovieId(), movie.getMovieName()));
            count++;
        }
        return count + " movies send to elastic search.";
    }

    public String addMovie(Movie movie) throws BadDetailsException, IllegalArgumentException {
        log.debug("Adding a movie with name: {}", movie.getMovieName());
        if (movieRepository.existsByMovieName(movie.getMovieName())) {
            log.error("Movie already Exists: {}", movie.getMovieName());
            throw new IllegalArgumentException("Movie Already Exists: " + movie.getMovieName());
        }
        String movieDetailsValidatorResponse = movieDetailsValidator(movie, true);
        if (!movieDetailsValidatorResponse.equals("valid")) {
            log.error("Invalid Movie Details: {}", movieDetailsValidatorResponse);
            throw new BadDetailsException(movieDetailsValidatorResponse);
        }
        movieRepository.save(movie);
        log.debug("Added movie successfully: {}", movie.getMovieName());
        movieESRepository.save(new MovieES(movie.getMovieId(), movie.getMovieName()));
        return "Added Movie: " + movie.getMovieName();
    }

    public String updateMovie(Movie updatedMovie) throws NotFoundException, BadDetailsException {
        log.debug("Updating a movie with Id: {}", updatedMovie.getMovieId());
        Movie foundMovie = movieRepository.findByMovieId(updatedMovie.getMovieId());
        if (foundMovie == null) {
            log.error("Movie does not exist with Id: {}", updatedMovie.getMovieId());
            throw new NotFoundException("Movie does not exist with Id: " + updatedMovie.getMovieId());
        }
        String movieDetailsValidatorResponse = movieDetailsValidator(updatedMovie, false);
        if (!movieDetailsValidatorResponse.equals("valid")) {
            log.error("Update Details invalid for movie with Id: {} because {}", updatedMovie.getMovieId(), movieDetailsValidatorResponse);
            throw new BadDetailsException(String.format("Update Details invalid for movie with Id: %d because %s",
                updatedMovie.getMovieId(),
                movieDetailsValidatorResponse));
        }
        updatedMovie.setMovieName(foundMovie.getMovieName());
        updatedMovie.setMovieRating(foundMovie.getMovieRating());
        NonNullObjectMapper.copyNonNullProperties(updatedMovie, foundMovie);
        movieRepository.save(foundMovie);
        log.debug("Updated Movie with Id: {}", updatedMovie.getMovieId());
        return "Movie Updated!";
    }

    public List<Movie> findAllMovies() throws NotFoundException {
        log.debug("Fetching All Movies");
        List<Movie> movieList = movieRepository.findAll();
        if (movieList.isEmpty()) {
            log.error("Movies not found");
            throw new NotFoundException("No Movies Found");
        }
        return movieList;
    }

    public String updateMovieRating(int movieId, float movieRating) throws NotFoundException {
        log.debug("Updating Movie Rating with Id: {}", movieId);
        if (movieRepository.existsById(movieId)) {
            movieRepository.updateMovieRating(movieId, movieRating);
            return "Updated Movie Rating!";
        } else {
            log.error("Movie Not found with Id: {}", movieId);
            throw new NotFoundException("Movie not found with Id: " + movieId);
        }
    }

    public String movieDetailsValidator(Movie movie, boolean isNewMovie) {
        if (isNewMovie) {
            if (movie.getMovieName() == null) {
                return "Enter Movie Name";
            }
            if (movie.getMovieLanguage() == null) {
                return "Enter Movie Language!";
            }
            if (movie.getMovieDuration() == 0.0f) {
                return "Enter Movie Duration!";
            }
            if (movie.getMovieGenre() == null) {
                return "Enter Movie Genre!";
            }
            if (movie.getMovieRating() != 0) {
                return "Movie Rating Must be Zero Initially!";
            }
            if (movie.getMovieDescription() == null) {
                return "Enter Movie Description!";
            }
        }

        if (movie.getMovieName() != null && movie.getMovieName().length() <= 1) {
            return "Movie name must be atleast 2 characters long";
        }
        if (movie.getMovieLanguage() != null && movie.getMovieLanguage().length() == 0) {
            return "Enter a Valid Movie Language";
        }
        if (movie.getMovieDuration() < 0.0f) {
            return "Movie Duration Cannot be Less than 0";
        }
        if (movie.getMovieGenre() != null && movie.getMovieGenre().length() == 0) {
            return "Enter a valid Movie Genre for the Movie!";
        }
        if (movie.getMovieRating() != 0.0f && (movie.getMovieRating() < 1 || movie.getMovieRating() > 5)) {
            return "Movie rating should be between  1 and 5;";
        }
        if (movie.getMovieDescription() != null && movie.getMovieDescription().length() < 20) {
            return "Movie description too short!";
        }
        return "valid";
    }
}
