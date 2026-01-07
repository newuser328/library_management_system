package com.example.library_management_system.repository;

import com.example.library_management_system.domain.entity.Book;
import com.example.library_management_system.domain.entity.Borrow;
import com.example.library_management_system.domain.entity.User;
import com.example.library_management_system.domain.enums.BorrowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BorrowRepository extends JpaRepository<Borrow, Long> {

    @EntityGraph(attributePaths = {"user", "book"})
    Optional<Borrow> findWithRelationsById(Long id);

    @EntityGraph(attributePaths = {"user", "book"})
    Page<Borrow> findByUser(User user, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "book"})
    Page<Borrow> findByBook(Book book, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "book"})
    Page<Borrow> findByStatus(BorrowStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "book"})
    List<Borrow> findByStatusAndDueDateBefore(BorrowStatus status, LocalDate dueDate);

    boolean existsByUserAndBookAndStatusIn(User user, Book book, List<BorrowStatus> statuses);

    @EntityGraph(attributePaths = {"user", "book"})
    Page<Borrow> findByBook_TitlePinyinStartingWithIgnoreCase(String titleInitial, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "book"})
    Page<Borrow> findByStatusAndBook_TitlePinyinStartingWithIgnoreCase(BorrowStatus status, String titleInitial, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"user", "book"})
    Page<Borrow> findAll(Pageable pageable);
}
