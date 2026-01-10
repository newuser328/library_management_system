-- 初始化数据库结构脚本（不插入任何业务数据）
-- 适用于 MySQL 8+

DROP DATABASE IF EXISTS library_management_system;

CREATE DATABASE IF NOT EXISTS library_management_system
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE library_management_system;

-- =========================
-- Tables Structure
-- =========================

CREATE TABLE `users` (
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
  UNIQUE KEY `uk_users_username` (`username`),
  UNIQUE KEY `uk_users_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `books` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `isbn` varchar(32) NOT NULL,
  `title` varchar(128) NOT NULL,
  `title_pinyin` varchar(32) DEFAULT NULL,
  `author` varchar(64) DEFAULT NULL,
  -- 兼容旧字段：字符串分类（新逻辑以 categories + book_categories 为准）
  `category` varchar(64) DEFAULT NULL,
  `publisher` varchar(64) DEFAULT NULL,
  `description` varchar(2048) DEFAULT NULL,
  `cover_url` varchar(512) DEFAULT NULL,
  `total_count` int NOT NULL,
  `available_count` int NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_books_isbn` (`isbn`),
  KEY `idx_books_isbn` (`isbn`),
  KEY `idx_books_title` (`title`),
  KEY `idx_books_author` (`author`),
  KEY `idx_books_category` (`category`),
  KEY `idx_books_title_pinyin` (`title_pinyin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `categories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(32) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_categories_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `book_categories` (
  `book_id` bigint NOT NULL,
  `category_id` bigint NOT NULL,
  PRIMARY KEY (`book_id`, `category_id`),
  KEY `idx_book_categories_category_id` (`category_id`),
  CONSTRAINT `fk_book_categories_book` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_book_categories_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `borrows` (
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

CREATE TABLE `admin_register_tokens` (
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
