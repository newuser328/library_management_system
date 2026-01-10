package com.example.library_management_system.repository;

import com.example.library_management_system.domain.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    @EntityGraph(attributePaths = {"categories"})
    Optional<Book> findWithCategoriesById(Long id);

    @EntityGraph(attributePaths = {"categories"})
    Page<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase(
            String titleKeyword,
            String authorKeyword,
            String isbnKeyword,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"categories"})
    Page<Book> findByCategoryContainingIgnoreCase(String categoryKeyword, Pageable pageable);

    @EntityGraph(attributePaths = {"categories"})
    Page<Book> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"categories"})
    @Query("select distinct b from Book b join b.categories c where c.id = :categoryId")
    Page<Book> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);
}

