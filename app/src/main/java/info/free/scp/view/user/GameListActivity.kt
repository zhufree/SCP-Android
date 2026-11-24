package info.free.scp.view.user

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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

@Composable
fun GamePage(gameViewModel: FireViewModel, finish: () -> Unit) {
    Scaffold(topBar = {
        CommonTopBar(title = "游戏列表") {
            finish()
        }
    }) { padding ->
        val tabTitles = listOf("PC", "mobile")
        val tabIcons = listOf(Icons.Default.Computer, Icons.Default.PhoneAndroid)
        val pagerState = rememberPagerState { tabTitles.size }
        val scope = rememberCoroutineScope()
        val games by gameViewModel.gameList.observeAsState()
        Column(modifier = Modifier.padding(padding)) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.scrollToPage(index, 0f)
                            }
                        },
                        text = { Text(text = title) },
                        icon = { Icon(imageVector = tabIcons[index], contentDescription = title) },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxHeight()
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

@Composable
fun GamePage(games: List<GameModel>) {
    Column(modifier = Modifier
        .padding(horizontal = 16.dp)
        .verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(8.dp))
        games.forEach { game ->
            Card(
                onClick = {
                    Utils.openUrl(game.link)
                },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = game.cover, contentDescription = "game", modifier = Modifier
                            .fillMaxWidth()
                    )
                    Text(text = game.name, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = game.desc, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "平台：${game.platform}",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
