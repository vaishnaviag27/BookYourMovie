package com.ebooking.BookUrMovie.accounts.repositories;

import com.ebooking.BookUrMovie.commons.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUserEmail(String userEmail);
    User findByUserId(int userId);
    boolean existsByUserEmail(String userEmail);
}
