-- ============================================================
-- SuFei 数据库初始化脚本 (V1)
-- 对应 Android 端: PoemEntity, PoetEntity, TagEntity, TuneEntity
-- ============================================================

-- 诗词表 (对应 PoemEntity)
CREATE TABLE IF NOT EXISTS poems (
                                     id          VARCHAR(36)   NOT NULL COMMENT 'UUID',
    source_url  TEXT                   DEFAULT NULL,
    title       VARCHAR(200)  NOT NULL COMMENT '标题',
    author      VARCHAR(100)  NOT NULL COMMENT '作者',
    dynasty     VARCHAR(50)   NOT NULL COMMENT '朝代',
    content     TEXT          NOT NULL COMMENT '正文',
    notes       TEXT                   DEFAULT NULL COMMENT '注释',
    translation TEXT                   DEFAULT NULL COMMENT '译文',
    intro       TEXT                   DEFAULT NULL COMMENT '赏析',
    background  TEXT                   DEFAULT NULL COMMENT '创作背景',
    created_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_poem_author (author),
    INDEX idx_poem_dynasty (dynasty),
    INDEX idx_poem_title (title)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 诗人表 (对应 PoetEntity)
CREATE TABLE IF NOT EXISTS poets (
                                     id           VARCHAR(36)   NOT NULL COMMENT 'UUID',
    name         VARCHAR(100)  NOT NULL COMMENT '姓名',
    dynasty      VARCHAR(50)   NOT NULL COMMENT '朝代',
    avatar_url   VARCHAR(500)           DEFAULT NULL COMMENT '头像链接',
    lifetime     VARCHAR(200)           DEFAULT NULL COMMENT '一句话生平',
    descriptions JSON                  DEFAULT NULL COMMENT '详细描述集合 (JSON数组)',
    poem_count   INT            NOT NULL DEFAULT 0 COMMENT '作品数量',
    created_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_poet_name_dynasty (name, dynasty),
    INDEX idx_poet_name (name),
    INDEX idx_poet_dynasty (dynasty)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 标签表 (对应 TagEntity)
CREATE TABLE IF NOT EXISTS tags (
                                    name VARCHAR(100) NOT NULL COMMENT '标签名',
    PRIMARY KEY (name)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 词牌表 (对应 TuneEntity)
CREATE TABLE IF NOT EXISTS tunes (
                                     name        VARCHAR(100)  NOT NULL COMMENT '词牌名',
    description VARCHAR(500)           DEFAULT NULL COMMENT '词牌描述',
    PRIMARY KEY (name)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 诗词-标签关联表 (多对多)
CREATE TABLE IF NOT EXISTS poem_tags (
                                         poem_id   VARCHAR(36)  NOT NULL,
    tag_name  VARCHAR(100) NOT NULL,
    PRIMARY KEY (poem_id, tag_name),
    CONSTRAINT fk_poem_tags_poem FOREIGN KEY (poem_id) REFERENCES poems(id) ON DELETE CASCADE,
    CONSTRAINT fk_poem_tags_tag FOREIGN KEY (tag_name) REFERENCES tags(name) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
                                     id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID (自增)',
                                     username        VARCHAR(50)  NOT NULL COMMENT '用户名',
    email           VARCHAR(255) NOT NULL COMMENT '邮箱',
    password_hash   VARCHAR(255) NOT NULL COMMENT '密码哈希 (BCrypt)',
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 收藏表
CREATE TABLE IF NOT EXISTS favorites (
                                         user_id    BIGINT       NOT NULL COMMENT '用户ID',
                                         poem_id    VARCHAR(36)  NOT NULL COMMENT '诗词ID',
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, poem_id),
    CONSTRAINT fk_favorites_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_favorites_poem FOREIGN KEY (poem_id) REFERENCES poems(id) ON DELETE CASCADE,
    INDEX idx_favorites_user (user_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;