package com.project.library.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tbl_category")
public class Category extends AbstractEntity<Integer> implements Serializable {

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(mappedBy = "category")
    private List<Book> books = new ArrayList<>();
}
