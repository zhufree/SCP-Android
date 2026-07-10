package info.free.scp.ui.draft

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.free.scp.R
import info.free.scp.bean.DraftModel
import info.free.scp.ui.MainTheme
import info.free.scp.util.ThemeUtil

@Composable
fun DraftListScreen(
    drafts: List<DraftModel>,
    onOpenDraft: (DraftModel) -> Unit,
    onDeleteDraft: (DraftModel) -> Unit
) {
    var pendingDelete by remember { mutableStateOf<DraftModel?>(null) }

    if (drafts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                modifier = Modifier.padding(top = 40.dp),
                text = stringResource(R.string.empty_draft),
                color = Color(ThemeUtil.darkText),
                fontSize = 22.sp
            )
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(drafts, key = { it.draftId }) { draft ->
                DraftListItem(
                    draft = draft,
                    onClick = { onOpenDraft(draft) },
                    onLongClick = { pendingDelete = draft }
                )
            }
        }
    }

    pendingDelete?.let { draft ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(text = "删除草稿") },
            text = { Text(text = "确定删除草稿“${draft.title}”吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteDraft(draft)
                        pendingDelete = null
                    }
                ) {
                    Text(text = "确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(text = "取消")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DraftListItem(
    draft: DraftModel,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(6.dp),
        backgroundColor = Color(ThemeUtil.containerBg),
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = draft.title,
                color = Color(ThemeUtil.darkText),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = draft.getLastTimeString(),
                color = Color(ThemeUtil.lightText),
                fontSize = 16.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DraftListScreenPreview() {
    MainTheme {
        DraftListScreen(
            drafts = listOf(
                DraftModel(1, System.currentTimeMillis(), "草稿标题", "正文"),
                DraftModel(2, System.currentTimeMillis(), "另一篇草稿", "正文")
            ),
            onOpenDraft = {},
            onDeleteDraft = {}
        )
    }
}
