CREATE DATABASE IF NOT EXISTS library_management_system
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE library_management_system;

-- =========================
-- Tables Structure
-- =========================


CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(64) NOT NULL,
  `password` varchar(255) NOT NULL,
  `name` varchar(64) NOT NULL,
  `phone` varchar(32) DEFAULT NULL,
  `email` varchar(128) DEFAULT NULL,
  `role` varchar(16) NOT NULL,
  `enabled` tinyint(1) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `books` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `isbn` varchar(32) NOT NULL,
  `title` varchar(128) NOT NULL,
  `title_pinyin` varchar(32) DEFAULT NULL,
  `author` varchar(64) DEFAULT NULL,
  `category` varchar(64) DEFAULT NULL,
  `publisher` varchar(64) DEFAULT NULL,
  `description` varchar(2048) DEFAULT NULL,
  `cover_url` varchar(512) DEFAULT NULL,
  `total_count` int NOT NULL,
  `available_count` int NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_books_isbn` (`isbn`),/*`books.isbn` 设为唯一键：确保图书按 ISBN 去重*/
  KEY `idx_books_isbn` (`isbn`),
  KEY `idx_books_title` (`title`),
  KEY `idx_books_author` (`author`),
  KEY `idx_books_category` (`category`),
  KEY `idx_books_title_pinyin` (`title_pinyin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `borrows` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `book_id` bigint NOT NULL,
  `status` varchar(16) NOT NULL,
  `request_time` datetime NOT NULL,
  `approve_time` datetime DEFAULT NULL,
  `reject_time` datetime DEFAULT NULL,
  `return_time` datetime DEFAULT NULL,
  `due_date` date DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_borrows_user_id` (`user_id`),
  KEY `idx_borrows_book_id` (`book_id`),
  KEY `idx_borrows_status` (`status`),
  KEY `idx_borrows_due_date` (`due_date`),
  CONSTRAINT `fk_borrows_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_borrows_book` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `admin_register_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `token` varchar(64) NOT NULL,
  `created_at` datetime NOT NULL,
  `expires_at` datetime NOT NULL,
  `used` tinyint(1) NOT NULL,
  `used_at` datetime DEFAULT NULL,
  `created_by_user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_register_tokens_token` (`token`),
  KEY `idx_admin_register_tokens_expires_at` (`expires_at`),
  KEY `idx_admin_register_tokens_used` (`used`),
  KEY `idx_admin_register_tokens_created_by_user_id` (`created_by_user_id`),
  CONSTRAINT `fk_admin_register_tokens_created_by_user` FOREIGN KEY (`created_by_user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- =========================
-- /以下为实验数据
-- =========================

-- Note: An 'admin' user (password 'admin123') is automatically created by DataInitializer.java on startup.
-- The passwords for the users below are all '123456'.
INSERT INTO `users` (`username`, `password`, `name`, `phone`, `email`, `role`, `enabled`, `created_at`, `updated_at`)
VALUES
  ('reader1', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '张三', '13800138001', 'zhangsan@example.com', 'READER', 1, NOW(), NOW()),
  ('reader2', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '李四', '13800138002', 'lisi@example.com', 'READER', 1, NOW(), NOW()),
  ('reader3', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '王五', '13800138003', 'wangwu@example.com', 'READER', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE/*可重复执行不报错*//*同一用户只会更新数据*/
  `password` = VALUES(`password`),
  `name` = VALUES(`name`),
  `phone` = VALUES(`phone`),
  `email` = VALUES(`email`),
  `role` = VALUES(`role`),
  `enabled` = VALUES(`enabled`),
  `updated_at` = VALUES(`updated_at`);

INSERT INTO `books` (`isbn`, `title`, `title_pinyin`, `author`, `category`, `publisher`, `description`, `total_count`, `available_count`, `created_at`, `updated_at`)
VALUES
  ('9787532722735', '三体', 'ST', '刘慈欣', '科幻', '重庆出版社', '在文化大革命如火如荼进行的同时，军方探寻外星文明的绝秘计划“红岸工程”取得了突破性进展。', 10, 10, NOW(), NOW()),
  ('9787544270878', '活着', 'HZ', '余华', '文学', '作家出版社', '讲述了在大时代背景下，随着内战、三反五反、大跃进、文化大革命等社会变革，徐福贵的人生和家庭不断经受着苦难的故事。', 8, 8, NOW(), NOW()),
  ('9787111128069', 'Java编程思想', 'JVBCTX', 'Bruce Eckel', '计算机', '机械工业出版社', '本书赢得了全球程序员的广泛赞誉，即使是最晦涩的概念，在Bruce Eckel的文字亲和力和小而直接的编程示例面前也会化解于无形。', 15, 12, NOW(), NOW()),
  ('9787115178508', 'Clean Code', 'CLEANCODE', 'Robert C. Martin', '计算机', '人民邮电出版社', '代码整洁之道。', 12, 12, NOW(), NOW()),
  ('9787544253994', '百年孤独', 'BNGD', '加西亚·马尔克斯', '文学', '南海出版公司', '作品描写了布恩迪亚家族七代人的传奇故事，以及加勒比海沿岸小镇马孔多的百年兴衰，反映了拉丁美洲一个世纪以来风云变幻的历史。', 7, 5, NOW(), NOW()),
  ('9787508647357', '人类简史', 'RLJS', '尤瓦尔·赫拉利', '历史', '中信出版社', '从十万年前有生命迹象开始到21世纪资本、科技交织的人类发展史。', 9, 9, NOW(), NOW()),
  ('9780618640157', 'The Lord of the Rings', 'THELORDOFTHERINGS', 'J.R.R. Tolkien', '科幻', 'Houghton Mifflin', 'The Fellowship of the Ring, The Two Towers, The Return of the King.', 5, 3, NOW(), NOW()),
  ('9787020024759', '围城', 'WC', '钱钟书', '文学', '人民文学出版社', '《围城》是中国现代文学史上一部风格独特的讽刺小说。', 6, 6, NOW(), NOW()),
  ('9787115428836', '深入理解计算机系统', 'SRLJJSJXT', 'Randal E. Bryant', '计算机', '机械工业出版社', '程序员必读经典著作，理解计算机系统首选。', 11, 11, NOW(), NOW()),
  ('9787505747048', '明朝那些事儿', 'MCNSSE', '当年明月', '历史', '中国海关出版社', '以史料为基础，以年代和具体人物为主线，并加入了小说的笔法，对明朝十七帝和其他王公权贵和小人物的命运进行全景展示。', 20, 20, NOW(), NOW()),
  ('9787533957291', '流浪地球', 'LLDQ', '刘慈欣', '科幻', '浙江文艺出版社', '刘慈欣的科幻小说作品。', 10, 10, NOW(), NOW()),
  ('9787508677552', '原则', 'YZ', '瑞·达利欧', '经管', '中信出版社', 'Principles: Life and Work', 8, 8, NOW(), NOW()),
  ('9787540484882', '万历十五年', 'WLSWN', '黄仁宇', '历史', '中华书局', '叙述了明朝万历十五年（1587年）所发生的一系列看似无关紧要的事件。', 4, 4, NOW(), NOW()),
  ('9787543683329', '解忧杂货店', 'JYzhd', '东野圭吾', '文学', '南海出版公司', '僻静的街道旁有一家杂货店，只要写下烦恼投进卷帘门的投信口，第二天就会在店后的牛奶箱里得到回答。', 13, 13, NOW(), NOW()),
  ('9787121282887', 'Effective Java', 'EFFECTIVEJAVA', 'Joshua Bloch', '计算机', '机械工业出版社', '本书介绍了在Java编程中78条极具实用价值的经验规则。', 10, 10, NOW(), NOW())
ON DUPLICATE KEY UPDATE/*可重复执行不报错*/
  `title` = VALUES(`title`),
  `title_pinyin` = VALUES(`title_pinyin`),
  `author` = VALUES(`author`),
  `category` = VALUES(`category`),
  `publisher` = VALUES(`publisher`),
  `description` = VALUES(`description`),
  `total_count` = VALUES(`total_count`),
  `available_count` = VALUES(`available_count`),
  `updated_at` = VALUES(`updated_at`);
