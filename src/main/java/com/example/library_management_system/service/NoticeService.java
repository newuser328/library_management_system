package com.example.library_management_system.service;

import com.example.library_management_system.domain.entity.Notice;
import com.example.library_management_system.repository.NoticeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    @Transactional
    public Notice create(Notice notice) {
        return noticeRepository.save(notice);
    }

    @Transactional
    public Notice update(Long id, Notice req) {
        Notice notice = getById(id);
        notice.setTitle(req.getTitle());
        notice.setContent(req.getContent());
        notice.setImageUrl(req.getImageUrl());
        notice.setImageUrls(req.getImageUrls());
        notice.setPublished(req.isPublished());
        return noticeRepository.save(notice);
    }

    @Transactional
    public void delete(Long id) {
        noticeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Notice getById(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notice not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Notice> listAll() {
        return noticeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Notice> listPublished() {
        return noticeRepository.findByPublishedTrueOrderByCreatedAtDesc();
    }
}
