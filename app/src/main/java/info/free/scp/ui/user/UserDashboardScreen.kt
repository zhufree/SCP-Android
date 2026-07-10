package info.free.scp.ui.user

import android.graphics.Bitmap
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
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.free.scp.R
import info.free.scp.bean.ScpRecordModel
import info.free.scp.util.ThemeUtil

@Composable
fun UserDashboardScreen(
    nicknameText: String,
    jobText: String,
    dataText: String,
    userHead: Bitmap?,
    recentHistory: List<ScpRecordModel>,
    showDonation: Boolean,
    showOtherWork: Boolean,
    onToggleTheme: () -> Unit,
    onPickUserHead: () -> Unit,
    onEditNickname: () -> Unit,
    onEditJob: () -> Unit,
    onOpenDraft: () -> Unit,
    onOpenGame: () -> Unit,
    onOpenPortal: () -> Unit,
    onOpenReadSettings: () -> Unit,
    onOpenLocalData: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenQuery: () -> Unit,
    onOpenOtherWork: () -> Unit,
    onOpenDonation: () -> Unit,
    onOpenMoreHistory: () -> Unit,
    onOpenHistoryItem: (ScpRecordModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(ThemeUtil.containerBg))
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        ProfileHeader(
            nicknameText = nicknameText,
            jobText = jobText,
            dataText = dataText,
            userHead = userHead,
            onToggleTheme = onToggleTheme,
            onPickUserHead = onPickUserHead,
            onEditNickname = onEditNickname,
            onEditJob = onEditJob
        )
        EntryGrid(
            showOtherWork = showOtherWork,
            onOpenDraft = onOpenDraft,
            onOpenGame = onOpenGame,
            onOpenPortal = onOpenPortal,
            onOpenReadSettings = onOpenReadSettings,
            onOpenLocalData = onOpenLocalData,
            onOpenAbout = onOpenAbout,
            onOpenQuery = onOpenQuery,
            onOpenOtherWork = onOpenOtherWork
        )
        if (showDonation) {
            DonationCard(onOpenDonation = onOpenDonation)
        }
        HistoryCard(
            recentHistory = recentHistory,
            onOpenMoreHistory = onOpenMoreHistory,
            onOpenHistoryItem = onOpenHistoryItem
        )
    }
}

