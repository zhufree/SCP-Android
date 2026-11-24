package info.free.scp.data.draft

// Allocation and insertion run in one SQLite write statement.
internal const val INSERT_DRAFT_SQL = """
    INSERT INTO draft (draftId, lastModifyTime, title, content)
    SELECT next_id, :lastModifyTime, :title, :content
    FROM (SELECT max(0, coalesce(max(draftId), 0)) + 1 AS next_id FROM draft)
    WHERE next_id <= 2147483647
"""
