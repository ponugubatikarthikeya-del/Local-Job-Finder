package com.localjobfinder.bakend.paging;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class PagedResponse<T> {

    private final List<T> content;

    private final int pageNumber;
    private final int pageSize;
    private final long totalElements;
    private final int totalPages;
    private final boolean isLast;

    @JsonCreator
    public PagedResponse(
            @JsonProperty("content") List<T> content,
            @JsonProperty("pageNumber") int pageNumber,
            @JsonProperty("pageSize") int pageSize,
            @JsonProperty("totalElements") long totalElements,
            @JsonProperty("totalPages") int totalPages,
            @JsonProperty("last") boolean isLast) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.isLast = isLast;
    }

    public static <T> PagedResponse<T> from(Page<T> page) {
        return PagedResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLast(page.isLast())
                .build();
    }

}
