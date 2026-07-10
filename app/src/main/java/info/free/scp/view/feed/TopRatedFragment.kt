package info.free.scp.view.feed

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import info.free.scp.ui.MainTheme
import info.free.scp.ui.feed.TopRatedMenuScreen
import info.free.scp.view.base.BaseFragment

class TopRatedFragment : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MainTheme {
                    TopRatedMenuScreen(
                        showTopBar = false,
                        onBackPressed = {},
                        onOpenFeedType = ::openTopRatedList
                    )
                }
            }
        }
    }

    private fun openTopRatedList(feedType: Int) {
        startActivity(
            Intent(requireContext(), TopRatedListActivity::class.java)
                .putExtra("feedType", feedType)
        )
    }

    companion object {
        @JvmStatic
        fun newInstance() = TopRatedFragment().apply {
            arguments = Bundle()
        }
    }
}
