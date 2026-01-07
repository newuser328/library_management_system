package com.example.library_management_system.service;

import com.example.library_management_system.domain.entity.User;
import com.example.library_management_system.domain.enums.UserRole;
import com.example.library_management_system.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User create(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public User update(Long id, User req) {
        User user = getById(id);
        user.setUsername(req.getUsername());
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        user.setName(req.getName());
        user.setPhone(req.getPhone());
        user.setEmail(req.getEmail());
        user.setRole(req.getRole() == null ? UserRole.READER : req.getRole());
        if (req.getEnabled() != null) {
            user.setEnabled(req.getEnabled());
        }
        return userRepository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<User> search(String keyword, UserRole role, Pageable pageable) {
        if (role != null) {
            return userRepository.findByRole(role, pageable);
        }
        if (keyword != null && !keyword.isBlank()) {
            return userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCase(keyword, keyword, pageable);
        }
        return userRepository.findAll(pageable);
    }
}

