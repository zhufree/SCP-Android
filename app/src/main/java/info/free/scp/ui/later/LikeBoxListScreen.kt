package info.free.scp.ui.later

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.free.scp.bean.ScpLikeBox
import info.free.scp.ui.MainTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import info.free.scp.ui.common.ScpActionSheet
import info.free.scp.ui.common.ScpConfirmDialog
import info.free.scp.ui.common.ScpInputDialog
import info.free.scp.ui.common.SheetAction
import info.free.scp.util.ThemeUtil

@Composable
fun LikeBoxListScreen(
    likeBoxes: List<ScpLikeBox>,
    onOpenBox: (ScpLikeBox) -> Unit,
    onDeleteAndClear: (ScpLikeBox) -> Unit,
    onDeleteAndMove: (ScpLikeBox) -> Unit,
    onRenameBox: (ScpLikeBox, String) -> Unit
) {
    var selectedBox by remember { mutableStateOf<ScpLikeBox?>(null) }
    var clearDeletingBox by remember { mutableStateOf<ScpLikeBox?>(null) }
    var moveDeletingBox by remember { mutableStateOf<ScpLikeBox?>(null) }
    var renamingBox by remember { mutableStateOf<ScpLikeBox?>(null) }
    var renameText by remember { mutableStateOf("") }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(likeBoxes, key = { it.id }) { box ->
            LikeBoxItem(
                box = box,
                onClick = { onOpenBox(box) },
                onLongClick = {
                    if (box.id != 1) {
                        selectedBox = box
                    }
                }
            )
        }
    }

    selectedBox?.let { box ->
        ScpActionSheet(
            title = box.name,
            actions = listOf(
                SheetAction(
                    label = "\u91cd\u547d\u540d\u6536\u85cf\u5939",
                    icon = Icons.Outlined.Edit,
                    onClick = {
                        renameText = box.name
                        renamingBox = box
                    }
                ),
                SheetAction(
                    label = "\u5220\u9664\u6536\u85cf\u5939\u5e76\u8f6c\u79fb\u5230\u9ed8\u8ba4\u6536\u85cf\u5939",
                    icon = Icons.AutoMirrored.Outlined.DriveFileMove,
                    destructive = true,
                    onClick = { moveDeletingBox = box }
                ),
                SheetAction(
                    label = "\u5220\u9664\u6536\u85cf\u5939\u5e76\u6e05\u7a7a\u5176\u4e2d\u6536\u85cf",
                    icon = Icons.Outlined.Delete,
                    destructive = true,
                    onClick = { clearDeletingBox = box }
                )
            ),
            onDismiss = { selectedBox = null }
        )
    }

    clearDeletingBox?.let { box ->
        ScpConfirmDialog(
            title = "Notice",
            message = "\u786e\u5b9a\u5220\u9664\u8be5\u6536\u85cf\u5939\uff0c\u5e76\u6e05\u7a7a\u5176\u4e2d\u7684\u6536\u85cf\u5185\u5bb9\u5417\uff1f",
            confirmText = "\u786e\u5b9a\u5220\u9664",
            dismissText = "\u53d6\u6d88",
            destructive = true,
            onConfirm = {
                onDeleteAndClear(box)
                clearDeletingBox = null
            },
            onDismiss = { clearDeletingBox = null }
        )
    }

    moveDeletingBox?.let { box ->
        ScpConfirmDialog(
            title = "Notice",
            message = "\u786e\u5b9a\u5220\u9664\u8be5\u6536\u85cf\u5939\uff0c\u5e76\u628a\u5185\u5bb9\u8f6c\u79fb\u5230\u9ed8\u8ba4\u6536\u85cf\u5939\u5417\uff1f",
            confirmText = "\u786e\u5b9a\u5220\u9664",
            dismissText = "\u53d6\u6d88",
            destructive = true,
            onConfirm = {
                onDeleteAndMove(box)
                moveDeletingBox = null
            },
            onDismiss = { moveDeletingBox = null }
        )
    }

    renamingBox?.let { box ->
        ScpInputDialog(
            title = "\u8f93\u5165\u65b0\u7684\u6536\u85cf\u5939\u540d",
            label = "\u6536\u85cf\u5939\u540d\u79f0",
            initialValue = renameText,
            confirmText = "\u786e\u5b9a",
            dismissText = "\u53d6\u6d88",
            allowBlank = false,
            onConfirm = {
                onRenameBox(box, it)
                renamingBox = null
            },
            onDismiss = { renamingBox = null }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LikeBoxItem(
    box: ScpLikeBox,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color(ThemeUtil.itemBg)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            text = box.name,
            color = Color(ThemeUtil.darkText),
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LikeBoxListScreenPreview() {
    MainTheme {
        LikeBoxListScreen(
            likeBoxes = listOf(
                ScpLikeBox(1, "Default"),
                ScpLikeBox(2, "Favorites")
            ),
            onOpenBox = {},
            onDeleteAndClear = {},
            onDeleteAndMove = {},
            onRenameBox = { _, _ -> }
        )
    }
}
