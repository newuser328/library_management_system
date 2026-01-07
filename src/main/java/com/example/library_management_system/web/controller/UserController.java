package com.example.library_management_system.web.controller;

import com.example.library_management_system.domain.entity.User;
import com.example.library_management_system.domain.enums.UserRole;
import com.example.library_management_system.service.UserService;
import com.example.library_management_system.web.dto.ApiResponse;
import com.example.library_management_system.web.dto.UserCreateUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ApiResponse<User> create(@Valid @RequestBody UserCreateUpdateRequest req) {
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new IllegalArgumentException("password is required");
        }
        User user = User.builder()
                .username(req.getUsername())
                .password(req.getPassword())
                .name(req.getName())
                .phone(req.getPhone())
                .email(req.getEmail())
                .role(req.getRole())
                .enabled(req.getEnabled())
                .build();
        return ApiResponse.ok(userService.create(user));
    }

    @PutMapping("/{id}")
    public ApiResponse<User> update(@PathVariable Long id, @Valid @RequestBody UserCreateUpdateRequest req) {
        User user = User.builder()
                .username(req.getUsername())
                .password(req.getPassword())
                .name(req.getName())
                .phone(req.getPhone())
                .email(req.getEmail())
                .role(req.getRole())
                .enabled(req.getEnabled())
                .build();
        return ApiResponse.ok(userService.update(id, user));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}")
    public ApiResponse<User> get(@PathVariable Long id) {
        return ApiResponse.ok(userService.getById(id));
    }

    @GetMapping
    public ApiResponse<Page<User>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserRole role,
            Pageable pageable
    ) {
        return ApiResponse.ok(userService.search(keyword, role, pageable));
    }
}

