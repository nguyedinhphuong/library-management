package com.project.library.repository;

import com.project.library.utils.BorrowStatus;
import com.project.library.utils.StudentStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

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
}
