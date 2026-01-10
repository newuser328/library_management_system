package com.example.library_management_system.web.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BookResponse {

    private Long id;
    private String isbn;
    private String title;
    private String author;

    /**
     * 旧字段（兼容展示用）
     */
    private String category;

    /**
     * 新字段：分类列表
     */
    private List<CategoryResponse> categories;

    private String publisher;
    private String description;
    private String coverUrl;
    private Integer totalCount;
    private Integer availableCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
