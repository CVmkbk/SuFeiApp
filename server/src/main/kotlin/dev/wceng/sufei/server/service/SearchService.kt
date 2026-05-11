package dev.wceng.sufei.server.service

import dev.wceng.sufei.server.database.DatabaseFactory
import dev.wceng.sufei.server.models.PoemListItem
import dev.wceng.sufei.server.models.PoetListItem
import dev.wceng.sufei.server.models.SearchResultResponse
import java.sql.ResultSet

object SearchService {

    fun searchAll(
        query: String?,
        dynasty: String?,
        tag: String?,
        tune: String?,
        limit: Int
    ): SearchResultResponse {
        val poems = searchPoems(query, dynasty, tag, tune, limit)
        val poets = if (!query.isNullOrBlank() && tag == null && tune == null && dynasty == null) {
            PoetService.searchPoets(query, limit)
        } else {
            emptyList()
        }

        return SearchResultResponse(poems = poems, poets = poets)
    }

    private fun searchPoems(
        query: String?,
        dynasty: String?,
        tag: String?,
        tune: String?,
        limit: Int
    ): List<PoemListItem> {
        val items = mutableListOf<PoemListItem>()
        val conditions = mutableListOf<String>()
        val params = mutableListOf<Any?>()

        if (!query.isNullOrBlank()) {
            conditions.add("(p.title LIKE ? OR p.author LIKE ? OR p.content LIKE ?)")
            val likeQuery = "%$query%"
            params.add(likeQuery); params.add(likeQuery); params.add(likeQuery)
        }

        if (!dynasty.isNullOrBlank()) {
            conditions.add("p.dynasty = ?")
            params.add(dynasty)
        }

        if (!tag.isNullOrBlank()) {
            conditions.add("EXISTS (SELECT 1 FROM poem_tags pt WHERE pt.poem_id = p.id AND pt.tag_name = ?)")
            params.add(tag)
        }

        if (!tune.isNullOrBlank()) {
            conditions.add("p.title LIKE ?")
            params.add("%$tune%")
        }

        val whereClause = if (conditions.isNotEmpty()) "WHERE ${conditions.joinToString(" AND ")}" else ""

        DatabaseFactory.getDataSource().connection.use { conn ->
            val sql = """
                SELECT p.id, p.title, p.author, p.dynasty, p.content,
                       GROUP_CONCAT(pt.tag_name) as tags
                FROM poems p
                LEFT JOIN poem_tags pt ON p.id = pt.poem_id
                $whereClause
                GROUP BY p.id
                LIMIT ?
            """.trimIndent()

            conn.prepareStatement(sql).use { stmt ->
                params.forEachIndexed { index, param -> stmt.setObject(index + 1, param) }
                stmt.setObject(params.size + 1, limit)
                stmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        items.add(toSearchPoemItem(rs))
                    }
                }
            }
        }

        return items
    }

    private fun toSearchPoemItem(rs: ResultSet): PoemListItem {
        val tagsStr = rs.getString("tags")
        val tags = if (tagsStr != null) tagsStr.split(",") else emptyList()
        return PoemListItem(
            id = rs.getString("id"),
            title = rs.getString("title"),
            author = rs.getString("author"),
            dynasty = rs.getString("dynasty"),
            content = rs.getString("content"),
            tags = tags
        )
    }
}