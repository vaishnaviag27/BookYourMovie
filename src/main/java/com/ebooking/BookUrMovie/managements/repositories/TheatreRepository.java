package com.ebooking.BookUrMovie.managements.repositories;

import com.ebooking.BookUrMovie.commons.models.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TheatreRepository extends JpaRepository<Theatre, Integer> {
    Theatre findByTheatreName(String name);

    Theatre findByTheatreId(int id);

    boolean existsByTheatreName(String name);

    @Modifying
    @Transactional
    @Query(value = "update theatre set theatre_rating = ?2 where theatre_id = ?1", nativeQuery = true)
    void updateTheatreRating(int theatreId, float theatreRating);

    @Query(value = "select * from theatre where theatre_location = ?1", nativeQuery = true)
    List<Theatre> getTheatresByLocation(String location);
}
