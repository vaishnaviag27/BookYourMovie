package com.ebooking.BookUrMovie.commons.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int movieId;

    @Column(unique = true)
    private String movieName;

    private String movieDescription;

    private float movieRating;

    private float movieDuration;

    private String movieGenre;

    private String movieLanguage;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    @JsonIgnore
    List<Slot> slots = new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    List<MovieRating> movieRatings = new ArrayList<>();

    public Movie(String movieName, String movieDescription, float movieRating, float movieDuration, String movieGenre, String movieLanguage) {
        this.movieName = movieName;
        this.movieDescription = movieDescription;
        this.movieRating = movieRating;
        this.movieDuration = movieDuration;
        this.movieGenre = movieGenre;
        this.movieLanguage = movieLanguage;
    }
}
