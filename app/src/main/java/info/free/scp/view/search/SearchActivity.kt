package info.free.scp.view.search

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.SearchType.CONTENT
import info.free.scp.SCPConstants.SearchType.TITLE
import info.free.scp.bean.ScpModel
import info.free.scp.components.CommonTopBar
import info.free.scp.ui.MainTheme
import info.free.scp.ui.search.SearchResultScreen
import info.free.scp.util.FileUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.detail.DetailActivity
import info.free.scp.view.tag.TagDetailActivity
import info.free.scp.view.user.DownloadActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchActivity : BaseActivity() {
    private val viewModel by viewModels<SearchViewModel>()
    private var keyword = ""
    private var selectedTab by mutableIntStateOf(0)
    private var titleResults by mutableStateOf<List<ScpModel>>(emptyList())
    private var contentLoading by mutableStateOf(false)
    private var contentSearched by mutableStateOf(false)
    private var needsOfflineData by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        keyword = intent?.getStringExtra("keyword") ?: ""
        searchTitle()
        setContent {
            val contentResults by viewModel.contentResult.observeAsState(emptyList())
            LaunchedEffect(contentResults) {
                if (contentSearched) {
                    contentLoading = false
                }
            }
            MainTheme {
                Scaffold(
                    topBar = {
                        CommonTopBar(
                            title = "\u641c\u7d22\u201c$keyword\u201d",
                            onBackPressed = { finish() }
                        )
                    },
                    backgroundColor = Color(ThemeUtil.containerBg)
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        androidx.compose.foundation.layout.Column {
                            SearchTabs(
                                selectedTab = selectedTab,
                                onSelectTab = { tab ->
                                    selectedTab = tab
                                    if (tab == 2) {
                                        openTagSearch()
                                    }
                                }
                            )
                            LaunchedEffect(selectedTab) {
                                if (selectedTab == 1 && !contentSearched) {
                                    searchContent()
                                }
                            }
                            when (selectedTab) {
                                0 -> SearchResultScreen(
                                    results = titleResults,
                                    loading = false,
                                    needsOfflineData = false,
                                    onOpenResult = { openResult(it) },
                                    onOpenDownload = { openDownload() }
                                )
                                1 -> SearchResultScreen(
                                    results = contentResults,
                                    loading = contentLoading,
                                    needsOfflineData = needsOfflineData,
                                    onOpenResult = { openResult(it) },
                                    onOpenDownload = { openDownload() }
                                )
                                else -> SearchResultScreen(
                                    results = emptyList(),
                                    loading = false,
                                    needsOfflineData = false,
                                    onOpenResult = { openResult(it) },
                                    onOpenDownload = { openDownload() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun searchTitle() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.searchScp(TITLE, "%$keyword%")
            withContext(Dispatchers.Main) {
                titleResults = viewModel.titleResult
            }
        }
    }

    private fun searchContent() {
        if (FileUtil.checkDataReady(SCPConstants.DETAIL_DB_NAME)) {
            needsOfflineData = false
            contentLoading = true
            viewModel.searchScp(CONTENT, "%$keyword%")
            contentSearched = true
        } else {
            needsOfflineData = true
        }
    }

    private fun openResult(search: ScpModel) {
        startActivity(
            Intent(this, DetailActivity::class.java)
                .putExtra("link", search.link)
                .putExtra("title", search.link)
        )
    }

    private fun openDownload() {
        startActivity(Intent(this, DownloadActivity::class.java))
    }

    private fun openTagSearch() {
        startActivity(
            Intent(this, TagDetailActivity::class.java)
                .putExtra("tag", keyword)
        )
    }
}

@Composable
private fun SearchTabs(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit
) {
    val titles = listOf(
        "\u6807\u9898\u641c\u7d22",
        "\u5168\u6587\u641c\u7d22",
        "Tag\u641c\u7d22"
    )
    TabRow(
        selectedTabIndex = selectedTab,
        backgroundColor = Color(ThemeUtil.itemBg),
        contentColor = Color(ThemeUtil.accentColor)
    ) {
        titles.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = { onSelectTab(index) },
                text = {
                    Text(
                        text = title,
                        color = if (selectedTab == index) {
                            Color(ThemeUtil.darkText)
                        } else {
                            Color(ThemeUtil.mediumText)
                        }
                    )
                }
            )
        }
    }
}
