package com.project.library.model;

import com.project.library.utils.BookStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tbl_book")
public class Book extends AbstractEntity<Long> implements Serializable {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "author")
    private String author;

    @Column(name = "isbn", unique = true)
    private String isbn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "quantity_total", nullable = false)
    private Integer quantityTotal;

    @Column(name = "quantity_available", nullable = false)
    private Integer quantityAvailable;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private BookStatus status;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<BorrowRecord> borrowRecords = new ArrayList<>();

    /* ===== Business methods giữ nguyên ===== */
}
