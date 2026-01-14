package com.example.library_management_system.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponse {
    private long userCount;
    private long todayBorrowCount;
    private long monthlyBorrowCount;
    private long categoryCount;
}
