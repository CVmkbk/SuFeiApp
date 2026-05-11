-- ============================================================
-- 全文检索索引 (V2)
-- 注意: FULLTEXT 索引需要 InnoDB 引擎和 utf8mb4 字符集
-- ============================================================

-- 诗词全文索引 (标题 + 作者 + 正文)
ALTER TABLE poems ADD FULLTEXT INDEX ft_poem_search (title, author, content);

-- 诗人全文索引 (姓名 + 生平)
ALTER TABLE poets ADD FULLTEXT INDEX ft_poet_search (name, lifetime);