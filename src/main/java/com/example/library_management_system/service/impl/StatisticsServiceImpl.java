package com.example.library_management_system.service.impl;

import com.example.library_management_system.domain.enums.UserRole;
import com.example.library_management_system.repository.BorrowRepository;
import com.example.library_management_system.repository.CategoryRepository;
import com.example.library_management_system.repository.UserRepository;
import com.example.library_management_system.service.StatisticsService;
import com.example.library_management_system.web.dto.StatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final UserRepository userRepository;
    private final BorrowRepository borrowRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public StatisticsResponse getStatistics() {
        long userCount = userRepository.countByRole(UserRole.READER);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = todayStart.plusDays(1);
        long todayBorrowCount = borrowRepository.countByRequestTimeBetween(todayStart, tomorrowStart);

        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime nextMonthStart = monthStart.plusMonths(1);
        long monthlyBorrowCount = borrowRepository.countByRequestTimeBetween(monthStart, nextMonthStart);

        long categoryCount = categoryRepository.count();

        return StatisticsResponse.builder()
                .userCount(userCount)
                .todayBorrowCount(todayBorrowCount)
                .monthlyBorrowCount(monthlyBorrowCount)
                .categoryCount(categoryCount)
                .build();
    }
}
