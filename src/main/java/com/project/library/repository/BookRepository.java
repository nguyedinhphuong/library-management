package com.project.library.repository;


import com.project.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);
    boolean existsByIsbn(String isbn);

    @Query("SELECT b.book as book, COUNT(b) as borrowCount, " +
            "SUM(CASE WHEN b.status = 'BORROWING' THEN 1 ELSE 0 END) as currentBorrowing " +
            "FROM BorrowRecord b " +
            "WHERE (:startDate IS NULL OR b.borrowDate >= :startDate) " +
            "GROUP BY b.book " +
            "ORDER BY COUNT(b) DESC")
    List<Object[]> findMostBorrowedBooks(@Param("startDate") LocalDate date, @Param("limit") int limit);

    @Query("select b from Book b where b.quantityAvailable <= :threshold and b.status = 'AVAILABLE' order by b.quantityAvailable asc")
    List<Book> findLowStockBooks(@Param("threshold") int threshold);

    @Query("SELECT b.book as book, COUNT(b) as borrowCount, " +
            "SUM(CASE WHEN b.status = 'BORROWING' THEN 1 ELSE 0 END) as currentBorrowing " +
            "FROM BorrowRecord b " +
            "WHERE b.borrowDate >= :startDate AND b.borrowDate <= :endDate " +
            "GROUP BY b.book " +
            "ORDER BY COUNT(b) DESC")
    List<Object[]> findMostBorrowedBooksInPeriod(@Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate,
                                                 @Param("limit") int limit);

    @Query("SELECT b.book as book, COUNT(b) as borrowCount, " +
            "SUM(CASE WHEN b.status = 'BORROWING' THEN 1 ELSE 0 END) as currentBorrowing " +
            "FROM BorrowRecord b " +
            "WHERE (:startDate IS NULL OR b.borrowDate >= :startDate) " +
            "GROUP BY b.book " +
            "ORDER BY COUNT(b) DESC")
    List<Object[]> findMostBorrowedBooks(@Param("startDate") LocalDate startDate);

    @Query("select b.isbn from Book b where b.isbn in :isbns")
    List<String> findExistingIsbns(@Param("isbns") List<String> isbns);
}
