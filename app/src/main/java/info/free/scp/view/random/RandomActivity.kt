package info.free.scp.view.random

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import info.free.scp.ui.MainTheme
import info.free.scp.ui.random.RandomScreen
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.category.GroupViewModel
import info.free.scp.view.detail.DetailActivity

class RandomActivity : BaseActivity() {
    private val vm: GroupViewModel by lazy {
        ViewModelProvider(this)[GroupViewModel::class.java]
    }
    private var randomType = 0 // 0 所有，1仅scp，2 故事，3 joke
    private val randomRange: String
        get() = when (randomType) {
            1 -> "1,2"
            2 -> "7,8"
            3 -> "3,4"
            else -> ""
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        randomType = intent.getIntExtra("random_type", 0)
        val title = intent.getStringExtra("random_title") ?: "随机"
        setContent {
            val randomState by vm.randomUiState.collectAsState()
            val randomList = randomState.documents
            LaunchedEffect(randomRange) {
                vm.refreshRandomList(randomRange)
            }
            MainTheme {
                RandomScreen(
                    title = title,
                    randomList = randomList,
                    onBackPressed = { finish() },
                    onRefresh = { vm.refreshRandomList(randomRange) },
                    onAddMore = { vm.refreshRandomList(randomRange, true) },
                    onOpenRandom = { scp, index ->
                        val randomLinkList = ArrayList(randomList.map { it.link })
                        val randomTitleList = ArrayList(randomList.map { it.title })
                        startActivity(
                            Intent(this, DetailActivity::class.java)
                                .putExtra("link", scp.link)
                                .putExtra("title", scp.title)
                                .putExtra("read_type", 1)
                                .putExtra("random_index", index)
                                .putStringArrayListExtra("random_link_list", randomLinkList)
                                .putStringArrayListExtra("random_title_list", randomTitleList)
                        )
                    }
                )
            }
        }
    }
}
