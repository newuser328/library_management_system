package com.example.library_management_system.web.controller;

import com.example.library_management_system.domain.entity.Book;
import com.example.library_management_system.service.BookService;
import com.example.library_management_system.web.dto.ApiResponse;
import com.example.library_management_system.web.dto.BookCreateUpdateRequest;
import com.example.library_management_system.web.dto.BookResponse;
import com.example.library_management_system.web.dto.CategoryResponse;
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
    public ApiResponse<BookResponse> create(@Valid @RequestBody BookCreateUpdateRequest req) {
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
        Book createdBook = bookService.create(book, req.getCategoryIds(), req.getCategory());
        return ApiResponse.ok(toBookResponse(createdBook));
    }

    @PutMapping("/{id}")
    public ApiResponse<BookResponse> update(@PathVariable Long id, @Valid @RequestBody BookCreateUpdateRequest req) {
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
        Book updated = bookService.update(id, book, req.getCategoryIds(), req.getCategory());
        return ApiResponse.ok(toBookResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        bookService.delete(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}")
    public ApiResponse<BookResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(toBookResponse(bookService.getById(id)));
    }

    @GetMapping
    public ApiResponse<Page<BookResponse>> search(
            @RequestParam(required = false) String keyword,
            // 兼容旧参数：category（字符串）
            @RequestParam(required = false) String category,
            // 新参数：categoryId（分类 id）
            @RequestParam(required = false) Long categoryId,
            Pageable pageable
    ) {
        Page<BookResponse> page = bookService.search(keyword, category, categoryId, pageable)
                .map(this::toBookResponse);
        return ApiResponse.ok(page);
    }

    private BookResponse toBookResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .category(book.getCategory())
                .categories(book.getCategories() == null ? java.util.List.of() : book.getCategories().stream()
                        .map(c -> new CategoryResponse(c.getId(), c.getName()))
                        .toList())
                .publisher(book.getPublisher())
                .description(book.getDescription())
                .coverUrl(book.getCoverUrl())
                .totalCount(book.getTotalCount())
                .availableCount(book.getAvailableCount())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }
}

