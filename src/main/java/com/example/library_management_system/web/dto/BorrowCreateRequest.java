package com.example.library_management_system.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BorrowCreateRequest {
    @NotNull
    private Long bookId;

    private String remark;
}

