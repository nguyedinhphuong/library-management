package com.spring.code.demo.repository.Criteria;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SearchCriteria {
    private String key; // firstName, lastName,...
    private String operation; //= > < ...
    private Object value; // String ,...

    public SearchCriteria(String key, String operation, String value) {
        this.key = key.trim();
        this.operation = operation.trim();
        this.value = value.trim();
    }
}
