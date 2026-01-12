package com.example.library_management_system.web.dto;

import com.example.library_management_system.domain.entity.Notice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeResponse {
    private Long id;
    private String title;
    private String content;

    // 新字段：轮播图
    private List<String> imageUrls;

    // 兼容旧字段：单图
    private String imageUrl;

    private boolean published;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NoticeResponse from(Notice notice) {
        if (notice == null) return null;

        List<String> urls = new ArrayList<>();

        if (notice.getImageUrls() != null && !notice.getImageUrls().isBlank()) {
            for (String s : notice.getImageUrls().split(",")) {
                if (s != null) {
                    String t = s.trim();
                    if (!t.isEmpty()) urls.add(t);
                }
            }
        }

        // 兼容旧数据：如果 imageUrls 为空但 imageUrl 有值，补到列表里
        if (urls.isEmpty() && notice.getImageUrl() != null && !notice.getImageUrl().isBlank()) {
            urls.add(notice.getImageUrl().trim());
        }

        return NoticeResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .imageUrls(urls)
                .imageUrl(notice.getImageUrl())
                .published(notice.isPublished())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build();
    }
}
