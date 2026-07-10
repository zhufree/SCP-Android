package info.free.scp.view.home

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.ui.MainTheme
import info.free.scp.ui.home.SearchBarScreen
import info.free.scp.util.EventUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.category.CategoryFragment
import info.free.scp.view.category.ScpListFragment
import info.free.scp.view.random.RandomActivity
import info.free.scp.view.search.SearchActivity
import toast

class HomeFragment : BaseFragment() {
    private val fragments: List<BaseFragment> by lazy {
        listOf(
            HomePageFragment.newInstance(),
            ScpListFragment.newInstance(SCPConstants.Entry.LIBRARY_DOC, 0, ""),
            CategoryFragment.newInstance(SCPConstants.Entry.INTERNATIONAL_DOC, 0),
            CategoryFragment.newInstance(SCPConstants.Entry.GOI_DOC, 0),
            CategoryFragment.newInstance(SCPConstants.Entry.ART_DOC, 0),
            ScpListFragment.newInstance(SCPConstants.Entry.INFORMATION_DOC, 0, "")
        )
    }
    private val titles = listOf(
        "\u9996\u9875",
        "\u56fe\u4e66\u9986",
        "SCP\u56fd\u9645\u7248",
        "GOI\u683c\u5f0f",
        "\u827a\u672f\u4f5c\u54c1",
        "\u80cc\u666f\u8d44\u6599\u4e0e\u6307\u5bfc"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                var selectedTab by remember { mutableIntStateOf(0) }
                MainTheme {
                    Column(modifier = Modifier.fillMaxSize()) {
                        SearchBarScreen(
                            onDirect = { startActivity(Intent(requireContext(), DirectActivity::class.java)) },
                            onRandom = { showRandomDialog() },
                            onSearch = { keyword -> openSearch(keyword) }
                        )
                        ScrollableTabRow(
                            selectedTabIndex = selectedTab,
                            backgroundColor = Color(ThemeUtil.itemBg),
                            contentColor = Color(ThemeUtil.accentColor)
                        ) {
                            titles.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    text = { Text(text = title) }
                                )
                            }
                        }
                        AndroidView(
                            modifier = Modifier.weight(1f),
                            factory = { context ->
                                android.widget.FrameLayout(context).apply {
                                    id = R.id.fl_home_container
                                }
                            }
                        )
                    }
                }
                LaunchedEffect(selectedTab) {
                    showChildFragment(fragments[selectedTab])
                }
            }
        }
    }

    private fun showChildFragment(fragment: Fragment) {
        if (childFragmentManager.isStateSaved) {
            return
        }
        childFragmentManager.beginTransaction()
            .replace(R.id.fl_home_container, fragment)
            .commit()
    }

    private fun openSearch(keyword: String) {
        if (keyword.isEmpty()) {
            toast("\u8bf7\u8f93\u5165\u6b63\u786e\u7684\u5173\u952e\u8bcd")
            return
        }
        startActivity(
            Intent(requireContext(), SearchActivity::class.java)
                .putExtra("keyword", keyword)
        )
    }

    private fun showRandomDialog() {
        val randomTypes = listOf("\u968f\u673a\u5168\u90e8", "\u968f\u673aSCP", "\u968f\u673a\u6545\u4e8b", "\u968f\u673a\u641e\u7b11")
        AlertDialog.Builder(requireContext())
            .setItems(randomTypes.toTypedArray()) { _, i ->
                when (i) {
                    0 -> EventUtil.onEvent(context, EventUtil.clickRandomAll)
                    1 -> EventUtil.onEvent(context, EventUtil.clickRandomScp)
                    2 -> EventUtil.onEvent(context, EventUtil.clickRandomTale)
                    3 -> EventUtil.onEvent(context, EventUtil.clickRandomJoke)
                }
                startActivity(
                    Intent(requireContext(), RandomActivity::class.java)
                        .putExtra("random_type", i)
                        .putExtra("random_title", randomTypes[i])
                )
            }
            .show()
    }

    override fun refreshTheme() {
        super.refreshTheme()
        fragments.forEach { it.refreshTheme() }
    }

    companion object {
        fun newInstance(): HomeFragment = HomeFragment()
    }
}
