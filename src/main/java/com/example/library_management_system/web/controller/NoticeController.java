package com.example.library_management_system.web.controller;

import com.example.library_management_system.domain.entity.Notice;
import com.example.library_management_system.service.NoticeService;
import com.example.library_management_system.web.dto.ApiResponse;
import com.example.library_management_system.web.dto.NoticeCreateUpdateRequest;
import com.example.library_management_system.web.dto.NoticeResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    // 读者端：首页展示已发布公告（允许匿名访问）
    @GetMapping("/published")
    public ApiResponse<List<NoticeResponse>> listPublished() {
        List<NoticeResponse> list = noticeService.listPublished().stream()
                .map(NoticeResponse::from)
                .toList();
        return ApiResponse.ok(list);
    }

    // 管理端：公告管理（需要 ADMIN 权限，由 SecurityConfig 配置）
    @GetMapping("/admin")
    public ApiResponse<List<NoticeResponse>> listAll() {
        List<NoticeResponse> list = noticeService.listAll().stream()
                .sorted(Comparator.comparing(Notice::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(NoticeResponse::from)
                .toList();
        return ApiResponse.ok(list);
    }

    @PostMapping("/admin")
    public ApiResponse<NoticeResponse> create(@Valid @RequestBody NoticeCreateUpdateRequest req) {
        Notice notice = Notice.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .imageUrl(req.getImageUrl())
                .imageUrls(joinImageUrls(req.getImageUrls()))
                .published(req.isPublished())
                .build();
        return ApiResponse.ok(NoticeResponse.from(noticeService.create(notice)));
    }

    @PutMapping("/admin/{id}")
    public ApiResponse<NoticeResponse> update(@PathVariable Long id, @Valid @RequestBody NoticeCreateUpdateRequest req) {
        Notice notice = Notice.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .imageUrl(req.getImageUrl())
                .imageUrls(joinImageUrls(req.getImageUrls()))
                .published(req.isPublished())
                .build();
        return ApiResponse.ok(NoticeResponse.from(noticeService.update(id, notice)));
    }

    @DeleteMapping("/admin/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        noticeService.delete(id);
        return ApiResponse.ok(null);
    }

    private String joinImageUrls(List<String> urls) {
        if (urls == null || urls.isEmpty()) return null;
        return urls.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .limit(3)
                .map(String::trim)
                .reduce((a, b) -> a + "," + b)
                .orElse(null);
    }
}
