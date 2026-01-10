package com.example.library_management_system.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookCreateUpdateRequest {

    @NotBlank
    private String isbn;

    @NotBlank
    private String title;

    private String author;

    // 兼容旧接口：若仍传 category 字符串则尝试按名称自动匹配/创建
    private String category;

    // 新接口：多分类 id 列表
    private java.util.List<Long> categoryIds;

    private String publisher;

    private String description;

    private String coverUrl;

    @NotNull
    @Min(0)
    private Integer totalCount;
}

