package info.free.scp.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.components.CommonTopBar
import info.free.scp.ui.MainTheme
import info.free.scp.util.ThemeUtil

private data class TopRatedEntry(
    val feedType: Int,
    val titleRes: Int,
    val color: Color
)

private val topRatedEntries = listOf(
    TopRatedEntry(SCPConstants.TOP_RATED_ALL, R.string.top_rated_all, Color(0xFFBA6502)),
    TopRatedEntry(SCPConstants.TOP_RATED_SCP, R.string.top_rated_scp, Color(0xFF0780B7)),
    TopRatedEntry(SCPConstants.TOP_RATED_TALES, R.string.top_rated_tale, Color(0xFF80562F)),
    TopRatedEntry(SCPConstants.TOP_RATED_GOI, R.string.top_rated_goi, Color(0xFF322E2E)),
    TopRatedEntry(SCPConstants.TOP_RATED_WANDERS, R.string.top_rated_wanderer, Color(0xFF136899))
)

@Composable
fun TopRatedMenuScreen(
    showTopBar: Boolean,
    onBackPressed: () -> Unit,
    onOpenFeedType: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            if (showTopBar) {
                CommonTopBar(
                    title = stringResource(R.string.top_rated_all),
                    onBackPressed = onBackPressed
                )
            }
        },
        backgroundColor = Color(ThemeUtil.containerBg)
    ) { padding ->
        TopRatedMenuContent(
            modifier = Modifier.padding(padding),
            contentPadding = if (showTopBar) {
                PaddingValues(start = 7.dp, top = 20.dp, end = 7.dp, bottom = 40.dp)
            } else {
                PaddingValues(32.dp)
            },
            compact = !showTopBar,
            onOpenFeedType = onOpenFeedType
        )
    }
}

@Composable
private fun TopRatedMenuContent(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    compact: Boolean,
    onOpenFeedType: (Int) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(if (compact) 16.dp else 20.dp)
    ) {
        topRatedEntries.forEach { entry ->
            TopRatedEntryCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compact) 112.dp else 140.dp),
                entry = entry,
                compact = compact,
                onClick = { onOpenFeedType(entry.feedType) }
            )
        }
    }
}

@Composable
private fun TopRatedEntryCard(
    modifier: Modifier,
    entry: TopRatedEntry,
    compact: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(if (compact) 5.dp else 6.dp),
        elevation = if (compact) 2.dp else 4.dp,
        backgroundColor = if (compact) Color(ThemeUtil.containerBg) else entry.color
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (compact) Color(ThemeUtil.containerBg) else entry.color)
                .padding(horizontal = if (compact) 8.dp else 30.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(entry.titleRes),
                color = if (compact) Color(ThemeUtil.darkText) else MaterialTheme.colors.onSurface,
                textAlign = TextAlign.Center,
                fontSize = if (compact) 22.sp else 30.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TopRatedMenuScreenPreview() {
    MainTheme {
        TopRatedMenuScreen(
            showTopBar = true,
            onBackPressed = {},
            onOpenFeedType = {}
        )
    }
}
