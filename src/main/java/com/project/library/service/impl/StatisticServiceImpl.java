package com.project.library.service.impl;

import com.project.library.converter.BookMapper;
import com.project.library.dto.response.*;
import com.project.library.exception.BusinessException;
import com.project.library.model.Book;
import com.project.library.model.Student;
import com.project.library.repository.BookRepository;
import com.project.library.repository.StatisticsRepository;
import com.project.library.service.StatisticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {

    private final StatisticsRepository statisticsRepository;
    private final BookRepository bookRepository;
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

    @Override
    @Transactional(readOnly = true)
    public MonthlyReportResponse getMonthlyReport(String month) {
        log.debug("Get monthly report for: {}", month);

        LocalDate startDate;
        LocalDate endDate;

        try {
            YearMonth yearMonth = YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"));
            startDate = yearMonth.atDay(1);
            endDate = yearMonth.atEndOfMonth();
        } catch (DateTimeParseException e) {
            throw new BusinessException("Invalid month format. Use YYYY-MM (e.g., 2026-01)");
        }

        log.debug("Processing report from {} to {}", startDate, endDate);

        Long totalBorrows = statisticsRepository.getBorrowsInPeriod(startDate, endDate);
        Long totalReturns = statisticsRepository.getReturnsInPeriod(startDate, endDate);
        Long newStudents = statisticsRepository.getNewStudentsInMonth(startDate, endDate);
        Long newBooks = statisticsRepository.getNewBooksInMonth(startDate, endDate);
        List<Object[]> topBooksData = bookRepository.findMostBorrowedBooksInPeriod(
                startDate, endDate, 5);
        List<MostBorrowedBookResponse> topBooks = convertToMostBorrowedResponse(topBooksData);
        List<Object[]> topStudentsData = statisticsRepository.getTopActiveStudents(
                startDate, endDate, 5);
        List<StudentRankingResponse> topStudents = convertToStudentRanking(topStudentsData);
        Long currentOverdue = statisticsRepository.getOverdueInPeriod(startDate, endDate);
        Double overdueRate = totalBorrows > 0
                ? (currentOverdue.doubleValue() / totalBorrows.doubleValue()) * 100
                : 0.0;
        Long totalActiveStudents = statisticsRepository.getActiveStudents();
        Long totalAvailableBooks = statisticsRepository.getTotalAvailableBooks();

        log.info("Monthly report {} - Borrows: {}, Returns: {}, New Students: {}, New Books: {}, Overdue Rate: {}%",
                month, totalBorrows, totalReturns, newStudents, newBooks,
                Math.round(overdueRate * 100.0) / 100.0);

        return MonthlyReportResponse.builder()
                .months(month)
                .totalBorrows(totalBorrows)
                .totalReturns(totalReturns)
                .newStudents(newStudents)
                .newBooks(newBooks)
                .topBorrowedBooks(topBooks)
                .mostActiveStudents(topStudents)
                .overdueRate(Math.round(overdueRate * 100.0) / 100.0)
                .currentOverdue(currentOverdue)
                .totalActiveStudents(totalActiveStudents)
                .totalAvailableBooks(totalAvailableBooks)
                .build();
    }
    /**
     * Convert query results to MostBorrowedBookResponse list
     * @param results [Book, borrowCount, currentBorrowing]
     */
    private List<MostBorrowedBookResponse> convertToMostBorrowedResponse(List<Object[]> results) {
        List<MostBorrowedBookResponse> responses = new ArrayList<>();
        int rank = 1;

        for (Object[] result : results) {
            Book book = (Book) result[0];
            Long totalBorrowCount = ((Number) result[1]).longValue();
            Long currentBorrowing = ((Number) result[2]).longValue();

            BookSummaryResponse bookSummary = BookMapper.toSummaryResponse(book);

            responses.add(MostBorrowedBookResponse.builder()
                    .rank(rank++)
                    .book(bookSummary)
                    .totalBorrowCount(totalBorrowCount)
                    .currentBorrowingCount(currentBorrowing)
                    .build());
        }

        return responses;
    }

    /**
     * Convert query results to StudentRankingResponse list
     * @param results [Student, totalBorrows, overdueCount]
     */
    private List<StudentRankingResponse> convertToStudentRanking(List<Object[]> results) {
        List<StudentRankingResponse> responses = new ArrayList<>();
        int rank = 1;

        for (Object[] result : results) {
            Student student = (Student) result[0];
            Long totalBooksRead = ((Number) result[1]).longValue();
            Long overdueCount = ((Number) result[2]).longValue();

            responses.add(StudentRankingResponse.builder()
                    .rank(rank++)
                    .studentCode(student.getStudentCode())
                    .fullName(student.getFullName())
                    .email(student.getEmail())
                    .totalBookRead(totalBooksRead)
                    .overdueCount(overdueCount)
                    .neverOverdue(overdueCount == 0)
                    .build());
        }

        return responses;
    }

}
