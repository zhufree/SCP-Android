package info.free.scp.ui.category

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.free.scp.R
import info.free.scp.bean.ScpModel
import info.free.scp.ui.MainTheme
import info.free.scp.util.ThemeUtil

data class ScpDocumentStatus(
    val isLiked: Boolean,
    val hasRead: Boolean,
    val isInLater: Boolean
)

@Composable
fun ScpDocumentListScreen(
    documents: List<ScpModel>,
    scrollToTopSignal: Int,
    statusProvider: (ScpModel) -> ScpDocumentStatus,
    onOpenDocument: (ScpModel, Int) -> Unit,
    onToggleRead: (ScpModel) -> Unit,
    onToggleLater: (ScpModel) -> Unit
) {
    val listState = rememberLazyListState()
    var selectedDocument by remember { mutableStateOf<ScpModel?>(null) }
    var refreshSignal by remember { mutableIntStateOf(0) }

    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > 0) {
            listState.animateScrollToItem(0)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(documents, key = { _, item -> item.link }) { index, document ->
            val status = statusProvider(document)
            ScpDocumentListItem(
                document = document,
                status = status,
                refreshSignal = refreshSignal,
                onOpen = { onOpenDocument(document, index) },
                onMore = { selectedDocument = document }
            )
        }
    }

    selectedDocument?.let { document ->
        val status = statusProvider(document)
        ScpDocumentActionDialog(
            document = document,
            status = status,
            onDismiss = { selectedDocument = null },
            onToggleRead = {
                onToggleRead(document)
                refreshSignal += 1
            },
            onToggleLater = {
                onToggleLater(document)
                refreshSignal += 1
            }
        )
    }
}

@Composable
private fun ScpDocumentListItem(
    document: ScpModel,
    status: ScpDocumentStatus,
    refreshSignal: Int,
    onOpen: () -> Unit,
    onMore: () -> Unit
) {
    val iconRes = when {
        status.isLiked -> R.drawable.ic_star_yellow_900_24dp
        status.hasRead -> R.drawable.baseline_fact_check_black_24dp
        else -> R.drawable.baseline_article_black_24dp
    }
    val titleColor = if (status.hasRead && !status.isLiked) {
        Color(ThemeUtil.mediumText)
    } else {
        Color(ThemeUtil.darkText)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(6.dp),
        backgroundColor = Color(ThemeUtil.itemBg),
        elevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(30.dp),
                painter = painterResource(iconRes),
                tint = Color.Unspecified,
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Text(
                modifier = Modifier.weight(1f),
                text = document.title,
                color = titleColor,
                fontSize = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(onClick = onMore) {
                Icon(
                    painter = painterResource(R.drawable.baseline_more_vert_grey_700_24dp),
                    tint = Color(ThemeUtil.mediumText),
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun ScpDocumentActionDialog(
    document: ScpModel,
    status: ScpDocumentStatus,
    onDismiss: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleLater: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = document.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        text = {
            Column {
                TextButton(onClick = onToggleLater) {
                    Text(text = if (status.isInLater) "取消待读" else "添加待读")
                }
                TextButton(onClick = onToggleRead) {
                    Text(text = if (status.hasRead) "取消已读" else "标记已读")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "关闭")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun ScpDocumentListScreenPreview() {
    MainTheme {
        ScpDocumentListScreen(
            documents = listOf(
                ScpModel(title = "SCP-173 - The Sculpture", link = "/scp-173"),
                ScpModel(title = "SCP-682 - Hard-to-Destroy Reptile", link = "/scp-682")
            ),
            scrollToTopSignal = 0,
            statusProvider = { ScpDocumentStatus(isLiked = false, hasRead = false, isInLater = false) },
            onOpenDocument = { _, _ -> },
            onToggleRead = {},
            onToggleLater = {}
        )
    }
}
