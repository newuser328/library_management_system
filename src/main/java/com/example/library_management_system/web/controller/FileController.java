package com.example.library_management_system.web.controller;

import com.example.library_management_system.web.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    @Value("${app.upload-dir:./uploads}")
    private String uploadDir;

    private static final Set<String> ALLOWED_EXT = Set.of("png", "jpg", "jpeg", "webp");

    @PostMapping("/upload")
    public ApiResponse<UploadResponse> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = getExt(originalFilename);
        if (ext == null || !ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("不支持的文件类型，仅支持: png/jpg/jpeg/webp");
        }

        // 简单限制：10MB
        long max = 10L * 1024 * 1024;
        if (file.getSize() > max) {
            throw new IllegalArgumentException("文件过大，最大支持10MB");
        }

        Path dir = Paths.get(uploadDir, "images");
        Files.createDirectories(dir);

        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path target = dir.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // 通过动态接口访问
        String url = "/api/files/images/" + filename;
        return ApiResponse.ok(new UploadResponse(url, filename));
    }

    /**
     * 图片读取建议不鉴权：img 标签/预览请求无法携带 Authorization header，鉴权会导致 401。
     * 这里仍保留基础的路径穿越校验。
     */
    @GetMapping("/images/{filename}")
    public ResponseEntity<byte[]> getImage(@PathVariable String filename, HttpServletRequest request) throws IOException {
        if (!StringUtils.hasText(filename) || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return ResponseEntity.badRequest().build();
        }

        Path file = Paths.get(uploadDir, "images", filename);
        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }

        byte[] bytes = Files.readAllBytes(file);
        String contentType = request.getServletContext().getMimeType(file.toString());
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (contentType != null) {
            mediaType = MediaType.parseMediaType(contentType);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "max-age=86400")
                .contentType(mediaType)
                .body(bytes);
    }

    private String getExt(String filename) {
        if (!StringUtils.hasText(filename)) return null;
        int idx = filename.lastIndexOf('.');
        if (idx < 0 || idx == filename.length() - 1) return null;
        return filename.substring(idx + 1).trim().toLowerCase();
    }

    public record UploadResponse(String url, String filename) {}
}
