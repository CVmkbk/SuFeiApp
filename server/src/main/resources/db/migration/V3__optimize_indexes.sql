-- ============================================================
-- V3: 性能优化索引
-- 1. 重建 FULLTEXT 索引 (使用 ngram parser 支持中文分词)
-- 2. 新增 poems.created_at 索引 (加速 ORDER BY created_at DESC)
-- ============================================================

-- 删除旧的 FULLTEXT 索引（默认 parser 不支持中文分词）
ALTER TABLE poems DROP INDEX ft_poem_search;
ALTER TABLE poets DROP INDEX ft_poet_search;

-- 重建 FULLTEXT 索引（ngram parser 适用于 CJK 文本）
ALTER TABLE poems ADD FULLTEXT INDEX ft_poem_search (title, author, content) WITH PARSER ngram;
ALTER TABLE poets ADD FULLTEXT INDEX ft_poet_search (name, lifetime) WITH PARSER ngram;

-- 为 poems.created_at 添加普通索引（加速 ORDER BY created_at DESC）
ALTER TABLE poems ADD INDEX idx_poems_created_at (created_at);