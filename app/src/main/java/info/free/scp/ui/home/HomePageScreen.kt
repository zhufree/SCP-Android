package info.free.scp.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.bean.FeedModel
import info.free.scp.util.ThemeUtil

@Composable
fun HomePageScreen(
    notice: String,
    showNotice: Boolean,
    latestCreate: List<FeedModel>,
    latestTranslate: List<FeedModel>,
    onDismissNotice: () -> Unit,
    onOpenGroup: (Int) -> Unit,
    onOpenFeed: () -> Unit,
    onOpenTopRated: () -> Unit,
    onOpenTags: () -> Unit,
    onOpenFeedItem: (FeedModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(ThemeUtil.containerBg))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 7.dp, vertical = 8.dp)
    ) {
        if (showNotice && notice.isNotBlank()) {
            NoticeCard(notice = notice, onDismiss = onDismissNotice)
            Spacer(modifier = Modifier.height(14.dp))
        }
        EntryGrid(onOpenGroup = onOpenGroup)
        Spacer(modifier = Modifier.height(14.dp))
        FeedSection(
            title = "\u6700\u8fd1\u539f\u521b",
            feeds = latestCreate,
            onMore = onOpenFeed,
            onOpenFeedItem = onOpenFeedItem
        )
        Spacer(modifier = Modifier.height(14.dp))
        FeedSection(
            title = "\u6700\u8fd1\u7ffb\u8bd1",
            feeds = latestTranslate,
            onMore = onOpenFeed,
            onOpenFeedItem = onOpenFeedItem
        )
        Spacer(modifier = Modifier.height(14.dp))
        HomeFeatureButton(text = stringResource(R.string.top_rated_all), onClick = onOpenTopRated)
        Spacer(modifier = Modifier.height(14.dp))
        HomeFeatureButton(text = stringResource(R.string.all_tags), onClick = onOpenTags)
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun NoticeCard(notice: String, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(ThemeUtil.itemBg),
        elevation = 2.dp,
        shape = RoundedCornerShape(6.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = notice,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                color = Color(ThemeUtil.darkText)
            )
            IconButton(
                modifier = Modifier.align(Alignment.TopEnd),
                onClick = onDismiss
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    tint = Color(ThemeUtil.mediumText),
                    contentDescription = "Close"
                )
            }
        }
    }
}

@Composable
private fun EntryGrid(onOpenGroup: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(ThemeUtil.itemBg),
        elevation = 1.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 24.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HomeEntry(
                title = stringResource(R.string.entry_scp_series),
                imageRes = R.drawable.scp_logo,
                onClick = { onOpenGroup(SCPConstants.Entry.SCP_DOC) },
                modifier = Modifier.weight(1f)
            )
            HomeEntry(
                title = stringResource(R.string.entry_scp_cn_series),
                imageRes = R.drawable.img_logo_cn,
                onClick = { onOpenGroup(SCPConstants.Entry.SCP_CN_DOC) },
                modifier = Modifier.weight(1f)
            )
            HomeEntry(
                title = stringResource(R.string.entry_scp_story),
                imageRes = R.drawable.img_break_logo,
                onClick = { onOpenGroup(SCPConstants.Entry.STORY_DOC) },
                modifier = Modifier.weight(1f)
            )
            HomeEntry(
                title = stringResource(R.string.entry_wander),
                imageRes = R.drawable.wander_logo,
                onClick = { onOpenGroup(SCPConstants.Entry.WANDER_DOC) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun HomeEntry(
    title: String,
    imageRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color(ThemeUtil.itemBg))
                .padding(5.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = title,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = Color(ThemeUtil.darkText),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun FeedSection(
    title: String,
    feeds: List<FeedModel>,
    onMore: () -> Unit,
    onOpenFeedItem: (FeedModel) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(ThemeUtil.itemBg),
        elevation = 1.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text(
                text = title,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                color = Color(ThemeUtil.darkText),
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium
            )
            feeds.forEach { feed ->
                FeedArticleRow(feed = feed, onClick = { onOpenFeedItem(feed) })
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                onClick = onMore
            ) {
                Text(text = "\u67e5\u770b\u66f4\u591a")
            }
        }
    }
}

@Composable
private fun FeedArticleRow(feed: FeedModel, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(text = feed.title, color = Color(ThemeUtil.darkText), fontSize = 16.sp)
        Row(modifier = Modifier.padding(top = 4.dp)) {
            Text(text = feed.createdTime, color = Color(ThemeUtil.mediumText), fontSize = 12.sp)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = feed.rank, color = Color(ThemeUtil.mediumText), fontSize = 12.sp)
        }
        Divider(
            modifier = Modifier.padding(top = 10.dp),
            color = Color(ThemeUtil.dividerGray)
        )
    }
}

@Composable
private fun HomeFeatureButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colors.primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White, fontSize = 30.sp)
    }
}

@Preview(showBackground = true)
@Composable
private fun HomePageScreenPreview() {
    HomePageScreen(
        notice = "Welcome",
        showNotice = true,
        latestCreate = listOf(FeedModel("SCP-CN-001", "/scp-cn-001", "2026-06-23", "+12")),
        latestTranslate = listOf(FeedModel("SCP-173", "/scp-173", "2026-06-22", "+8")),
        onDismissNotice = {},
        onOpenGroup = {},
        onOpenFeed = {},
        onOpenTopRated = {},
        onOpenTags = {},
        onOpenFeedItem = {}
    )
}
