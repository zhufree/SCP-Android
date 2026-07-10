package info.free.scp.view.later

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import info.free.scp.R
import info.free.scp.bean.ScpLikeBox
import info.free.scp.bean.ScpLikeModel
import info.free.scp.components.CommonTopBar
import info.free.scp.ui.MainTheme
import info.free.scp.ui.later.LikeListScreen
import info.free.scp.util.ThemeUtil
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.detail.DetailActivity

class LikeListActivity : BaseActivity() {
    private val viewModel by viewModels<LaterViewModel>()
    private var boxId = 0
    private var boxName = "收藏列表"
    private var orderType by mutableIntStateOf(0)
    private var likeList by mutableStateOf<List<ScpLikeModel>>(emptyList())
    private var likeBoxes by mutableStateOf<List<ScpLikeBox>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        boxId = intent?.getIntExtra("box_id", 0) ?: 0
        boxName = intent?.getStringExtra("box_name") ?: "收藏列表"
        refreshLists()

        setContent {
            MainTheme {
                Scaffold(
                    topBar = {
                        CommonTopBar(
                            title = boxName,
                            actions = {
                                IconButton(onClick = { reverseOrder() }) {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_swap_vertical_circle_white_24dp),
                                        tint = Color(ThemeUtil.lightText),
                                        contentDescription = "Reverse"
                                    )
                                }
                            },
                            onBackPressed = { finish() }
                        )
                    },
                    backgroundColor = Color(ThemeUtil.containerBg)
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        LikeListScreen(
                            likes = likeList,
                            likeBoxes = likeBoxes,
                            onOpenLike = { like -> openLike(like) },
                            onDeleteLike = { like -> deleteLike(like) },
                            onMoveLike = { like, box -> moveLike(like, box) }
                        )
                    }
                }
            }
        }
    }

    private fun refreshLists() {
        val orderedList = mutableListOf<ScpLikeModel>()
        if (boxId == 1) {
            orderedList.addAll(viewModel.getLikeListByBoxId(0))
        }
        orderedList.addAll(viewModel.getLikeListByBoxId(boxId))
        likeList = if (orderType == 1) {
            orderedList.sortedBy { it.title }
        } else {
            orderedList
        }
        likeBoxes = viewModel.getExistingLikeBoxes()
    }

    private fun reverseOrder() {
        orderType = if (orderType == 0) 1 else 0
        refreshLists()
    }

    private fun openLike(like: ScpLikeModel) {
        startActivity(
            Intent(this, DetailActivity::class.java)
                .putExtra("link", like.link)
                .putExtra("title", like.title)
        )
    }

    private fun deleteLike(like: ScpLikeModel) {
        viewModel.deleteLike(like)
        refreshLists()
    }

    private fun moveLike(like: ScpLikeModel, box: ScpLikeBox) {
        viewModel.moveLike(like, box.id)
        refreshLists()
    }
}
