package com.project.library.service.impl;

import com.project.library.dto.response.DashboardStatsResponse;
import com.project.library.repository.StatisticsRepository;
import com.project.library.service.StatisticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
@Slf4j
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {

    private final StatisticsRepository statisticsRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {

        log.debug("Fetching dashboard statistics");

        Long totalStudents = statisticsRepository.getTotalStudents();
        Long activeStudents = statisticsRepository.getActiveStudents();
        Long suspendedStudents = statisticsRepository.getSuspendedStudents();
        Long totalBooks = statisticsRepository.getTotalBooks();
        Long availableBooks = statisticsRepository.getTotalAvailableBooks();
        Long totalCategories = statisticsRepository.getTotalCategories();
        Long currentBorrowing = statisticsRepository.getCurrentBorrowing();
        Long overdueBooks = statisticsRepository.getOverdueBooks();
        Long borrowsThisMonth = statisticsRepository.getBorrowsThisMonth();
        Long returnsThisMonth = statisticsRepository.getReturnsThisMonth();

        // ti le muon
        Double borrowingRate = totalBooks > 0
                ? (currentBorrowing.doubleValue()/totalBooks.doubleValue()) * 100 : 0.0;

        log.debug("Dashboard stats - Total students: {}, Active: {}, Current borrowing: {}, Overdue: {}",
                totalStudents, activeStudents, currentBorrowing, overdueBooks);

        return DashboardStatsResponse.builder()
                .totalStudents(totalStudents)
                .totalActiveStudents(activeStudents)
                .totalSuspendedStudents(suspendedStudents)
                .totalBooks(totalBooks)
                .totalAvailableBooks(availableBooks)
                .totalCategories(totalCategories)
                .currentBorrowing(currentBorrowing)
                .overdueBooks(overdueBooks)
                .totalBorrowsThisMonth(borrowsThisMonth)
                .totalReturnsThisMonth(returnsThisMonth)
                .borrowingRate(Math.round(borrowingRate * 100.0) / 100.0) // Round to 2 decimal places
                .lastUpdated(LocalDateTime.now())
                .build();
    }
}
