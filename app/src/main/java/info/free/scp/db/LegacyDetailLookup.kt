package info.free.scp.db

// Called only on a primary-key miss. SQLite lower() handles historical ASCII spellings.
internal const val LEGACY_DETAIL_LOOKUP = """
    SELECT link, detail, not_found, tags FROM scp_detail
    WHERE rtrim(lower(substr(trim(link), 1,
        min(instr(trim(link) || '#', '#'), instr(trim(link) || '?', '?')) - 1)), '/') IN
        (lower(:link), 'http://scp-wiki-cn.wikidot.com' || lower(:link),
            'https://scp-wiki-cn.wikidot.com' || lower(:link), 'file://' || lower(:link))
    ORDER BY link COLLATE BINARY LIMIT 1
"""
