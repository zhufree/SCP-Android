package info.free.scp.data.feed

import info.free.scp.bean.FeedIndexModel
import info.free.scp.bean.FeedModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedIndexStateTest {
    private val item = FeedModel("SCP-CN-001", "/scp-cn-001", "2026-11-24", "+3")

    @Test
    fun initialStateIsLoadingAndNeedsLoad() {
        val state = FeedIndexUiState()
        assertEquals(FeedSectionState.Loading, state.create)
        assertEquals(FeedSectionState.Loading, state.translate)
        assertTrue(state.needsLoad)
    }

    @Test
    fun successSplitsContentAndEmpty() {
        val state = FeedIndexUiState().applyResult(Result.success(FeedIndexModel(listOf(item), emptyList())))
        assertEquals(FeedSectionState.Content(listOf(item)), state.create)
        assertEquals(FeedSectionState.Empty, state.translate)
        assertTrue(state.needsLoad)
    }

    @Test
    fun failureWithoutContentIsFailedAndRetryShowsLoading() {
        val failed = FeedIndexUiState().applyResult(Result.failure(RuntimeException("net")))
        assertEquals(FeedSectionState.Failed, failed.create)
        assertEquals(FeedSectionState.Failed, failed.translate)
        val retrying = failed.beginLoad()
        assertEquals(FeedSectionState.Loading, retrying.create)
        assertEquals(FeedSectionState.Loading, retrying.translate)
    }

    @Test
    fun failureKeepsPreviouslyLoadedContent() {
        val loaded = FeedIndexUiState().applyResult(Result.success(FeedIndexModel(listOf(item), listOf(item))))
        assertFalse(loaded.needsLoad)
        assertEquals(loaded, loaded.beginLoad())
        assertEquals(loaded, loaded.applyResult(Result.failure(RuntimeException("net"))))
    }

    @Test
    fun retrySucceedsAfterFailure() {
        val failed = FeedIndexUiState().applyResult(Result.failure(RuntimeException("net")))
        val recovered = failed.beginLoad().applyResult(Result.success(FeedIndexModel(listOf(item), listOf(item))))
        assertEquals(FeedSectionState.Content(listOf(item)), recovered.create)
        assertEquals(FeedSectionState.Content(listOf(item)), recovered.translate)
    }

    @Test
    fun nullListsFromGsonAreTreatedAsEmpty() {
        // Gson 反序列化缺字段的 JSON 时不会校验 Kotlin 的非空声明
        val broken = com.google.gson.Gson().fromJson("{}", FeedIndexModel::class.java)
        val state = FeedIndexUiState().applyResult(Result.success(broken))
        assertEquals(FeedSectionState.Empty, state.create)
        assertEquals(FeedSectionState.Empty, state.translate)
    }
}
