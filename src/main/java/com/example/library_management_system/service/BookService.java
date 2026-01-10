package com.example.library_management_system.service;

import com.example.library_management_system.domain.entity.Book;
import com.example.library_management_system.domain.entity.Category;
import com.example.library_management_system.repository.BookRepository;
import com.example.library_management_system.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public Book create(Book book) {
        if (book.getAvailableCount() == null) {
            book.setAvailableCount(book.getTotalCount() == null ? 0 : book.getTotalCount());
        }
        // titlePinyin 在实体 @PrePersist 中会自动生成
        return bookRepository.save(book);
    }

    @Transactional
    public Book create(Book book, List<Long> categoryIds, String legacyCategoryName) {
        if (book.getAvailableCount() == null) {
            book.setAvailableCount(book.getTotalCount() == null ? 0 : book.getTotalCount());
        }

        Set<Category> categories = resolveCategories(categoryIds, legacyCategoryName);
        book.setCategories(categories);

        return bookRepository.save(book);
    }

    @Transactional
    public Book update(Long id, Book req) {
        Book book = getById(id);
        book.setIsbn(req.getIsbn());
        book.setTitle(req.getTitle());
        // titlePinyin 在实体 @PreUpdate 中会自动同步
        book.setAuthor(req.getAuthor());
        book.setCategory(req.getCategory());
        book.setPublisher(req.getPublisher());
        book.setDescription(req.getDescription());
        book.setCoverUrl(req.getCoverUrl());

        if (req.getTotalCount() != null) {
            int delta = req.getTotalCount() - book.getTotalCount();
            book.setTotalCount(req.getTotalCount());
            book.setAvailableCount(Math.max(0, book.getAvailableCount() + delta));
        }

        return bookRepository.save(book);
    }

    @Transactional
    public Book update(Long id, Book req, List<Long> categoryIds, String legacyCategoryName) {
        Book book = update(id, req);
        Set<Category> categories = resolveCategories(categoryIds, legacyCategoryName);
        book.setCategories(categories);
        return bookRepository.save(book);
    }

    @Transactional
    public void delete(Long id) {
        bookRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Book getById(Long id) {
        return bookRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Book not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Book> search(String keyword, String category, Pageable pageable) {
        if (keyword != null && !keyword.isBlank()) {
            return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase(
                    keyword, keyword, keyword, pageable
            );
        }
        if (category != null && !category.isBlank()) {
            return bookRepository.findByCategoryContainingIgnoreCase(category, pageable);
        }
        return bookRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Book> search(String keyword, String legacyCategory, Long categoryId, Pageable pageable) {
        if (keyword != null && !keyword.isBlank()) {
            return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase(
                    keyword, keyword, keyword, pageable
            );
        }
        if (categoryId != null) {
            return bookRepository.findByCategoryId(categoryId, pageable);
        }
        // 兼容旧参数 category（字符串）
        if (legacyCategory != null && !legacyCategory.isBlank()) {
            return bookRepository.findByCategoryContainingIgnoreCase(legacyCategory, pageable);
        }
        return bookRepository.findAll(pageable);
    }

    private Set<Category> resolveCategories(List<Long> categoryIds, String legacyCategoryName) {
        Set<Category> categories = new LinkedHashSet<>();

        if (categoryIds != null && !categoryIds.isEmpty()) {
            List<Category> found = categoryRepository.findAllById(categoryIds);
            if (found.size() != categoryIds.size()) {
                throw new IllegalArgumentException("部分分类不存在");
            }
            categories.addAll(found);
            return categories;
        }

        // 兼容旧接口：如果传了 category 字符串，则按名称匹配，不存在则创建
        if (legacyCategoryName != null && !legacyCategoryName.trim().isEmpty()) {
            String normalized = legacyCategoryName.trim();
            if (normalized.length() > 32) {
                throw new IllegalArgumentException("分类名称长度不能超过32");
            }
            Category category = categoryRepository.findByName(normalized)
                    .orElseGet(() -> categoryRepository.save(Category.builder().name(normalized).build()));
            categories.add(category);
        }

        return categories;
    }
}

