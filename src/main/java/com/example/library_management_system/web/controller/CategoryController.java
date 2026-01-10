package com.example.library_management_system.web.controller;

import com.example.library_management_system.domain.entity.Category;
import com.example.library_management_system.service.CategoryService;
import com.example.library_management_system.web.dto.ApiResponse;
import com.example.library_management_system.web.dto.CategoryRequest;
import com.example.library_management_system.web.dto.CategoryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ApiResponse<List<CategoryResponse>> list() {
        List<CategoryResponse> list = categoryService.listAll().stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName()))
                .toList();
        return ApiResponse.ok(list);
    }

    @PostMapping
    public ApiResponse<CategoryResponse> create(@Valid @RequestBody CategoryRequest req) {
        Category category = categoryService.create(req.getName());
        return ApiResponse.ok(new CategoryResponse(category.getId(), category.getName()));
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest req) {
        Category category = categoryService.update(id, req.getName());
        return ApiResponse.ok(new CategoryResponse(category.getId(), category.getName()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ApiResponse.ok(null);
    }
}
