package com.project.library.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
public class PageResponse<T> implements Serializable {

    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private T item;
}
