package com.example.library_management_system.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class NoticeCreateUpdateRequest {

    @NotBlank
    @Size(max = 128)
    private String title;

    @NotBlank
    private String content;

    // 新字段：最多三张图
    @Size(max = 3)
    private List<@Size(max = 512) String> imageUrls;

    // 兼容旧字段：单图
    @Size(max = 512)
    private String imageUrl;

    private boolean published;
}
