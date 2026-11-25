package info.free.scp.ui.user

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.free.scp.R
import info.free.scp.bean.ScpRecordModel
import info.free.scp.ui.common.ContainerShape
import info.free.scp.ui.common.SectionTitleRow
import info.free.scp.ui.common.outlinedContainerBorder
import info.free.scp.ui.common.outlinedContainerColors
import info.free.scp.util.ThemeUtil

@Composable
fun UserDashboardScreen(
    nickname: String,
    userCode: Int,
    jobTitle: String,
    readCount: Int,
    likeCount: Int,
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
            .background(MaterialTheme.colorScheme.background)
    ) {
        UserTopBar(onToggleTheme = onToggleTheme)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp)
        ) {
            ProfileCard(
                nickname = nickname,
                userCode = userCode,
                jobTitle = jobTitle,
                readCount = readCount,
                likeCount = likeCount,
                userHead = userHead,
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
                DonationBanner(onOpenDonation = onOpenDonation)
            }
            SectionTitleRow(
                title = stringResource(R.string.me_history_title),
                tag = stringResource(R.string.me_history_tag),
                modifier = Modifier.padding(top = 8.dp),
                trailing = {
                    IconButton(onClick = onOpenMoreHistory) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = stringResource(R.string.me_history_more),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
            HistoryList(recentHistory = recentHistory, onOpenHistoryItem = onOpenHistoryItem)
        }
    }
}

/** 绿色标题栏：右侧日夜切换键（日间显示月亮，夜间显示太阳）。状态栏区域由 Activity 窗口背景画成同色。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserTopBar(onToggleTheme: () -> Unit) {
    val night = ThemeUtil.currentTheme == ThemeUtil.NIGHT_THEME
    TopAppBar(
        title = { Text(text = stringResource(R.string.me_title)) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(ThemeUtil.toolbarBg),
            titleContentColor = Color(ThemeUtil.onToolbar),
            actionIconContentColor = Color(ThemeUtil.onToolbar)
        ),
        actions = {
            IconButton(onClick = onToggleTheme) {
                Icon(
                    imageVector = if (night) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = stringResource(
                        if (night) R.string.me_theme_to_day else R.string.me_theme_to_night
                    )
                )
            }
        }
    )
}

@Composable
private fun ProfileCard(
    nickname: String,
    userCode: Int,
    jobTitle: String,
    readCount: Int,
    likeCount: Int,
    userHead: Bitmap?,
    onPickUserHead: () -> Unit,
    onEditNickname: () -> Unit,
    onEditJob: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = outlinedContainerColors(),
        border = outlinedContainerBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(scheme.surfaceContainer)
                .height(32.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.me_profile_motto),
                modifier = Modifier.weight(1f),
                color = scheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.2.sp
            )
            Surface(shape = CircleShape, color = scheme.primaryContainer) {
                Text(
                    text = stringResource(R.string.me_profile_badge),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                    color = scheme.onPrimaryContainer,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        HorizontalDivider(color = scheme.outlineVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(userHead = userHead, onClick = onPickUserHead)
            Column(modifier = Modifier.padding(start = 16.dp)) {
                ProfileField(
                    label = stringResource(R.string.me_label_nickname),
                    value = nickname,
                    emphasized = true,
                    onClick = onEditNickname
                )
                ProfileField(
                    label = stringResource(R.string.me_label_code),
                    value = userCode.toString(),
                    mono = true
                )
                ProfileField(
                    label = stringResource(R.string.me_label_job),
                    value = jobTitle,
                    onClick = onEditJob
                )
            }
        }
        HorizontalDivider(color = scheme.outlineVariant)
        Row(modifier = Modifier.height(72.dp)) {
            StatCell(
                count = readCount,
                label = stringResource(R.string.me_stat_read),
                modifier = Modifier.weight(1f)
            )
            VerticalDivider(color = scheme.outlineVariant)
            StatCell(
                count = likeCount,
                label = stringResource(R.string.me_stat_like),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun Avatar(userHead: Bitmap?, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Box(modifier = Modifier.size(76.dp), contentAlignment = Alignment.TopStart) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(16.dp))
                // 默认头像是黑/灰线条的位图：夜间换浅色底衬，保证线条可辨（不改图本身）
                .background(
                    if (ThemeUtil.currentTheme == ThemeUtil.NIGHT_THEME) scheme.onPrimaryContainer
                    else scheme.primaryContainer
                )
                .clickable(
                    onClickLabel = stringResource(R.string.me_avatar_change),
                    role = Role.Button,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            if (userHead != null) {
                Image(
                    bitmap = userHead.asImageBitmap(),
                    contentDescription = stringResource(R.string.me_avatar_change),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.icon_read),
                    contentDescription = stringResource(R.string.me_avatar_change),
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 0.dp, y = 0.dp)
                .size(26.dp),
            shape = CircleShape,
            color = scheme.primary,
            border = androidx.compose.foundation.BorderStroke(2.dp, scheme.surface)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = scheme.onPrimary
                )
            }
        }
    }
}

/** 键 – 值一行。可点击项（代号/职务）≥ 48dp 高并带铅笔；未设置时值显示主色“点击设置”。 */
@Composable
private fun ProfileField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
    mono: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme
    val unset = value.isBlank()
    Row(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(role = Role.Button, onClick = onClick)
                } else {
                    Modifier.heightIn(min = 32.dp)
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(end = 12.dp),
            color = scheme.onSurfaceVariant,
            fontSize = 12.sp
        )
        Text(
            text = if (unset) stringResource(R.string.me_tap_to_set) else value,
            color = if (unset) scheme.primary else scheme.onSurface,
            fontSize = if (emphasized) 18.sp else 15.sp,
            fontWeight = if (emphasized) FontWeight.SemiBold else FontWeight.Normal,
            fontFamily = if (mono) FontFamily.Monospace else null,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        if (onClick != null) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 6.dp)
                    .size(16.dp),
                tint = scheme.primary
            )
        }
    }
}

