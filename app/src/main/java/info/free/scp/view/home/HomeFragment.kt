package info.free.scp.view.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.ui.MainTheme
import info.free.scp.ui.home.HomeTopBar
import info.free.scp.ui.home.randomTypeLabels
import info.free.scp.util.EventUtil
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
                var selectedTab by rememberSaveable { mutableIntStateOf(0) }
                MainTheme {
                    Column(modifier = Modifier.fillMaxSize()) {
                        HomeTopBar(
                            titles = titles,
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it },
                            onDirect = { startActivity(Intent(requireContext(), DirectActivity::class.java)) },
                            onRandom = { openRandom(it) },
                            onSearch = { keyword -> openSearch(keyword) }
                        )
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
            toast(getString(R.string.home_search_empty))
            return
        }
        startActivity(
            Intent(requireContext(), SearchActivity::class.java)
                .putExtra("keyword", keyword)
        )
    }

    private fun openRandom(type: Int) {
        when (type) {
            0 -> EventUtil.onEvent(context, EventUtil.clickRandomAll)
            1 -> EventUtil.onEvent(context, EventUtil.clickRandomScp)
            2 -> EventUtil.onEvent(context, EventUtil.clickRandomTale)
            3 -> EventUtil.onEvent(context, EventUtil.clickRandomJoke)
        }
        startActivity(
            Intent(requireContext(), RandomActivity::class.java)
                .putExtra("random_type", type)
                .putExtra("random_title", getString(randomTypeLabels[type]))
        )
    }

    override fun refreshTheme() {
        super.refreshTheme()
        fragments.forEach { it.refreshTheme() }
    }

    companion object {
        fun newInstance(): HomeFragment = HomeFragment()
    }
}
