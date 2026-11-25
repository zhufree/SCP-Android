package info.free.scp.data.feed

import info.free.scp.bean.FeedIndexModel
import info.free.scp.bean.FeedModel

/** 首页“最近原创/最近翻译”单个区块的加载状态。 */
sealed interface FeedSectionState {
    data object Loading : FeedSectionState
    data class Content(val items: List<FeedModel>) : FeedSectionState
    data object Empty : FeedSectionState
    data object Failed : FeedSectionState

    /** 重新请求开始时：已有内容保持显示，其余（失败/空/加载中）显示加载骨架。 */
    fun beginLoad(): FeedSectionState = if (this is Content) this else Loading

    /** 请求失败：已有内容保持显示（和旧行为一致），否则进入失败态。 */
    fun failed(): FeedSectionState = if (this is Content) this else Failed

    companion object {
        fun of(items: List<FeedModel>?): FeedSectionState =
            if (items.isNullOrEmpty()) Empty else Content(items)
    }
}

data class FeedIndexUiState(
    val create: FeedSectionState = FeedSectionState.Loading,
    val translate: FeedSectionState = FeedSectionState.Loading
) {
    /** 任一区块还没有内容时需要（重新）请求。 */
    val needsLoad: Boolean get() = create !is FeedSectionState.Content || translate !is FeedSectionState.Content

    fun beginLoad() = FeedIndexUiState(create.beginLoad(), translate.beginLoad())

    fun applyResult(result: Result<FeedIndexModel>): FeedIndexUiState = result.fold(
        onSuccess = {
            // Gson 不校验 Kotlin 的非空声明，缺字段时列表可能是 null
            @Suppress("USELESS_CAST")
            FeedIndexUiState(
                FeedSectionState.of(it.latestCreate as List<FeedModel>?),
                FeedSectionState.of(it.latestTranslate as List<FeedModel>?)
            )
        },
        onFailure = { FeedIndexUiState(create.failed(), translate.failed()) }
    )
}
