package com.example.library_management_system.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 解决前端（Vue Router history 模式）在刷新/直接访问子路由时 404 的问题：
 * 
 * - /api/** 由 RestController 处理
 * - 静态资源（*.js/*.css/*.png 等）由 Spring Boot static handler 处理
 * - 其余路径统一转发到 /index.html
 */
@Controller
public class SpaForwardController {

    // 不匹配 /api 开头的路径
    // 同时排除包含文件扩展名的静态资源请求（例如 /assets/app.123.js）
    @GetMapping({
            "/{path:^(?!api$).*$}",
            "/{path:^(?!api$).*$}/{path2:[^\\.]*}",
            "/{path:^(?!api$).*$}/{path2:[^\\.]*}/{path3:[^\\.]*}",
            "/{path:^(?!api$).*$}/{path2:[^\\.]*}/{path3:[^\\.]*}/{path4:[^\\.]*}",
            "/{path:^(?!api$).*$}/{path2:[^\\.]*}/{path3:[^\\.]*}/{path4:[^\\.]*}/{path5:[^\\.]*}"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
