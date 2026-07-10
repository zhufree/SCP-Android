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
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.OutlinedTextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.free.scp.bean.ScpLikeBox
import info.free.scp.ui.MainTheme
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
        AlertDialog(
            onDismissRequest = { selectedBox = null },
            title = { Text(text = "Notice") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            selectedBox = null
                            clearDeletingBox = box
                        }
                    ) {
                        Text(text = "\u5220\u9664\u6536\u85cf\u5939\u5e76\u6e05\u7a7a\u5176\u4e2d\u6536\u85cf")
                    }
                    TextButton(
                        onClick = {
                            selectedBox = null
                            moveDeletingBox = box
                        }
                    ) {
                        Text(text = "\u5220\u9664\u6536\u85cf\u5939\u5e76\u8f6c\u79fb\u5230\u9ed8\u8ba4\u6536\u85cf\u5939")
                    }
                    TextButton(
                        onClick = {
                            selectedBox = null
                            renameText = box.name
                            renamingBox = box
                        }
                    ) {
                        Text(text = "\u91cd\u547d\u540d\u6536\u85cf\u5939")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedBox = null }) {
                    Text(text = "\u53d6\u6d88")
                }
            }
        )
    }

    clearDeletingBox?.let { box ->
        AlertDialog(
            onDismissRequest = { clearDeletingBox = null },
            title = { Text(text = "Notice") },
            text = { Text(text = "\u786e\u5b9a\u5220\u9664\u8be5\u6536\u85cf\u5939\uff0c\u5e76\u6e05\u7a7a\u5176\u4e2d\u7684\u6536\u85cf\u5185\u5bb9\u5417\uff1f") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteAndClear(box)
                        clearDeletingBox = null
                    }
                ) {
                    Text(text = "\u786e\u5b9a\u5220\u9664")
                }
            },
            dismissButton = {
                TextButton(onClick = { clearDeletingBox = null }) {
                    Text(text = "\u53d6\u6d88")
                }
            }
        )
    }

    moveDeletingBox?.let { box ->
        AlertDialog(
            onDismissRequest = { moveDeletingBox = null },
            title = { Text(text = "Notice") },
            text = { Text(text = "\u786e\u5b9a\u5220\u9664\u8be5\u6536\u85cf\u5939\uff0c\u5e76\u628a\u5185\u5bb9\u8f6c\u79fb\u5230\u9ed8\u8ba4\u6536\u85cf\u5939\u5417\uff1f") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteAndMove(box)
                        moveDeletingBox = null
                    }
                ) {
                    Text(text = "\u786e\u5b9a\u5220\u9664")
                }
            },
            dismissButton = {
                TextButton(onClick = { moveDeletingBox = null }) {
                    Text(text = "\u53d6\u6d88")
                }
            }
        )
    }

    renamingBox?.let { box ->
        AlertDialog(
            onDismissRequest = { renamingBox = null },
            title = { Text(text = "Notice") },
            text = {
                Column {
                    Text(
                        text = "\u8f93\u5165\u65b0\u7684\u6536\u85cf\u5939\u540d",
                        color = Color(ThemeUtil.darkText),
                        fontSize = 18.sp
                    )
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        value = renameText,
                        onValueChange = { renameText = it },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRenameBox(box, renameText)
                        renamingBox = null
                    }
                ) {
                    Text(text = "\u786e\u5b9a")
                }
            },
            dismissButton = {
                TextButton(onClick = { renamingBox = null }) {
                    Text(text = "\u53d6\u6d88")
                }
            }
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
        shape = RoundedCornerShape(6.dp),
        backgroundColor = Color(ThemeUtil.itemBg),
        elevation = 2.dp
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
