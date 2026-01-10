package com.example.library_management_system.service;

import com.example.library_management_system.domain.entity.Book;
import com.example.library_management_system.domain.entity.Borrow;
import com.example.library_management_system.domain.entity.User;
import com.example.library_management_system.domain.enums.BorrowStatus;
import com.example.library_management_system.repository.BookRepository;
import com.example.library_management_system.repository.BorrowRepository;
import com.example.library_management_system.security.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowService {

    private static final int DEFAULT_BORROW_DAYS = 30;
    private static final int MAX_ACTIVE_BORROWS_PER_USER = 3;

    private final BorrowRepository borrowRepository;
    private final BookRepository bookRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public Borrow requestBorrow(Long bookId, String remark) {
        User user = securityUtils.currentUser(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication());

        // 每人最多 3 本未归还（PENDING + APPROVED 都算在内）
        long activeCount = borrowRepository.countByUserAndStatusIn(user, List.of(BorrowStatus.PENDING, BorrowStatus.APPROVED));
        if (activeCount >= MAX_ACTIVE_BORROWS_PER_USER) {
            throw new IllegalStateException("每个人一次最多可借三本书");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found: " + bookId));

        if (book.getAvailableCount() == null || book.getAvailableCount() <= 0) {
            throw new IllegalStateException("Book is not available");
        }

        boolean exists = borrowRepository.existsByUserAndBookAndStatusIn(
                user,
                book,
                List.of(BorrowStatus.PENDING, BorrowStatus.APPROVED)
        );
        if (exists) {
            throw new IllegalStateException("You already have a pending/approved borrow for this book");
        }

        Borrow borrow = Borrow.builder()
                .user(user)
                .book(book)
                .status(BorrowStatus.PENDING)
                .requestTime(LocalDateTime.now())
                .remark(remark)
                .build();

        return borrowRepository.save(borrow);
    }

    @Transactional
    public Borrow cancelMyBorrow(Long borrowId) {
        User user = securityUtils.currentUser(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication());
        Borrow borrow = getByIdWithRelations(borrowId);

        if (!borrow.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("无权限取消该借阅申请");
        }
        if (borrow.getStatus() != BorrowStatus.PENDING) {
            throw new IllegalStateException("仅待审核的申请可取消");
        }

        borrow.setStatus(BorrowStatus.CANCELLED);
        // 管理端展示：标记为用户取消
        borrow.setRemark("用户取消借阅");
        // 取消不设置 rejectTime/returnTime，保留 requestTime 作为申请时间
        return borrowRepository.save(borrow);
    }

    @Transactional
    public Borrow approve(Long borrowId) {
        Borrow borrow = getByIdWithRelations(borrowId);
        if (borrow.getStatus() != BorrowStatus.PENDING) {
            throw new IllegalStateException("Only PENDING borrow can be approved");
        }

        Book book = borrow.getBook();
        if (book.getAvailableCount() == null || book.getAvailableCount() <= 0) {
            throw new IllegalStateException("Book is not available");
        }

        book.setAvailableCount(book.getAvailableCount() - 1);
        bookRepository.save(book);

        borrow.setStatus(BorrowStatus.APPROVED);
        borrow.setApproveTime(LocalDateTime.now());
        borrow.setDueDate(LocalDate.now().plusDays(DEFAULT_BORROW_DAYS));
        return borrowRepository.save(borrow);
    }

    @Transactional
    public Borrow reject(Long borrowId, String remark) {
        Borrow borrow = getByIdWithRelations(borrowId);
        if (borrow.getStatus() != BorrowStatus.PENDING) {
            throw new IllegalStateException("Only PENDING borrow can be rejected");
        }
        borrow.setStatus(BorrowStatus.REJECTED);
        borrow.setRejectTime(LocalDateTime.now());
        if (remark != null && !remark.isBlank()) {
            borrow.setRemark(remark);
        }
        return borrowRepository.save(borrow);
    }

    @Transactional
    public Borrow markReturned(Long borrowId) {
        Borrow borrow = getByIdWithRelations(borrowId);
        if (borrow.getStatus() != BorrowStatus.APPROVED) {
            throw new IllegalStateException("Only APPROVED borrow can be returned");
        }

        Book book = borrow.getBook();
        book.setAvailableCount(book.getAvailableCount() + 1);
        bookRepository.save(book);

        borrow.setStatus(BorrowStatus.RETURNED);
        borrow.setReturnTime(LocalDateTime.now());
        return borrowRepository.save(borrow);
    }

    @Transactional(readOnly = true)
    public Borrow getById(Long id) {
        return borrowRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Borrow not found: " + id));
    }

    @Transactional(readOnly = true)
    public Borrow getByIdWithRelations(Long id) {
        return borrowRepository.findWithRelationsById(id)
                .orElseThrow(() -> new EntityNotFoundException("Borrow not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Borrow> listAll(BorrowStatus status, String titleInitial, Pageable pageable) {
        if (status != null && titleInitial != null && !titleInitial.isBlank()) {
            return borrowRepository.findByStatusAndBook_TitlePinyinStartingWithIgnoreCase(status, titleInitial.trim(), pageable);
        }
        if (status != null) {
            return borrowRepository.findByStatus(status, pageable);
        }
        if (titleInitial != null && !titleInitial.isBlank()) {
            return borrowRepository.findByBook_TitlePinyinStartingWithIgnoreCase(titleInitial.trim(), pageable);
        }
        return borrowRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Borrow> listMyBorrows(BorrowStatus status, Pageable pageable) {
        User user = securityUtils.currentUser(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication());
        if (status != null) {
            return borrowRepository.findByUserAndStatus(user, status, pageable);
        }
        return borrowRepository.findByUser(user, pageable);
    }
}
