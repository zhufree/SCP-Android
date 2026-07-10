package info.free.scp.view.later

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.ViewModelProvider
import info.free.scp.bean.ScpLikeBox
import info.free.scp.ui.MainTheme
import info.free.scp.ui.later.LikeBoxListScreen
import info.free.scp.view.base.BaseFragment

class LikeBoxListFragment : BaseFragment() {
    private val viewModel by lazy {
        ViewModelProvider(this)[LaterViewModel::class.java]
    }
    private var likeBoxes by mutableStateOf<List<ScpLikeBox>>(emptyList())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): ComposeView {
        refreshLikeBoxes()
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MainTheme {
                    LikeBoxListScreen(
                        likeBoxes = likeBoxes,
                        onOpenBox = { openLikeBox(it) },
                        onDeleteAndClear = { deleteAndClear(it) },
                        onDeleteAndMove = { deleteAndMove(it) },
                        onRenameBox = { box, newName -> renameBox(box, newName) }
                    )
                }
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            refreshLikeBoxes()
        }
    }

    private fun refreshLikeBoxes() {
        likeBoxes = viewModel.getLikeBoxList()
    }

    private fun openLikeBox(box: ScpLikeBox) {
        startActivity(
            Intent(requireContext(), LikeListActivity::class.java)
                .putExtra("box_id", box.id)
                .putExtra("box_name", box.name)
        )
    }

    private fun deleteAndClear(box: ScpLikeBox) {
        viewModel.deleteLikeBoxAndClear(box.id)
        refreshLikeBoxes()
    }

    private fun deleteAndMove(box: ScpLikeBox) {
        viewModel.deleteLikeBoxAndMoveToDefault(box.id)
        refreshLikeBoxes()
    }

    private fun renameBox(box: ScpLikeBox, newName: String) {
        viewModel.renameLikeBox(box.id, newName)
        refreshLikeBoxes()
    }

    companion object {
        fun newInstance(): LikeBoxListFragment {
            val fragment = LikeBoxListFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
