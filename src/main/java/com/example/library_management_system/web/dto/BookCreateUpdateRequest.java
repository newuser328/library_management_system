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

    private String category;

    private String publisher;

    private String description;

    private String coverUrl;

    @NotNull
    @Min(0)
    private Integer totalCount;
}

