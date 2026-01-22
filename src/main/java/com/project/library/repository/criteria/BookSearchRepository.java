package com.project.library.repository.criteria;

import com.project.library.dto.response.PageResponse;
import com.project.library.model.Book;
import com.project.library.utils.BookStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Repository
public class BookSearchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public PageResponse<?> searchBooks(String search, Integer categoryId, BookStatus status, Boolean onlyAvailable, int pageNo, int pageSize, String sortBy) {
        log.debug("Search books - keyword: {}, categoryId: {}, status: {}, onlyAvailable: {}",
                search, categoryId, status, onlyAvailable);

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Book> query = builder.createQuery(Book.class);
        Root<Book> root = query.from(Book.class);
        query.distinct(true);
        root.fetch("category", jakarta.persistence.criteria.JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();
        if(StringUtils.hasLength(search)){
            String searchPattern = "%"+search.toLowerCase()+"%";
            Predicate titlePredicate = builder.like(builder.lower(root.get("title")), searchPattern);
            Predicate authorPredicate = builder.like(builder.lower(root.get("author")), searchPattern);
            Predicate isbnPredicate = builder.like(builder.lower(root.get("isbn")), searchPattern);

            predicates.add(builder.or(titlePredicate,authorPredicate,isbnPredicate));
        }

        if(categoryId != null) predicates.add(builder.equal(root.get("category").get("id"), categoryId));
        if(status != null) predicates.add(builder.equal(root.get("status"), status));
        if(onlyAvailable != null && onlyAvailable) {
            predicates.add(builder.greaterThan(root.get("quantityAvailable"), 0));
            predicates.add(builder.equal(root.get("status"), BookStatus.AVAILABLE));
        }
        query.where(builder.and(predicates.toArray(new Predicate[0])));
        if(StringUtils.hasLength(sortBy)){
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
            Matcher matcher = pattern.matcher(sortBy);
            if(matcher.find()){
                String field = matcher.group(1);
                String direction = matcher.group(3);

                if("desc".equalsIgnoreCase(direction)){
                    query.orderBy(builder.desc(root.get(field)));
                } else if ("asc".equalsIgnoreCase(direction)) {
                    query.orderBy(builder.asc(root.get(field)));
                }
            }
        }
        List<Book> books = entityManager.createQuery(query)
                .setFirstResult(pageNo * pageSize)
                .setMaxResults(pageSize)
                .getResultList();

        long totalElements = getTotalCount(search, categoryId, status, onlyAvailable);
        int totalPages = pageSize == 0 ? 0 : (int) Math.ceil((double)totalElements/pageSize);
        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .totalElements(totalElements)
                .item(books)
                .build();
    }

    private long getTotalCount(String search, Integer categoryId, BookStatus status, Boolean onlyAvailable) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<Book> root = countQuery.from(Book.class);
        countQuery.select(builder.count(root));

        List<Predicate> predicates = new ArrayList<>();
        if(StringUtils.hasLength(search)){
            String searchPattern = "%"+search.toLowerCase()+"%";
            Predicate titlePredicate = builder.like(builder.lower(root.get("title")), searchPattern);
            Predicate authorPredicate = builder.like(builder.lower(root.get("author")), searchPattern);
            Predicate isbnPredicate = builder.like(builder.lower(root.get("isbn")), searchPattern);

            predicates.add(builder.or(titlePredicate,authorPredicate,isbnPredicate));
        }

        if(categoryId != null) predicates.add(builder.equal(root.get("category").get("id"), categoryId));
        if(status != null) predicates.add(builder.equal(root.get("status"), status));
        if(onlyAvailable != null && onlyAvailable) {
            predicates.add(builder.greaterThan(root.get("quantityAvailable"), 0));
            predicates.add(builder.equal(root.get("status"), BookStatus.AVAILABLE));
        }
        countQuery.where(builder.and(predicates.toArray(new Predicate[0])));
        return entityManager.createQuery(countQuery).getSingleResult();
    }
}
