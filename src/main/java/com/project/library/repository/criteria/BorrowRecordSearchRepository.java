package com.project.library.repository.criteria;


import com.project.library.dto.response.PageResponse;
import com.project.library.model.BorrowRecord;
import com.project.library.utils.BorrowStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Repository
public class BorrowRecordSearchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public PageResponse<?> searchBorrowRecords(Long studentId, Long bookId, BorrowStatus status, LocalDate fromDate, LocalDate toDate, int pageNo, int pageSize, String sortBy) {
        log.debug("Search borrow records - studentId: {}, bookId: {}, status: {}", studentId, bookId, status);

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<BorrowRecord> query = builder.createQuery(BorrowRecord.class);
        Root<BorrowRecord> root = query.from(BorrowRecord.class);

        Fetch<BorrowRecord, ?> studentFetch = root.fetch("student", JoinType.LEFT);
        Join<BorrowRecord, ?> studentJoin = (Join<BorrowRecord, ?>) studentFetch;

        Fetch<BorrowRecord, ?> bookFetch = root.fetch("book", JoinType.LEFT);
        Join<BorrowRecord, ?> bookJoin = (Join<BorrowRecord, ?>) bookFetch;

        bookFetch.fetch("category", JoinType.LEFT);
        query.distinct(true);

        List<Predicate> predicates = new ArrayList<>();
        if(studentId != null ) predicates.add(builder.equal(studentJoin.get("id"), studentId));
        if(bookId != null ) predicates.add(builder.equal(bookJoin.get("id"), bookId));
        if(status != null ) predicates.add(builder.equal(root.get("status"), status));
        if(fromDate != null) predicates.add(builder.greaterThanOrEqualTo(root.get("borrowDate"), fromDate));
        if(toDate != null) predicates.add(builder.lessThanOrEqualTo(root.get("borrowDate"), fromDate));

        query.where(builder.and(predicates.toArray(new Predicate[0])));
        if(sortBy != null && !sortBy.isEmpty()) {
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)"); // studentId:desc
            Matcher matcher = pattern.matcher(sortBy);
            if(matcher.find()) {
                String field = matcher.group(1);
                String direction = matcher.group(3);
                if("desc".equalsIgnoreCase(direction)) {
                    query.orderBy(builder.desc(root.get(field)));
                } else if("asc".equalsIgnoreCase(direction)) {
                    query.orderBy(builder.desc(root.get(field)));
                }
            }else {
                query.orderBy(builder.desc(root.get("borrowDate")));
            }
        }
        // run
        List<BorrowRecord> records = entityManager.createQuery(query)
                .setFirstResult(pageNo * pageSize)
                .setMaxResults(pageSize)
                .getResultList();

        long totalElements = getTotalCount(studentId, bookId, status, fromDate, toDate);
        int totalPage = pageSize == 0 ? 0 : (int) Math.ceil((double) totalElements / pageSize);
        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPages(totalPage)
                .totalElements(totalElements)
                .item(records)
                .build();
    }

    private long getTotalCount(Long studentId, Long bookId, BorrowStatus status, LocalDate fromDate, LocalDate toDate) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<BorrowRecord> root = countQuery.from(BorrowRecord.class);
        Join<BorrowRecord, ?> studentJoin = root.join("student", JoinType.LEFT);
        Join<BorrowRecord, ?> bookJoin = root.join("book", JoinType.LEFT);
        countQuery.select(builder.countDistinct(root));
        List<Predicate> predicates = new ArrayList<>();
        if(studentId != null ) predicates.add(builder.equal(studentJoin.get("id"), studentId));
        if(bookId != null ) predicates.add(builder.equal(bookJoin.get("id"), bookId));
        if(status != null ) predicates.add(builder.equal(root.get("status"), status));
        if(fromDate != null) predicates.add(builder.greaterThanOrEqualTo(root.get("borrowDate"), fromDate));
        if(toDate != null) predicates.add(builder.lessThanOrEqualTo(root.get("borrowDate"), fromDate));
        countQuery.where(builder.and(predicates.toArray(new Predicate[0])));
        return entityManager.createQuery(countQuery).getSingleResult();
    }
}
