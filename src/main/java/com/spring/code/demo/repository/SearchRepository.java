package com.spring.code.demo.repository;


import com.spring.code.demo.dto.response.PageResponse;
import com.spring.code.demo.model.User;
import com.spring.code.demo.repository.Criteria.SearchCriteria;
import com.spring.code.demo.repository.Criteria.UserSearchCriteriaQueryConsumer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Repository
public class SearchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public PageResponse<?> getAllUserWithSortByColumnsAndSearch(int pageNo, int pageSize, String search, String sortBy) {
        // Query ra list user
        StringBuilder sqlQuery = new StringBuilder("select new com.spring.code.demo.dto.response.UserDetailResponse(u.id, u.firstName, u.lastName, u.email, u.phone) from User u where 1=1 ");
        if (StringUtils.hasLength(search)) {
            sqlQuery.append(" and lower(u.firstName) like lower(:firstName) ");
            sqlQuery.append(" or lower(u.lastName) like lower(:lastName) ");
            sqlQuery.append(" or lower(u.email) like lower(:email) ");
        }
        if (StringUtils.hasLength(sortBy)) {
            Pattern pattern = Pattern.compile("(\\w*?)(:)(.*)");
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                sqlQuery.append(String.format(" order by u.%s %s", matcher.group(1), matcher.group(3)));
            }
        }
        Query selectQuery = entityManager.createQuery(sqlQuery.toString());
        selectQuery.setFirstResult(pageNo);
        selectQuery.setMaxResults(pageSize);
        if (StringUtils.hasLength(search)) {
            selectQuery.setParameter("firstName", String.format("%%%s%%", search));
            selectQuery.setParameter("lastName", String.format("%%%s%%", search));
            selectQuery.setParameter("email", String.format("%%%s%%", search));
        }
        List users = selectQuery.getResultList();
        System.out.println(users);

        // query ra list user


        // Query ra số record
        StringBuilder sqlCountQuery = new StringBuilder("select count(*) from User u where 1=1 ");
        if (StringUtils.hasLength(search)) {
            sqlCountQuery.append(" and lower(u.firstName) like lower(?1) ");
            sqlCountQuery.append(" or lower(u.lastName) like lower(?2) ");
            sqlCountQuery.append(" or lower(u.email) like lower(?3) ");
        }
        Query selectCountQuery = entityManager.createQuery(sqlCountQuery.toString());
        if (StringUtils.hasLength(search)) {
            selectCountQuery.setParameter(1, String.format("%%%s%%", search));
            selectCountQuery.setParameter(2, String.format("%%%s%%", search));
            selectCountQuery.setParameter(3, String.format("%%%s%%", search));
        }
        Long totalElements = (Long) selectCountQuery.getSingleResult();
        System.out.println(totalElements);

        Page<?> page = new PageImpl<Object>(users, PageRequest.of(pageNo, pageSize), totalElements);
        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(page.getTotalPages())
                .items(page.stream().toList())
                .build();
    }

    public PageResponse getAdvanceSearchByCriteria(int pageNo, int pageSize, String sortBy, String... search) {
        //Lay ra ds user firstName:desc, lastName:...
        List<SearchCriteria> criteriaList = new ArrayList<>();
        if(search != null){
            Pattern pattern = Pattern.compile("(\\w+(?:\\.\\w+)*)([=><:])(\\s*)(.+)", Pattern.CASE_INSENSITIVE);
            for(String s: search){
                Matcher matcher = pattern.matcher(s);
                if(matcher.matches()){
                    criteriaList.add(new SearchCriteria(
                            matcher.group(1),
                            matcher.group(2),
                            matcher.group(4)
                    ));
                }
            }
        }
        // Lya ra sl ban ghi
        List<User> users = getUsers(pageNo, pageSize, criteriaList, sortBy);
        long totalElements = getCount(criteriaList);
        int totalPage = pageSize == 0 ? 0 : (int) Math.ceil((double) totalElements/pageSize);
        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(totalPage)
                .items(users)
                .build();
    }

    private List<User> getUsers(int pageNo, int pageSize, List<SearchCriteria> criteriaList, String sortBy) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
        Root<User> root = query.from(User.class);// Chỉ dịnh dối tượng cần tìm kiếm

        // Xử lý các điều kiện tìm kiếm
        Predicate predicate = criteriaBuilder.conjunction();
        if(criteriaList != null && !criteriaList.isEmpty()){
            UserSearchCriteriaQueryConsumer queryConsumer = new UserSearchCriteriaQueryConsumer(criteriaBuilder, predicate, root);
            // lấy dữ liệu từ root
            criteriaList.forEach(queryConsumer);
            predicate = queryConsumer.getPredicate(); // xl xong gán lại gtri
        }
        query.where(predicate);
        return entityManager.createQuery(query)
                .setFirstResult(pageNo* pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    private long getCount(List<SearchCriteria> criteriaList) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<User> root = countQuery.from(User.class);
        countQuery.select(criteriaBuilder.count(root));

        Predicate predicate = criteriaBuilder.conjunction();
        UserSearchCriteriaQueryConsumer consumer = null;
        if (!criteriaList.isEmpty()) {
            consumer = new UserSearchCriteriaQueryConsumer(criteriaBuilder, predicate, root);
            criteriaList.forEach(consumer);
            predicate = consumer.getPredicate();
        }
        countQuery.where(predicate);
        return entityManager.createQuery(countQuery).getSingleResult();
    }
}