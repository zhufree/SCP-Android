package info.free.scp.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.bean.FeedModel
import info.free.scp.data.feed.FeedSectionState
import info.free.scp.ui.common.ContainerShape
import info.free.scp.ui.common.SectionTitleRow
import info.free.scp.ui.common.outlinedContainerBorder
import info.free.scp.ui.common.outlinedContainerColors
import info.free.scp.util.ThemeUtil

@Composable
fun HomePageScreen(
    notice: String,
    showNotice: Boolean,
    createState: FeedSectionState,
    translateState: FeedSectionState,
    snackbarHostState: SnackbarHostState,
    onDismissNotice: () -> Unit,
    onOpenGroup: (Int) -> Unit,
    onOpenFeed: () -> Unit,
    onRetryFeed: () -> Unit,
    onOpenTopRated: () -> Unit,
    onOpenTags: () -> Unit,
    onOpenFeedItem: (FeedModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, bottom = 24.dp)
        ) {
            if (showNotice && notice.isNotBlank()) {
                NoticeBanner(
                    notice = notice,
                    onDismiss = onDismissNotice,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            EntryRow(onOpenGroup = onOpenGroup)
            Spacer(modifier = Modifier.height(8.dp))
            FeedSection(
                title = stringResource(R.string.home_section_original),
                tag = stringResource(R.string.home_section_original_tag),
                errorText = stringResource(R.string.home_feed_error_original),
                state = createState,
                onMore = onOpenFeed,
                onRetry = onRetryFeed,
                onOpenFeedItem = onOpenFeedItem
            )
            Spacer(modifier = Modifier.height(8.dp))
            FeedSection(
                title = stringResource(R.string.home_section_translation),
                tag = stringResource(R.string.home_section_translation_tag),
                errorText = stringResource(R.string.home_feed_error_translation),
                state = translateState,
                onMore = onOpenFeed,
                onRetry = onRetryFeed,
                onOpenFeedItem = onOpenFeedItem
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 24.dp, end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BottomEntry(
                    text = stringResource(R.string.top_rated_all),
                    icon = Icons.Outlined.StarOutline,
                    onClick = onOpenTopRated,
                    modifier = Modifier.weight(1f)
                )
                BottomEntry(
                    text = stringResource(R.string.all_tags),
                    icon = Icons.Outlined.Label,
                    onClick = onOpenTags,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        )
    }
}

/** 公告条：默认折叠 2 行，点击展开/收起；关闭键独立在右侧，不压文字。 */
@Composable
private fun NoticeBanner(notice: String, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = ContainerShape,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(modifier = Modifier.padding(start = 12.dp)) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = stringResource(R.string.home_notice_icon),
                modifier = Modifier
                    .padding(top = 14.dp)
                    .size(20.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        onClickLabel = stringResource(
                            if (expanded) R.string.home_notice_collapse else R.string.home_notice_expand
                        )
                    ) { expanded = !expanded }
                    .padding(horizontal = 8.dp, vertical = 12.dp)
                    .animateContentSize()
            ) {
                Text(
                    text = notice,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (expanded) {
                    Text(
                        text = stringResource(R.string.home_notice_collapse),
                        modifier = Modifier.padding(top = 4.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.home_notice_close),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun EntryRow(onOpenGroup: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
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
            darkPlate = true,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 沿用原有位图，不改图本身：黑/灰线条的三枚放在浅色底衬上（日间 primaryContainer、夜间 onPrimaryContainer），
 * 白色线条的放逐者图标放在深绿底衬上（日间 primary、夜间 primaryContainer），日夜都清晰可辨。
 */
@Composable
private fun HomeEntry(
    title: String,
    imageRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    darkPlate: Boolean = false
) {
    val scheme = MaterialTheme.colorScheme
    val night = ThemeUtil.currentTheme == ThemeUtil.NIGHT_THEME
    val plate = when {
        darkPlate && night -> scheme.primaryContainer
        darkPlate -> scheme.primary
        night -> scheme.onPrimaryContainer
        else -> scheme.primaryContainer
    }
    Column(
        modifier = modifier
            .heightIn(min = 80.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(plate)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
        Text(
            text = title,
            modifier = Modifier.padding(top = 8.dp),
            color = scheme.onSurface,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
private fun FeedSection(
    title: String,
    tag: String,
    errorText: String,
    state: FeedSectionState,
    onMore: () -> Unit,
    onRetry: () -> Unit,
    onOpenFeedItem: (FeedModel) -> Unit
) {
    SectionTitleRow(
        title = title,
        tag = tag,
        actionText = stringResource(R.string.home_section_more),
        onAction = onMore
    )
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = ContainerShape,
        colors = outlinedContainerColors(),
        border = outlinedContainerBorder()
    ) {
        when (state) {
            FeedSectionState.Loading -> FeedSkeleton()
            FeedSectionState.Failed -> FeedError(message = errorText, onRetry = onRetry)
            FeedSectionState.Empty -> FeedEmpty()
            is FeedSectionState.Content -> state.items.forEachIndexed { index, feed ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
                FeedArticleRow(feed = feed, onClick = { onOpenFeedItem(feed) })
            }
        }
    }
}

@Composable
private fun FeedArticleRow(feed: FeedModel, onClick: () -> Unit) {
    val titleColor = MaterialTheme.colorScheme.onSurface
    val numberLength = scpNumberLength(feed.title)
    val title = remember(feed.title) {
        buildAnnotatedString {
            if (numberLength > 0) {
                withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                    append(feed.title.substring(0, numberLength))
                }
                append(feed.title.substring(numberLength))
            } else {
                append(feed.title)
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(
            text = title,
            color = titleColor,
            fontSize = 15.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Row(modifier = Modifier.padding(top = 2.dp)) {
            Text(
                text = feed.createdTime,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = feed.rank,
                color = if (isPositiveRank(feed.rank)) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun FeedSkeleton() {
    val transition = rememberInfiniteTransition(label = "feed-skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "feed-skeleton-alpha"
    )
    val block = MaterialTheme.colorScheme.surfaceVariant
    val loading = stringResource(R.string.home_feed_loading)
    Column(modifier = Modifier.semantics { contentDescription = loading }) {
        repeat(3) { index ->
            if (index > 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .alpha(alpha)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (index == 1) 0.55f else 0.75f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(block)
                )
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .width(96.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(block)
                )
            }
        }
    }
}

@Composable
private fun FeedError(message: String, onRetry: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.CloudOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.home_feed_error_title),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
        TextButton(onClick = onRetry) {
            Text(text = stringResource(R.string.home_feed_retry))
        }
    }
}

@Composable
private fun FeedEmpty() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.home_feed_empty),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun BottomEntry(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = ContainerShape,
        colors = outlinedContainerColors(),
        border = outlinedContainerBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomePageScreenPreview() {
    HomePageScreen(
        notice = "Welcome",
        showNotice = true,
        createState = FeedSectionState.Content(
            listOf(FeedModel("SCP-CN-001 test", "/scp-cn-001", "2026-06-23", "+12"))
        ),
        translateState = FeedSectionState.Failed,
        snackbarHostState = SnackbarHostState(),
        onDismissNotice = {},
        onOpenGroup = {},
        onOpenFeed = {},
        onRetryFeed = {},
        onOpenTopRated = {},
        onOpenTags = {},
        onOpenFeedItem = {}
    )
}
