package info.free.scp.ui.home

private val scpNumberPrefix = Regex("^SCP-[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*", RegexOption.IGNORE_CASE)

/** 标题开头的 SCP 编号（如 `SCP-CN-3017`）长度，没有则为 0。用于局部套用等宽字。 */
internal fun scpNumberLength(title: String): Int = scpNumberPrefix.find(title)?.value?.length ?: 0

/** 评分是否为正（`+18`、`18`），无法解析或 ≤ 0 时为 false。 */
internal fun isPositiveRank(rank: String): Boolean = rank.trim().removePrefix("+").toIntOrNull()?.let { it > 0 } == true