@Composable
private fun ProfileHeader(
    nicknameText: String,
    jobText: String,
    dataText: String,
    userHead: Bitmap?,
    onToggleTheme: () -> Unit,
    onPickUserHead: () -> Unit,
    onEditNickname: () -> Unit,
    onEditJob: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.slogan),
            modifier = Modifier
                .padding(start = 45.dp)
                .height(64.dp),
            color = Color(ThemeUtil.lightText).copy(alpha = 0.2f),
            fontSize = 20.sp
        )
        IconButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 24.dp, end = 24.dp)
                .size(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colors.primary),
            onClick = onToggleTheme
        ) {
            Icon(
                imageVector = Icons.Default.DarkMode,
                contentDescription = "Theme",
                tint = Color.White
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 60.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onPickUserHead),
                contentAlignment = Alignment.Center
            ) {
                if (userHead != null) {
                    Image(
                        bitmap = userHead.asImageBitmap(),
                        contentDescription = stringResource(R.string.app_name),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.icon_read),
                        contentDescription = stringResource(R.string.app_name),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Text(
                    text = "\u673a\u5bc6\u4eba\u5458",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .background(Color.White.copy(alpha = 0.78f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    color = MaterialTheme.colors.primary,
                    fontSize = 12.sp
                )
            }
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    text = nicknameText,
                    modifier = Modifier.clickable(onClick = onEditNickname),
                    color = Color(ThemeUtil.darkText),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp
                )
                Text(
                    text = jobText,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable(onClick = onEditJob),
                    color = Color(ThemeUtil.mediumText),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Text(
                    text = dataText,
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colors.primary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun EntryGrid(
    showOtherWork: Boolean,
    onOpenDraft: () -> Unit,
    onOpenGame: () -> Unit,
    onOpenPortal: () -> Unit,
    onOpenReadSettings: () -> Unit,
    onOpenLocalData: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenQuery: () -> Unit,
    onOpenOtherWork: () -> Unit
) {
    val items = buildList {
        add(DashboardEntry("\u8349\u7a3f\u7bb1", R.drawable.baseline_border_color_white_24dp, Color(0xFFF26BD9), onOpenDraft))
        add(DashboardEntry("\u6e38\u620f\u5217\u8868", R.drawable.baseline_videogame_asset_white_24dp, Color(0xFF10BED6), onOpenGame))
        add(DashboardEntry("\u4f20\u9001\u95e8", R.drawable.baseline_sensor_door_white_24dp, Color(0xFFF44336), onOpenPortal))
        add(DashboardEntry("\u9605\u8bfb\u8bbe\u7f6e", R.drawable.baseline_settings_white_24dp, Color(0xFFEBA0B3), onOpenReadSettings))
        add(DashboardEntry("\u672c\u5730\u6570\u636e", R.drawable.baseline_download_for_offline_white_24dp, Color(0xFF8076A3), onOpenLocalData))
        add(DashboardEntry("app\u4f7f\u7528\u8bf4\u660e", R.drawable.baseline_info_white_24dp, Color(0xFF346C9C), onOpenAbout))
        add(DashboardEntry("\u7528\u6237\u95ee\u5377", R.drawable.baseline_receipt_long_white_24dp, Color(0xFF346C9C), onOpenQuery))
        if (showOtherWork) {
            add(DashboardEntry("\u5176\u4ed6\u4f5c\u54c1", R.drawable.baseline_food_bank_white_24dp, Color(0xFF2296F3), onOpenOtherWork))
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        backgroundColor = Color(ThemeUtil.itemBg),
        elevation = 2.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            items.chunked(4).forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    row.forEach { item ->
                        DashboardEntryItem(
                            item = item,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(4 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardEntryItem(item: DashboardEntry, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clickable(onClick = item.onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(item.background)
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(item.iconRes),
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize()
            )
        }
        Text(
            text = item.title,
            modifier = Modifier.padding(top = 10.dp),
            color = Color(ThemeUtil.darkText),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DonationCard(onOpenDonation: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 14.dp),
        backgroundColor = Color(ThemeUtil.itemBg),
        elevation = 2.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(modifier = Modifier.height(140.dp)) {
            Image(
                painter = painterResource(R.drawable.bg_donation),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.author_head),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Button(
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .height(30.dp),
                    onClick = onOpenDonation
                ) {
                    Text(text = "\u652f\u6301\u5f00\u53d1\u8005", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(
    recentHistory: List<ScpRecordModel>,
    onOpenMoreHistory: () -> Unit,
    onOpenHistoryItem: (ScpRecordModel) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        backgroundColor = Color(ThemeUtil.itemBg),
        elevation = 1.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 24.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "\u6d4f\u89c8\u5386\u53f2",
                    modifier = Modifier.weight(1f),
                    color = Color(ThemeUtil.darkText),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
                IconButton(onClick = onOpenMoreHistory) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "More",
                        tint = Color(ThemeUtil.mediumText)
                    )
                }
            }
            recentHistory.forEach { history ->
                HistoryRow(history = history, onClick = { onOpenHistoryItem(history) })
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun HistoryRow(history: ScpRecordModel, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 32.dp, top = 16.dp, end = 32.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.outline_note_black_24dp),
            contentDescription = history.title,
            modifier = Modifier.size(30.dp)
        )
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(
                text = history.title,
                color = Color(ThemeUtil.darkText),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = history.showTime(),
                modifier = Modifier.padding(top = 8.dp),
                color = Color(ThemeUtil.mediumText),
                fontSize = 14.sp
            )
        }
    }
}

private data class DashboardEntry(
    val title: String,
    val iconRes: Int,
    val background: Color,
    val onClick: () -> Unit
)

@Preview(showBackground = true)
@Composable
private fun UserDashboardScreenPreview() {
    UserDashboardScreen(
        nicknameText = "\u4ee3\u53f7\uff1aAgent",
        jobText = "\u7f16\u53f7\uff1a173\n\u804c\u52a1\uff1a\u7814\u7a76\u5458",
        dataText = "\u5df2\u9605\u8bfb\u9879\u76ee\uff1a12    \u5df2\u6536\u85cf\u9879\u76ee\uff1a4",
        userHead = null,
        recentHistory = emptyList(),
        showDonation = true,
        showOtherWork = true,
        onToggleTheme = {},
        onPickUserHead = {},
        onEditNickname = {},
        onEditJob = {},
        onOpenDraft = {},
        onOpenGame = {},
        onOpenPortal = {},
        onOpenReadSettings = {},
        onOpenLocalData = {},
        onOpenAbout = {},
        onOpenQuery = {},
        onOpenOtherWork = {},
        onOpenDonation = {},
        onOpenMoreHistory = {},
        onOpenHistoryItem = {}
    )
}
