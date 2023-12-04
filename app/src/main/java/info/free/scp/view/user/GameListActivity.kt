package info.free.scp.view.user

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import eth.zhufree.baihehub.ui.hehuanHong
import eth.zhufree.baihehub.ui.shizhuHong
import info.free.scp.bean.GameModel
import info.free.scp.components.CommonTopBar
import info.free.scp.ui.MainTheme
import info.free.scp.util.EventUtil
import info.free.scp.util.Utils
import info.free.scp.view.base.BaseActivity
import info.free.scp.viewmodel.FireViewModel
import kotlinx.coroutines.launch
/**
 * compose
 */
class GameListActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventUtil.onEvent(this, EventUtil.clickGameList)
        val gameViewModel = FireViewModel()
        gameViewModel.getGames()
        setContent {
            MainTheme {
                GamePage(gameViewModel) {
                    finish()
                }
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun GamePage(gameViewModel: FireViewModel, finish: () -> Unit) {
    Scaffold(topBar = {
        CommonTopBar(title = "游戏列表") {
            finish()
        }
    }) { _ ->
        val tabTitles = listOf("PC", "mobile")
        val tabIcons = listOf(Icons.Default.Computer, Icons.Default.PhoneAndroid)
        val pagerState = rememberPagerState(
            initialPage = 0,
        )
        val scope = rememberCoroutineScope()
        val games by gameViewModel.gameList.observeAsState()
        Column {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                backgroundColor = MaterialTheme.colors.background,
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.scrollToPage(index, 0f)
                            }
                        },
                        text = {
                            Text(
                                text = title,
                                color = if (pagerState.currentPage == index) shizhuHong else hehuanHong
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = tabIcons[index],
                                "icon",
                                tint = if (pagerState.currentPage == index) shizhuHong else hehuanHong
                            )
                        },
                        selectedContentColor = MaterialTheme.colors.primaryVariant
                    )
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxHeight(),
                count = tabTitles.size
            ) { pagePosition ->
                when (pagePosition) {
                    0 -> {
                        games?.let { list ->
                            GamePage(games = list.filter { it.type == "PC" })
                        }
                    }
                    1 -> {
                        games?.let { list ->
                            GamePage(games = list.filter { it.type == "mobile" })
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun GamePage(games: List<GameModel>) {
    Column(modifier = Modifier
        .padding(horizontal = 16.dp)
        .verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(8.dp))
        games.forEach { game ->
            Card(shape = RectangleShape, elevation = 5.dp, onClick = {
                Utils.openUrl(game.link)
            }) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colors.onBackground)
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = game.cover, contentDescription = "game", modifier = Modifier
                            .fillMaxWidth()
                    )
                    Text(text = game.name, fontSize = 20.sp, color = MaterialTheme.colors.onPrimary)
                    Text(text = game.desc, fontSize = 18.sp, color = MaterialTheme.colors.onPrimary)
                    Text(
                        text = "平台：${game.platform}",
                        fontSize = 16.sp,
                        color = MaterialTheme.colors.onPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
