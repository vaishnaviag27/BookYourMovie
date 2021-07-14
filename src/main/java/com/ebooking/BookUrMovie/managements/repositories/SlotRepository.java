package com.ebooking.BookUrMovie.managements.repositories;

import com.ebooking.BookUrMovie.commons.models.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SlotRepository extends JpaRepository<Slot, Integer> {
    Slot findBySlotId(int slotId);

    @Query(value = "select * from slot where movie_movie_id = ?1 AND theatre_theatre_id = ?2", nativeQuery = true)
    List<Slot> findSlotByMovieIdAndTheatreId(int movieId, int theatreId);

    @Query(value = "select m.movie_name from movie m inner join slot s on s.movie_movie_id=m.movie_id " +
        "where s.slot_id=?1", nativeQuery = true)
    String findMovieNameBySlotId(int slotId);

    @Query(value = "select t.theatre_name from theatre t inner join slot s on s.theatre_theatre_id=t.theatre_id " +
        "where s.slot_id=?1", nativeQuery = true)
    String findTheatreNameBySlotId(int slotId);

    @Query(value = "select * from slot where DATEDIFF(slot_date, CURDATE()) < 0 AND is_outdated = false", nativeQuery = true)
    List<Slot> findOutdatedSlots();

    @Query(value = "SELECT * from slot s INNER JOIN theatre t on t.theatre_id = s.theatre_theatre_id " +
        "where t.theatre_location = ?1 AND DATEDIFF(slot_date, CURDATE()) >= 0 order by movie_movie_id",
        nativeQuery = true)

    List<Slot> getSlotsByLocation(String userLocation);
    @Query(value = "SELECT * from slot s INNER JOIN theatre t on t.theatre_id = " +
        "s.theatre_theatre_id INNER JOIN movie m ON s.movie_movie_id = m.movie_id where m.movie_name =?1 " +
        "AND t.theatre_location = ?2 AND DATEDIFF(slot_date, CURDATE()) >= 0", nativeQuery = true)
    List<Slot> getSlotsByMovieNameAndLocation(String movieName, String userLocation);
}
