package com.ebooking.BookUrMovie.feedback.dto;

import com.ebooking.BookUrMovie.commons.models.Movie;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MovieDTO {

  private String movieName;

  private String movieDescription;

  private float movieRating;

  private float movieDuration;

  private String movieGenre;

  private String movieLanguage;

  private Double significanceScore;

  public MovieDTO(Movie movie, Double score) {
    this.movieName = movie.getMovieName();
    this.movieDescription = movie.getMovieDescription();
    this.movieRating = movie.getMovieRating();
    this.movieDuration = movie.getMovieDuration();
    this.movieGenre = movie.getMovieGenre();
    this.movieLanguage = movie.getMovieLanguage();
    this.significanceScore = score;
  }
}
