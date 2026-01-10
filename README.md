# 图书馆管理系统（Spring Boot 4 + Vue 3 + Element Plus）

本项目是一个前后端分离的图书馆管理系统：

- 后端：Spring Boot 4 + Spring Security + JWT + Spring Data JPA + MySQL + Redis
- 前端：Vue 3 + Vite + Element Plus + Pinia + Vue Router

功能概览：

- 管理员端：图书/分类/用户/借阅管理、管理员注册口令生成
- 读者端：图书浏览、批量借阅申请（借阅对话框展示当前选中书籍）、我的借阅、个人中心
- 认证：账号密码登录 + 手机号验证码登录（容联云通信 / Mock）
- 安全增强：手机号验证码自动注册用户必须先设置密码（设置后才可进入系统）

---

## 1. 项目结构

```text
library_management_system/               # 后端（Spring Boot）
├─ src/main/java/com/example/library_management_system
├─ src/main/resources
│  ├─ application.properties
│  └─ static/
│     ├─ int.sql                         # 初始化脚本（含示例数据）
│     └─ init_schema.sql                 # 初始化脚本（仅建表，不插入数据）
├─ pom.xml
└─ README.md

library_ui/                              # 前端（Vue3 + Vite + Element Plus）
├─ src
└─ package.json
```

---

## 2. 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8+
- Redis（用于短信验证码存储与限流）

---

## 3. 数据库初始化

项目提供脚本：

- `src/main/resources/static/init_schema.sql`：仅建库建表（不插入任何数据）

执行示例（含示例数据）：

```bash
mysql -uroot -p < src/main/resources/static/int.sql
```

执行示例（仅建表，不插入数据）：

```bash
mysql -uroot -p < src/main/resources/static/init_schema.sql
```

---

## 4. 配置说明

配置文件：`src/main/resources/application.properties`

### 4.1 MySQL 配置

按本机修改：

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

### 4.2 Redis 配置

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=
spring.data.redis.database=0
spring.data.redis.timeout=3000
```

### 4.3 JWT 配置

```properties
app.jwt.secret=please_change_this_secret_to_a_long_random_string_at_least_32_chars
app.jwt.expire-seconds=86400
```

### 4.4 短信服务配置（容联云通信 / Mock）

开发阶段建议使用 Mock（验证码输出到后端日志）：

```properties
app.sms.provider=mock
```

生产环境使用容联云通信：

```properties
app.sms.provider=yuntongxun
app.sms.yuntongxun.server-ip=app.cloopen.com
app.sms.yuntongxun.server-port=8883
app.sms.yuntongxun.account-sid=你的AccountSID
app.sms.yuntongxun.account-token=你的AccountToken
app.sms.yuntongxun.app-id=你的AppID
app.sms.yuntongxun.template-id=你的模板ID

app.sms.code-length=6
app.sms.code-expire-seconds=300
app.sms.send-limit-seconds=60
```

注意：

- **`template-id` 必须填写“模板ID”，不是模板内容**
- 请不要把短信平台的 `Token`/`密钥` 等敏感信息提交到仓库，建议使用环境变量或本地私密配置

---

## 5. 启动方式

### 5.1 启动后端

IDEA：运行 `com.example.library_management_system.LibraryManagementSystemApplication`

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

### 5.2 启动前端

```bash
cd library_ui
npm i
npm run dev
```

前端默认地址：

- `http://localhost:5173`

---

## 6. 默认账号

系统启动时会自动创建管理员账号（后端 `DataInitializer` 初始化）：

- 用户名：`admin`
- 密码：`admin123`



---

## 7. 登录与会话策略

前端登录信息使用 `sessionStorage` 保存：

- 同一 Tab 刷新不会退出登录
- 新开 Tab / 新窗口不共享登录态

---

## 8. 手机号验证码登录

### 8.1 API

- 发送验证码：`POST /api/auth/sms/send`
- 验证码登录：`POST /api/auth/sms/login`

### 8.2 Redis Key 约定

- 验证码：`sms:code:{phone}`（默认 5 分钟过期）
- 发送频率限制：`sms:limit:{phone}`（默认 60 秒）

### 8.3 强制设置密码

手机号验证码登录若触发自动注册（或账号被标识为需要设置密码），登录后会返回：

- `needSetPassword=true`

前端会强制跳转到 `/set-password` 页面；设置成功后才允许进入系统。

设置密码接口：

- `POST /api/me/set-password`

---

## 9. 借阅规则

- 每人同时最多借 3 本（PENDING + APPROVED 计入）
- 默认借阅期限：30 天

---

## 10. 常见问题

### 10.1 短信发送失败

容联云通信发送失败最常见原因：

- `template-id` 配错：应填写“模板ID”，不是模板内容。

开发阶段建议先用：

```properties
app.sms.provider=mock
```

### 10.2 Redis CLI 不存在

即便 `redis-cli` 不在 PATH，只要 `redis-server` 进程存在且 6379 端口可连，后端仍可正常连接 Redis。

---

## 11. 许可证

仅用于学习与演示。
