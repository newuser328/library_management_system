package com.example.library_management_system.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {
    
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 32, message = "分类名称长度不能超过32")
    private String name;
}