@Composable
private fun StatCell(count: Int, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = count.toString(),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 24.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
    }
}

private data class DashboardEntry(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

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
    val entries = buildList {
        add(DashboardEntry(stringResource(R.string.me_entry_draft), Icons.Outlined.Edit, onOpenDraft))
        add(DashboardEntry(stringResource(R.string.me_entry_game), Icons.Outlined.SportsEsports, onOpenGame))
        add(DashboardEntry(stringResource(R.string.me_entry_portal), Icons.Outlined.MeetingRoom, onOpenPortal))
        add(DashboardEntry(stringResource(R.string.me_entry_read_settings), Icons.Outlined.Tune, onOpenReadSettings))
        add(DashboardEntry(stringResource(R.string.me_entry_local_data), Icons.Outlined.Download, onOpenLocalData))
        add(DashboardEntry(stringResource(R.string.me_entry_about), Icons.Outlined.Info, onOpenAbout))
        add(DashboardEntry(stringResource(R.string.me_entry_query), Icons.AutoMirrored.Outlined.Assignment, onOpenQuery))
        if (showOtherWork) {
            add(DashboardEntry(stringResource(R.string.me_entry_other_work), Icons.Outlined.Storefront, onOpenOtherWork))
        }
    }
    val rows = entryRowSpans(entries.size)
    val outline = MaterialTheme.colorScheme.outlineVariant
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = ContainerShape,
        colors = outlinedContainerColors(),
        border = outlinedContainerBorder()
    ) {
        var next = 0
        rows.forEachIndexed { rowIndex, spans ->
            if (rowIndex > 0) HorizontalDivider(color = outline)
            Row(modifier = Modifier.height(80.dp)) {
                spans.forEachIndexed { cellIndex, span ->
                    if (cellIndex > 0) VerticalDivider(color = outline)
                    val entry = entries[next++]
                    EntryCell(entry = entry, wide = span > 1, modifier = Modifier.weight(span.toFloat()))
                }
            }
        }
    }
}

@Composable
private fun EntryCell(entry: DashboardEntry, wide: Boolean, modifier: Modifier = Modifier) {
    val content: @Composable () -> Unit = {
        Icon(
            imageVector = entry.icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = entry.title,
            modifier = if (wide) Modifier.padding(start = 12.dp) else Modifier.padding(top = 6.dp),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
    if (wide) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .clickable(role = Role.Button, onClick = entry.onClick),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) { content() }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .clickable(role = Role.Button, onClick = entry.onClick)
                .padding(horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) { content() }
    }
}

/** 保留原有赞助大图，压到 120dp，加半透明遮罩，中间放一个纯色胶囊保证头像和文字清晰。 */
@Composable
private fun DonationBanner(onOpenDonation: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    OutlinedCard(
        onClick = onOpenDonation,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 8.dp),
        shape = ContainerShape,
        colors = outlinedContainerColors(),
        border = outlinedContainerBorder()
    ) {
        Box(modifier = Modifier.height(120.dp), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(R.drawable.bg_donation),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(scheme.scrim.copy(alpha = 0.35f))
            )
            Surface(
                shape = CircleShape,
                color = scheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .heightIn(min = 56.dp)
                        .padding(start = 8.dp, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.author_head),
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = stringResource(R.string.donation),
                        modifier = Modifier.padding(horizontal = 12.dp),
                        color = scheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = scheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryList(recentHistory: List<ScpRecordModel>, onOpenHistoryItem: (ScpRecordModel) -> Unit) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = ContainerShape,
        colors = outlinedContainerColors(),
        border = outlinedContainerBorder()
    ) {
        if (recentHistory.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.me_history_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        } else {
            recentHistory.forEachIndexed { index, history ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
                HistoryRow(history = history, onClick = { onOpenHistoryItem(history) })
            }
        }
    }
}

@Composable
private fun HistoryRow(history: ScpRecordModel, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Description,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(
                text = history.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = history.showTime(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UserDashboardScreenPreview() {
    UserDashboardScreen(
        nickname = "Agent",
        userCode = 173,
        jobTitle = "研究员",
        readCount = 12,
        likeCount = 4,
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
