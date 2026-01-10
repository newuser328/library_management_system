package com.example.library_management_system.repository;

import com.example.library_management_system.domain.entity.User;
import com.example.library_management_system.domain.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByPhone(String phone);

    Page<User> findByRole(UserRole role, Pageable pageable);

    Page<User> findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCase(String usernameKeyword, String nameKeyword, Pageable pageable);
}

