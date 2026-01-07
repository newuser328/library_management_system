package com.example.library_management_system.repository;

import com.example.library_management_system.domain.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

    Page<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase(
            String titleKeyword,
            String authorKeyword,
            String isbnKeyword,
            Pageable pageable
    );

    Page<Book> findByCategoryContainingIgnoreCase(String categoryKeyword, Pageable pageable);
}

