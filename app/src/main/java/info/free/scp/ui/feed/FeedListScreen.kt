package info.free.scp.ui.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.free.scp.R
import info.free.scp.bean.FeedModel
import info.free.scp.components.CommonTopBar
import info.free.scp.ui.MainTheme
import info.free.scp.util.ThemeUtil

@Composable
fun LatestFeedScreen(
    createdFeedList: List<FeedModel>,
    translatedFeedList: List<FeedModel>,
    onBackPressed: () -> Unit,
    onOpenFeed: (FeedModel) -> Unit
) {
    var selectedTab by rememberSaveable { androidx.compose.runtime.mutableIntStateOf(0) }
    val tabs = listOf("最近原创", "最近翻译")
    Scaffold(
        topBar = {
            CommonTopBar(
                title = "最近更新",
                onBackPressed = onBackPressed
            )
        },
        backgroundColor = Color(ThemeUtil.containerBg)
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                backgroundColor = Color(ThemeUtil.toolbarBg),
                contentColor = Color.White
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(text = title) }
                    )
                }
            }
            FeedListScreen(
                feedList = if (selectedTab == 0) createdFeedList else translatedFeedList,
                onOpenFeed = onOpenFeed
            )
        }
    }
}

@Composable
fun TopRatedFeedScreen(
    title: String,
    feedList: List<FeedModel>,
    onBackPressed: () -> Unit,
    onOpenFeed: (FeedModel) -> Unit
) {
    Scaffold(
        topBar = {
            CommonTopBar(
                title = title,
                onBackPressed = onBackPressed
            )
        },
        backgroundColor = Color(ThemeUtil.containerBg)
    ) { padding ->
        FeedListScreen(
            modifier = Modifier.padding(padding),
            feedList = feedList,
            onOpenFeed = onOpenFeed
        )
    }
}

@Composable
fun FeedListScreen(
    modifier: Modifier = Modifier,
    feedList: List<FeedModel>,
    onOpenFeed: (FeedModel) -> Unit
) {
    if (feedList.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Loading...", color = Color(ThemeUtil.mediumText))
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        content = {
            items(feedList, key = { it.link }) { feed ->
                FeedListItem(
                    feed = feed,
                    onClick = { onOpenFeed(feed) }
                )
            }
        }
    )
}

@Composable
private fun FeedListItem(
    feed: FeedModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color(ThemeUtil.itemBg),
        elevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(R.drawable.baseline_article_black_24dp),
                    tint = Color(ThemeUtil.darkText),
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    modifier = Modifier.weight(1f),
                    text = feed.title,
                    color = Color(ThemeUtil.darkText),
                    fontSize = 18.sp,
                    maxLines = 2
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = feed.rank,
                    color = Color(ThemeUtil.darkText),
                    fontSize = 16.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = feed.createdTime,
                color = Color(ThemeUtil.darkText),
                fontSize = 12.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FeedListScreenPreview() {
    MainTheme {
        FeedListScreen(
            feedList = listOf(
                FeedModel(
                    title = "SCP-173 - The Sculpture",
                    link = "/scp-173",
                    createdTime = "2026-06-23",
                    rank = "+173"
                ),
                FeedModel(
                    title = "A longer feed item title that wraps across two lines cleanly",
                    link = "/example",
                    createdTime = "2026-06-22",
                    rank = "+42"
                )
            ),
            onOpenFeed = {}
        )
    }
}
