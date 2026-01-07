package com.example.library_management_system.web.controller;

import com.example.library_management_system.domain.entity.Book;
import com.example.library_management_system.service.BookService;
import com.example.library_management_system.web.dto.ApiResponse;
import com.example.library_management_system.web.dto.BookCreateUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping
    public ApiResponse<Book> create(@Valid @RequestBody BookCreateUpdateRequest req) {
        Book book = Book.builder()
                .isbn(req.getIsbn())
                .title(req.getTitle())
                .author(req.getAuthor())
                .category(req.getCategory())
                .publisher(req.getPublisher())
                .description(req.getDescription())
                .coverUrl(req.getCoverUrl())
                .totalCount(req.getTotalCount())
                .availableCount(req.getTotalCount())
                .build();
        return ApiResponse.ok(bookService.create(book));
    }

    @PutMapping("/{id}")
    public ApiResponse<Book> update(@PathVariable Long id, @Valid @RequestBody BookCreateUpdateRequest req) {
        Book book = Book.builder()
                .isbn(req.getIsbn())
                .title(req.getTitle())
                .author(req.getAuthor())
                .category(req.getCategory())
                .publisher(req.getPublisher())
                .description(req.getDescription())
                .coverUrl(req.getCoverUrl())
                .totalCount(req.getTotalCount())
                .build();
        return ApiResponse.ok(bookService.update(id, book));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        bookService.delete(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}")
    public ApiResponse<Book> get(@PathVariable Long id) {
        return ApiResponse.ok(bookService.getById(id));
    }

    @GetMapping
    public ApiResponse<Page<Book>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            Pageable pageable
    ) {
        return ApiResponse.ok(bookService.search(keyword, category, pageable));
    }
}

