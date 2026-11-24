package info.free.scp.data.catalog

import info.free.scp.bean.ScpModel
import info.free.scp.util.normalizeLink

internal fun <T : ScpModel> List<T>.distinctDocuments(): List<T> = distinctBy { normalizeLink(it.link) }

internal fun catalogRowKey(document: ScpModel): Int = document.id
