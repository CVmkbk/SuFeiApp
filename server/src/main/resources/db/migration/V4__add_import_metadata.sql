-- ============================================================
-- V4: 增量导入支持
-- 新增 import_metadata 表，记录每个 JSONL 文件的导入状态
-- 支持断点续传和增量更新
-- ============================================================

CREATE TABLE IF NOT EXISTS import_metadata (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name       VARCHAR(100)  NOT NULL COMMENT 'JSONL 文件名',
    record_count    INT           NOT NULL DEFAULT 0 COMMENT '文件中待导入的总记录数',
    imported_count  INT           NOT NULL DEFAULT 0 COMMENT '已成功导入的记录数',
    status          VARCHAR(20)   NOT NULL DEFAULT 'pending' COMMENT '状态: pending/importing/completed/failed',
    started_at      TIMESTAMP     NULL DEFAULT NULL COMMENT '开始导入时间',
    completed_at    TIMESTAMP     NULL DEFAULT NULL COMMENT '完成导入时间',
    error_message   TEXT          NULL DEFAULT NULL COMMENT '错误信息',
    UNIQUE KEY uk_file_name (file_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;