package com.ebooking.BookUrMovie.managements.repositories;

import com.ebooking.BookUrMovie.commons.models.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SeatRepository extends JpaRepository<Seat, Integer> {

    Seat findBySeatId(int seatId);

    @Modifying
    @Query(value = "delete from seat where slot_slot_id = ?1", nativeQuery = true)
    void deleteSeatsBySlotId(int slotId);
}

