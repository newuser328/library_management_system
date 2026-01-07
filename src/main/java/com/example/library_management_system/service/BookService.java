package com.example.library_management_system.service;

import com.example.library_management_system.domain.entity.Book;
import com.example.library_management_system.repository.BookRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    @Transactional
    public Book create(Book book) {
        if (book.getAvailableCount() == null) {
            book.setAvailableCount(book.getTotalCount() == null ? 0 : book.getTotalCount());
        }
        // titlePinyin 在实体 @PrePersist 中会自动生成
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
}

