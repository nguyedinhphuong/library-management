package com.project.library.repository.criteria;

import com.project.library.dto.response.PageResponse;
import com.project.library.model.Student;
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
public class StudentSearchRepository {
    @PersistenceContext
    private EntityManager entityManager;

    /**
     *
     * Advanced search with dynamic criteria
     * Format: "fullName:Nguyen", "major=INFORMATION_TECHNOLOGY", "status=ACTIVE"
     */
    public PageResponse<?> advanceSearchByCriteria(int pageNo, int pageSize, String sortBy, String... search){
        log.debug("Advances search - pageNo: {} , pageSize: {}, sortBy: {}", pageNo, pageSize, sortBy);

        List<SearchCriteria> criteriaList = new ArrayList<>();
        if(search != null) {
            Pattern pattern = Pattern.compile("(\\w+(?:\\.\\w+)*)([=><:])(.+)", Pattern.CASE_INSENSITIVE);
            for(String s: search) {
                Matcher matcher = pattern.matcher(s);
                if(matcher.matches()){
                    criteriaList.add(new SearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3).trim()));
                }
            }
        }
        List<Student> students = getStudents(pageNo, pageSize, criteriaList, sortBy);
        long totalElements = getTotalCount(criteriaList);
        int totalPage = pageSize == 0 ? 0 : (int) Math.ceil((double) totalElements/pageSize);
        return  PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalElements(totalElements)
                .totalPages(totalPage)
                .item(students)
                .build();
    }

    private long getTotalCount(List<SearchCriteria> criteriaList) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<Student> root = countQuery.from(Student.class);
        countQuery.select(builder.count(root));

        Predicate predicate = builder.conjunction();
        if(criteriaList != null && !criteriaList.isEmpty()) {
            StudentSearchCriteriaQueryConsumer queryConsumer = new StudentSearchCriteriaQueryConsumer(builder, predicate, root);
            criteriaList.forEach(queryConsumer);
            predicate = queryConsumer.getPredicate();
        }
        countQuery.where(predicate);
        return entityManager.createQuery(countQuery).getSingleResult();

    }

    // get ds sv
    private List<Student> getStudents(int pageNo, int pageSize, List<SearchCriteria> criteriaList, String sortBy) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Student> query = builder.createQuery(Student.class);
        Root<Student> root = query.from(Student.class);

        Predicate predicate = builder.conjunction();
        if(criteriaList != null && !criteriaList.isEmpty()) {
            StudentSearchCriteriaQueryConsumer queryConsumer = new StudentSearchCriteriaQueryConsumer(builder, predicate, root);
            criteriaList.forEach(queryConsumer);
            predicate = queryConsumer.getPredicate();
        }
        query.where(predicate);

        // sort
        if(StringUtils.hasLength(sortBy)){
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)"); // fullName:desc
            Matcher matcher = pattern.matcher(sortBy);
            if(matcher.find()){
                String field = matcher.group(1);
                String direction = matcher.group(3);

                if("desc".equalsIgnoreCase(direction)) {
                    query.orderBy(builder.desc(root.get(field)));
                } else if ("asc".equalsIgnoreCase(direction)) {
                    query.orderBy(builder.asc(root.get(field)));
                }
            }
        }
        return entityManager.createQuery(query)
                .setFirstResult(pageNo * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

}
