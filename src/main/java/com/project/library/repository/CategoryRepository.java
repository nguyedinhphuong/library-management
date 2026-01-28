package com.project.library.repository;

import com.project.library.dto.response.BookResponse;
import com.project.library.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    Optional<Category> findByCode(String code);
    List<Category> findByIdIn(List<Integer> ids);
}
