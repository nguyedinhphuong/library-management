package com.project.library.repository;


import com.project.library.model.BorrowRecord;
import com.project.library.utils.BorrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    long countByStudentIdAndStatus(Long studentId, BorrowStatus status);
    boolean existsByStudentIdAndBookIdAndStatus(Long studentId, Long bookId, BorrowStatus status);

    @Query("""
    SELECT br
    FROM BorrowRecord br
    WHERE br.returnDate IS NULL
      AND br.dueDate < :today
      AND br.status = 'BORROWING'
    """)
    List<BorrowRecord> findOverdueRecords(@Param("today") LocalDate today);

    List<BorrowRecord> findByStudentIdAndStatus(Long studentId, BorrowStatus status);

    @Query("""
    SELECT br\s
    FROM BorrowRecord br\s
    WHERE br.status = 'BORROWING'\s
        AND br.dueDate >= :today\s
        AND br.dueDate <= :targetDate
    ORDER BY br.dueDate ASC       \s
   \s""")
    List<BorrowRecord> findDueSoonRecords(@Param("today") LocalDate today,
                                          @Param("targetDate") LocalDate targetDate);
}
