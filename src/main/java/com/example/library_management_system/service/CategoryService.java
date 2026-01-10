package com.example.library_management_system.service;

import com.example.library_management_system.domain.entity.Category;
import com.example.library_management_system.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<Category> listAll() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    @Transactional(readOnly = true)
    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));
    }

    @Transactional
    public Category create(String name) {
        String normalized = normalizeName(name);
        if (categoryRepository.existsByName(normalized)) {
            throw new IllegalArgumentException("分类名称已存在: " + normalized);
        }
        Category category = Category.builder().name(normalized).build();
        return categoryRepository.save(category);
    }

    @Transactional
    public Category update(Long id, String name) {
        Category category = getById(id);
        String normalized = normalizeName(name);
        if (categoryRepository.existsByNameAndIdNot(normalized, id)) {
            throw new IllegalArgumentException("分类名称已存在: " + normalized);
        }
        category.setName(normalized);
        return categoryRepository.save(category);
    }

    @Transactional
    public void delete(Long id) {
        Category category = getById(id);
        // 最小改动：如果有关联图书则禁止删除
        if (category.getBooks() != null && !category.getBooks().isEmpty()) {
            throw new IllegalStateException("该分类下仍有关联图书，无法删除");
        }
        categoryRepository.delete(category);
    }

    private String normalizeName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name 不能为空");
        }
        String normalized = name.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("name 不能为空");
        }
        if (normalized.length() > 32) {
            throw new IllegalArgumentException("name 长度不能超过32");
        }
        return normalized;
    }
}
