package info.free.scp.ui.user

/**
 * “我的”页入口网格的列跨度：每行 [columns] 列，最后一行不满时把空列并给最后一格（7 项 → 4 + 3，末格跨 2 列；8 项 → 4 + 4）。
 */
internal fun entryRowSpans(count: Int, columns: Int = 4): List<List<Int>> {
    if (count <= 0 || columns <= 0) return emptyList()
    return (0 until count).chunked(columns).map { row ->
        List(row.size) { index -> if (index == row.lastIndex) 1 + columns - row.size else 1 }
    }
}
