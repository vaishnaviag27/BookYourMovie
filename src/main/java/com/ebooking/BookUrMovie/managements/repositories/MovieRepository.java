package com.ebooking.BookUrMovie.managements.repositories;

import com.ebooking.BookUrMovie.commons.models.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface MovieRepository extends JpaRepository<Movie, Integer> {
    Movie findByMovieName(String name);

    Movie findByMovieId(int movieName);

    boolean existsByMovieName(String name);

    @Modifying
    @Transactional
    @Query(value = "update movie set movie_rating = ?2 where movie_id = ?1", nativeQuery = true)
    void updateMovieRating(int movieId, float movieRating);
}
