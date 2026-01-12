package com.example.library_management_system.repository;

import com.example.library_management_system.domain.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findByPublishedTrueOrderByCreatedAtDesc();
}
