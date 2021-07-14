package com.ebooking.BookUrMovie.managements.repositories;

import com.ebooking.BookUrMovie.commons.models.AddOn;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddOnRepository extends JpaRepository<AddOn, String> {
    AddOn findByAddonName(String name);
    Boolean existsByAddonName(String name);
    void deleteByAddonName(String name);
}
