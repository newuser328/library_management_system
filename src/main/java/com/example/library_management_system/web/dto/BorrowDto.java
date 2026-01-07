package com.example.library_management_system.web.dto;

import com.example.library_management_system.domain.enums.BorrowStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class BorrowDto {

    private Long id;

    private BorrowStatus status;

    private LocalDateTime requestTime;

    private LocalDateTime approveTime;

    private LocalDateTime rejectTime;

    private LocalDateTime returnTime;

    private LocalDate dueDate;

    private String remark;

    private Long userId;
    private String username;

    private Long bookId;
    private String bookTitle;
    private String bookIsbn;
}

