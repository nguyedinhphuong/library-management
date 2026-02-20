package com.project.library.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_user_has_group", uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "user_id"}))
public class UserHasGroup extends AbstractEntity<Integer>{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
