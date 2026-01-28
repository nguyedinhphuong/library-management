package com.project.library.repository;

import com.project.library.utils.BorrowStatus;
import com.project.library.utils.StudentStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class StatisticsRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Long getTotalStudents() {
        String jpql = "SELECT COUNT(s) FROM Student s";
        return entityManager.createQuery(jpql, Long.class).getSingleResult();
    }

    public Long getActiveStudents() {
        String jpql = "SELECT COUNT(s) FROM Student s WHERE s.status = :status";
        return entityManager.createQuery(jpql, Long.class)
                .setParameter("status", StudentStatus.ACTIVE)
                .getSingleResult();
    }

    public Long getSuspendedStudents() {
        String jpql = "SELECT COUNT(s) FROM Student s WHERE s.status = :status";
        return entityManager.createQuery(jpql, Long.class)
                .setParameter("status", StudentStatus.SUSPENDED)
                .getSingleResult();
    }

    public Long getTotalBooks() {
        String jpql = "SELECT COUNT(b) FROM Book b";
        return entityManager.createQuery(jpql, Long.class).getSingleResult();
    }

    public Long getTotalAvailableBooks() {
        String jpql = "SELECT COALESCE(SUM(b.quantityAvailable), 0) FROM Book b WHERE b.status = 'AVAILABLE'";
        return entityManager.createQuery(jpql, Long.class).getSingleResult();
    }

    public Long getTotalCategories() {
        String jpql = "SELECT COUNT(c) FROM Category c";
        return entityManager.createQuery(jpql, Long.class).getSingleResult();
    }

    public Long getCurrentBorrowing() {
        String jpql = "SELECT COUNT(br) FROM BorrowRecord br WHERE br.status = :status";
        return entityManager.createQuery(jpql, Long.class)
                .setParameter("status", BorrowStatus.BORROWING)
                .getSingleResult();
    }

    public Long getOverdueBooks() {
        String jpql = "SELECT COUNT(br) FROM BorrowRecord br " +
                "WHERE br.status = :status AND br.dueDate < :today";
        return entityManager.createQuery(jpql, Long.class)
                .setParameter("status", BorrowStatus.BORROWING)
                .setParameter("today", LocalDate.now())
                .getSingleResult();
    }

    public Long getBorrowsThisMonth() {
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate lastDayOfMonth = LocalDate.now().withDayOfMonth(
                LocalDate.now().lengthOfMonth());

        String jpql = "SELECT COUNT(br) FROM BorrowRecord br " +
                "WHERE br.borrowDate >= :startDate AND br.borrowDate <= :endDate";
        return entityManager.createQuery(jpql, Long.class)
                .setParameter("startDate", firstDayOfMonth)
                .setParameter("endDate", lastDayOfMonth)
                .getSingleResult();
    }

    public Long getReturnsThisMonth() {
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate lastDayOfMonth = LocalDate.now().withDayOfMonth(
                LocalDate.now().lengthOfMonth());

        String jpql = "SELECT COUNT(br) FROM BorrowRecord br " +
                "WHERE br.returnDate >= :startDate AND br.returnDate <= :endDate";
        return entityManager.createQuery(jpql, Long.class)
                .setParameter("startDate", firstDayOfMonth)
                .setParameter("endDate", lastDayOfMonth)
                .getSingleResult();
    }

    public Long getBorrowsInPeriod(LocalDate startDate, LocalDate endDate) {
        String jpql = "SELECT COUNT(br) FROM BorrowRecord br " +
                "WHERE br.borrowDate >= :startDate AND br.borrowDate <= :endDate";
        return entityManager.createQuery(jpql, Long.class)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getSingleResult();
    }

    public Long getReturnsInPeriod(LocalDate startDate, LocalDate endDate) {
        String jpql = "SELECT COUNT(br) FROM BorrowRecord br " +
                "WHERE br.returnDate >= :startDate AND br.returnDate <= :endDate " +
                "AND br.status = 'RETURNED'";
        return entityManager.createQuery(jpql, Long.class)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getSingleResult();
    }

    public Long getNewStudentsInMonth(LocalDate startDate, LocalDate endDate) {
        String jpql = "SELECT COUNT(s) FROM Student s " +
                "WHERE s.createdAt >= :startDateTime AND s.createdAt < :endDateTime";

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        return entityManager.createQuery(jpql, Long.class)
                .setParameter("startDateTime", startDateTime)
                .setParameter("endDateTime", endDateTime)
                .getSingleResult();
    }

    public Long getNewBooksInMonth(LocalDate startDate, LocalDate endDate) {
        String jpql = "SELECT COUNT(b) FROM Book b " +
                "WHERE b.createdAt >= :startDateTime AND b.createdAt < :endDateTime";

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        return entityManager.createQuery(jpql, Long.class)
                .setParameter("startDateTime", startDateTime)
                .setParameter("endDateTime", endDateTime)
                .getSingleResult();
    }

    public List<Object[]> getTopActiveStudents(LocalDate startDate, LocalDate endDate, int limit) {
        String jpql = "SELECT br.student, " +
                "COUNT(br), " +
                "SUM(CASE WHEN br.returnDate IS NOT NULL AND br.returnDate > br.dueDate THEN 1 " +
                "         WHEN br.returnDate IS NULL AND :today > br.dueDate THEN 1 " +
                "         ELSE 0 END) " +
                "FROM BorrowRecord br " +
                "WHERE br.borrowDate >= :startDate AND br.borrowDate <= :endDate " +
                "GROUP BY br.student " +
                "ORDER BY COUNT(br) DESC";

        return entityManager.createQuery(jpql, Object[].class)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("today", LocalDate.now())
                .setMaxResults(limit)
                .getResultList();
    }


    public Long getOverdueInPeriod(LocalDate startDate, LocalDate endDate) {
        String jpql = "SELECT COUNT(br) FROM BorrowRecord br " +
                "WHERE br.borrowDate >= :startDate AND br.borrowDate <= :endDate " +
                "AND br.status = 'BORROWING' " +
                "AND br.dueDate < :today";
        return entityManager.createQuery(jpql, Long.class)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("today", LocalDate.now())
                .getSingleResult();
    }

}
