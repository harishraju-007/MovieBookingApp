package com.app.movieBookingSystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.app.movieBookingSystem.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByLoginId(String loginId);
    boolean existsByRole(String role);
}