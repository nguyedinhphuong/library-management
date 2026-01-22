package com.project.library.repository.criteria;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SearchCriteria {

    private String key; // fullName,...
    private String operation; // ><...
    private Object value;

    public SearchCriteria(String key, String operation, String value) {
        this.key = key.trim();
        this.operation = operation.trim();
        this.value = value.trim();
    }
}
