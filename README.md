# 图书馆管理系统（Spring Boot + Vue3 + Element Plus）

本项目是一个 **Spring Boot + Vue3** 的图书馆管理系统示例实现，包含后端 REST API 与前端管理端/读者端页面。

- 管理员端：图书管理、用户管理、借阅审核/归还、管理员注册口令生成
- 读者端：图书浏览、借阅申请、我的借阅、个人中心（资料/改密）

> 后端：当前项目根目录（Spring Boot）
> 前端：`library_ui`（Vue3 + Vite + Element Plus）

---

## 1. 项目结构

```text
library_management_system/               # 后端（Spring Boot）
├─ src/main/java/com/example/library_management_system
├─ src/main/resources
│  ├─ application.properties
│  └─ static/int.sql                     # 数据库初始化脚本（建库/建表/示例数据）
├─ pom.xml
└─ README.md

library_ui/                              # 前端（Vue3 + Vite + Element Plus）
├─ src
└─ package.json
```

---

## 2. 技术栈

### 后端
- Spring Boot 4
- Spring Security + JWT
- Spring Data JPA
- MySQL 8

### 前端
- Vue 3 + Vite
- Element Plus
- Vue Router 4
- Pinia
- Axios
- NProgress（路由切换进度条）

---

## 3. 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8+

---

## 4. 数据库初始化（可重复执行 / 不删表）

初始化脚本：

- `src/main/resources/static/int.sql`

脚本特点：
- ✅ `CREATE TABLE IF NOT EXISTS`：不删除表结构，可重复执行
- ✅ 示例数据 `INSERT ... ON DUPLICATE KEY UPDATE`：重复执行不报错（用于 demo 环境）
- ✅ `books.isbn` 设置唯一键 `uk_books_isbn`：确保图书按 ISBN 去重

执行方式：

```bash
mysql -uroot -p < src/main/resources/static/int.sql
```

> 如果你之前已经创建过 `books` 表且没有 `uk_books_isbn` 唯一键，由于脚本不会删表，需要你手动执行一次：
>
> ```sql
> ALTER TABLE books ADD UNIQUE KEY uk_books_isbn (isbn);
> ```

---

## 5. 后端启动

### 5.1 修改数据源配置

文件：`src/main/resources/application.properties`

请按你的本地 MySQL 修改：
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

### 5.2 启动方式

- IDEA：运行 `com.example.library_management_system.LibraryManagementSystemApplication`

或 Maven Wrapper：

```bash
./mvnw -DskipTests spring-boot:run
```

Windows：

```bash
.\mvnw.cmd -DskipTests spring-boot:run
```

后端默认地址：
- `http://localhost:8080`

---

## 6. 前端启动

```bash
cd library_ui
npm i
npm run dev
```

前端默认地址：
- `http://localhost:5173`

---

## 7. 登录与会话策略（重要）

前端登录信息使用 **sessionStorage** 保存：
- ✅ 同一 Tab 刷新（Ctrl + R）：不会退出登录，仍停留在刷新时的页面（只刷新数据）
- ✅ 新开 Tab / 新窗口：始终回到登录页（不共享旧 Tab 登录态）

---

## 8. 默认账号与权限

### 8.1 默认管理员

系统启动时会自动创建管理员账号（后端 `DataInitializer` 初始化）：
- 用户名：`admin`
- 密码：`admin123`

### 8.2 示例读者账号（来自 int.sql）

密码均为：`123456`
- `reader1 / 123456`
- `reader2 / 123456`
- `reader3 / 123456`

### 8.3 管理员注册机制

出于安全考虑，管理员不能随意注册。

流程：
1. 已登录管理员在管理端生成一次性管理员注册口令（24 小时有效，用一次即失效）
2. 使用该口令注册管理员账号

---

## 9. 功能概览

### 9.1 管理员端
- 图书管理：新增/编辑/删除/分页查询
- 用户管理：新增/编辑/删除/分页查询
- 借阅管理：审核通过/拒绝、登记归还、按状态与书名拼音首字母筛选
- 管理员口令：生成管理员注册口令

### 9.2 读者端
- 图书浏览：分页/关键词搜索/详情
- 借阅管理：提交借阅申请、查看我的借阅
- 个人中心：修改资料、修改密码

---

## 10. API 速查（部分）

### 10.1 认证
- `POST /api/auth/login`
- `POST /api/auth/register`
- `POST /api/auth/register-admin`
- `GET /api/me`

### 10.2 图书
- `GET /api/books`（允许匿名访问）
- `POST /api/books`（ADMIN）
- `PUT /api/books/{id}`（ADMIN）
- `DELETE /api/books/{id}`（ADMIN）

### 10.3 借阅
- `POST /api/borrows`（登录）
- `GET /api/borrows/my`（登录）
- `GET /api/borrows`（ADMIN，支持 `status`、`titleInitial`）
- `POST /api/borrows/{id}/approve`（ADMIN）
- `POST /api/borrows/{id}/reject`（ADMIN）
- `POST /api/borrows/{id}/return`（ADMIN）

### 10.4 管理员口令
- `POST /api/admin-register-tokens`（ADMIN）

---

## 11. 常见问题

### 11.1 后端启动失败：8080 端口被占用

报错：
```text
Port 8080 was already in use
```

解决：关闭占用 8080 的进程，或修改端口：

```properties
server.port=8081
```

### 11.2 前端请求 Network Error

常见原因：
- 后端未启动
- CORS 跨域

本项目后端已配置允许 `http://localhost:5173` 访问。

---

如需进一步扩展（罚金规则、分类字典、导出、统计报表、实体册/条码管理等），可继续在此基础上迭代。
