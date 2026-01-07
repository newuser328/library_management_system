package com.example.library_management_system.domain.entity;

import com.example.library_management_system.util.PinyinUtils;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String isbn;

    @Column(nullable = false, length = 128)
    private String title;

    @Column(length = 16)
    private String titlePinyin; // 新增：书名首字母（自动生成）

    @Column(length = 64)
    private String author;

    @Column(length = 64)
    private String category;

    @Column(length = 64)
    private String publisher;

    @Column(length = 2048)
    private String description;

    @Column(length = 512)
    private String coverUrl;

    @Column(nullable = false)
    private Integer totalCount;

    @Column(nullable = false)
    private Integer availableCount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (totalCount == null) {
            totalCount = 0;
        }
        if (availableCount == null) {
            availableCount = totalCount;
        }
        // 自动生成书名首字母
        if (title != null) {
            this.titlePinyin = PinyinUtils.firstLetters(title);
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        // 标题更新时同步更新拼音首字母
        if (title != null) {
            this.titlePinyin = PinyinUtils.firstLetters(title);
        }
    }
}