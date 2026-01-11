# 图书馆管理系统（Spring Boot 4 + Vue 3 + Element Plus）

本项目是一个**前后端分离**的图书馆管理系统：

- **后端**：Spring Boot 4 + Spring Security + JWT + Spring Data JPA + MySQL + Redis
- **前端**：Vue 3 + Vite + Element Plus + Pinia + Vue Router

## 1. 功能概览

- **管理员端**
  - 图书管理（增删改查）
  - 分类管理
  - 用户管理（增删改查）
  - 借阅管理（审批/驳回/归还）
  - 管理员注册口令生成
- **读者端**
  - 图书浏览/分页
  - 关键字搜索、按分类筛选
  - 批量借阅申请（借阅对话框展示当前选中书籍）
  - 我的借阅
  - 个人中心
- **认证与安全**
  - 用户名密码登录
  - 手机号验证码登录（支持 Mock / 容联云通信）
  - **安全增强**：手机号验证码自动注册用户必须先设置密码（设置后才可进入系统）

---

## 2. 项目结构

```text
library_management_system/                 # 后端（Spring Boot）
  src/main/java/com/example/...            # Java 代码
  src/main/resources/
    application.properties                 # 后端配置
    static/
      init_schema.sql                      # 初始化脚本（仅建表，不插入数据）
  pom.xml
  README.md

library_ui/                                # 前端（Vue 3 + Vite + Element Plus）
  src/
    api/                                   # 前端 API 封装
    router/                                # 路由
    store/                                 # Pinia（登录态等）
    utils/request.js                        # axios 封装（baseURL/拦截器）
  package.json
  README.md                                # 前端说明（已建议看根 README）
```

---

## 3. 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8+
- Redis（用于短信验证码存储与限流）

---

## 4. 后端配置

配置文件：`src/main/resources/application.properties`

### 4.1 MySQL

按本机修改：

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

### 4.2 Redis（Redis地址：https://github.com/microsoftarchive/redis/releases 下载后运行redis-sever.exe）

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=
spring.data.redis.database=0
spring.data.redis.timeout=3000
```

### 4.3 JWT

```properties
app.jwt.secret=please_change_this_secret_to_a_long_random_string_at_least_32_chars
app.jwt.expire-seconds=86400
```

### 4.4 文件上传

```properties
app.upload-dir=./uploads
```

### 4.5 短信服务（Mock / 容联云通信）（可登录容联云官网查看详细文档）

开发阶段建议使用 Mock（验证码输出到后端日志）：

```properties
app.sms.provider=mock
```

生产环境使用容联云通信（需自行填写账号信息）：

```properties
app.sms.provider=yuntongxun(测试换成mock)
app.sms.yuntongxun.server-ip=app.cloopen.com
app.sms.yuntongxun.server-port=8883
app.sms.yuntongxun.account-sid=
app.sms.yuntongxun.account-token=
app.sms.yuntongxun.app-id=
app.sms.yuntongxun.template-id=

app.sms.code-length=6
app.sms.code-expire-seconds=300
app.sms.send-limit-seconds=60
```

注意：

- **`template-id` 必须填写“模板ID”，不是模板内容**。
- 不要将短信平台的密钥/Token 提交到仓库。推荐使用本地私密配置或环境变量注入。

---

## 5. 数据库初始化

项目提供脚本：

- `src/main/resources/static/init_schema.sql`：仅建库建表（不插入任何数据）

执行示例：

```bash
mysql -uroot -p < src/main/resources/static/init_schema.sql
```

说明：

- 当前 `spring.jpa.hibernate.ddl-auto=update`，**启动后端时也可能自动补全/更新表结构**。
- README 里原本提到“建表之后要插入一条管理员数据”但未给出命令：
  - 如果你的代码中包含 `DataInitializer` 自动初始化管理员账号，那么无需手动插入。
  - 若你移除了初始化逻辑，请按你数据库的 `users` 表结构手动插入管理员，或补充初始化 SQL。

---

## 6. 启动方式

### 6.1 启动后端（Spring Boot）

IDEA/VScode：运行 `com.example.library_management_system.LibraryManagementSystemApplication`

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

### 6.2 启动前端（Vue 3 + Vite）

```bash
cd library_ui
npm install
npm run dev
```

前端默认地址：

- `http://localhost:5173`

---

## 7. 前端接口地址（baseURL）

前端请求封装在：`library_ui/src/utils/request.js`

- 默认 `baseURL`：`http://localhost:8080/api`
- 若你后端端口或地址有变化，请同步修改此处。

---

## 8. 默认账号

README 约定：系统启动时会自动创建管理员账号（后端通常通过 `DataInitializer` 之类初始化）：

- 用户名：`admin`
- 密码：`admin123`

***注意：这里的 password 是 BCrypt 格式（通常对应 Spring Security 的 BCryptPasswordEncoder）。如果你项目里密码编码器不是 BCrypt（例如 {noop}/PBKDF2 等），这条需要按实际编码器调整。

如果你当前代码已修改/移除初始化逻辑，请以实际为准，并建议在此处补充：

- 管理员注册方式（口令生成/注册入口）
- 或初始化 SQL

---

## 9. 登录与会话策略

前端登录信息使用 `sessionStorage` 保存（通常由 Pinia store 管理）：

- 同一 Tab 刷新不会退出登录
- 新开 Tab / 新窗口不共享登录态

后端通过 JWT（`Authorization: Bearer <token>`）鉴权。

---

## 10. 手机号验证码登录

### 10.1 API

- 发送验证码：`POST /api/auth/sms/send`
- 验证码登录：`POST /api/auth/sms/login`

### 10.2 Redis Key 约定

- 验证码：`sms:code:{phone}`（默认 5 分钟过期）
- 发送频率限制：`sms:limit:{phone}`（默认 60 秒）

### 10.3 强制设置密码

手机号验证码登录若触发自动注册（或账号被标识为需要设置密码），登录后会返回：

- `needSetPassword=true`

前端会跳转到设置密码页面；设置成功后才允许进入系统。

设置密码接口：

- `POST /api/me/set-password`

---

## 11. 借阅规则

- 每人同时最多借 3 本（PENDING + APPROVED 计入）
- 默认借阅期限：30 天

---

## 12. 常见问题

### 12.1 短信发送失败

容联云通信发送失败常见原因：

- `template-id` 配错：应填写“模板ID”，不是模板内容。

开发阶段建议先用：

```properties
app.sms.provider=mock
```

### 12.2 后端能连 Redis 但没有 redis-cli

即便 `redis-cli` 不在 PATH，只要 `redis-server` 进程存在且 `6379` 端口可连，后端仍可正常连接 Redis。

### 12.3 前后端跨域

如遇到跨域问题，优先检查：

- 后端是否配置了允许 `http://localhost:5173` 的 CORS
- 前端是否请求到了正确的后端地址（`baseURL`）

---

## 13. 许可证

仅用于学习与演示。
