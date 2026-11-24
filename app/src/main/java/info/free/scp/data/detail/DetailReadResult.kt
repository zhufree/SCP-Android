package info.free.scp.data.detail

import info.free.scp.bean.ScpDetail

internal sealed interface DetailReadResult {
    data class Hit(val html: String, val tags: String?) : DetailReadResult
    data object Missing : DetailReadResult
    data object NotFound : DetailReadResult
    data object Failed : DetailReadResult
}

internal fun classifyDetail(row: ScpDetail?, failure: Exception? = null): DetailReadResult {
    val html = row?.detail
    return when {
        failure != null -> DetailReadResult.Failed
        row == null -> DetailReadResult.Missing
        row.notFound == 1 || row.notFound == 404 -> DetailReadResult.NotFound
        html.isNullOrBlank() || html.trim() == "null" -> DetailReadResult.Missing
        else -> DetailReadResult.Hit(html, row.tags)
    }
}
