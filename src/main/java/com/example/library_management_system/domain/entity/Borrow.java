package com.example.library_management_system.domain.entity;

import com.example.library_management_system.domain.enums.BorrowStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "borrows")
public class Borrow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 避免序列化时因LAZY代理/循环引用导致 HttpMessageNotWritableException
    @JsonIgnoreProperties({"password", "createdAt", "updatedAt", "hibernateLazyInitializer", "handler"})
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 避免序列化时因LAZY代理/循环引用导致 HttpMessageNotWritableException
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BorrowStatus status;

    @Column(nullable = false)
    private LocalDateTime requestTime;

    private LocalDateTime approveTime;

    private LocalDateTime rejectTime;

    private LocalDateTime returnTime;

    private LocalDate dueDate;

    @Column(length = 255)
    private String remark;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = BorrowStatus.PENDING;
        }
        if (requestTime == null) {
            requestTime = LocalDateTime.now();
        }
    }
}
