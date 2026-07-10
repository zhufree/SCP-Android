package info.free.scp.ui.later

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.free.scp.R
import info.free.scp.bean.ScpLikeBox
import info.free.scp.bean.ScpLikeModel
import info.free.scp.ui.MainTheme
import info.free.scp.util.ThemeUtil

@Composable
fun LikeListScreen(
    likes: List<ScpLikeModel>,
    likeBoxes: List<ScpLikeBox>,
    onOpenLike: (ScpLikeModel) -> Unit,
    onDeleteLike: (ScpLikeModel) -> Unit,
    onMoveLike: (ScpLikeModel, ScpLikeBox) -> Unit
) {
    var selectedLike by remember { mutableStateOf<ScpLikeModel?>(null) }
    var movingLike by remember { mutableStateOf<ScpLikeModel?>(null) }
    var deletingLike by remember { mutableStateOf<ScpLikeModel?>(null) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(likes, key = { _, item -> item.link }) { _, like ->
            LikeListItem(
                like = like,
                onClick = { onOpenLike(like) },
                onLongClick = { selectedLike = like }
            )
        }
    }

    selectedLike?.let { like ->
        AlertDialog(
            onDismissRequest = { selectedLike = null },
            title = { Text(text = "收藏操作") },
            text = { Text(text = like.title) },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedLike = null
                        movingLike = like
                    }
                ) {
                    Text(text = "移动")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        selectedLike = null
                        deletingLike = like
                    }
                ) {
                    Text(text = "删除")
                }
            }
        )
    }

    deletingLike?.let { like ->
        AlertDialog(
            onDismissRequest = { deletingLike = null },
            title = { Text(text = "删除收藏") },
            text = { Text(text = "确定删除“${like.title}”吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteLike(like)
                        deletingLike = null
                    }
                ) {
                    Text(text = "确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingLike = null }) {
                    Text(text = "取消")
                }
            }
        )
    }

    movingLike?.let { like ->
        AlertDialog(
            onDismissRequest = { movingLike = null },
            title = { Text(text = "选择收藏夹") },
            text = {
                androidx.compose.foundation.layout.Column {
                    likeBoxes.forEach { box ->
                        TextButton(
                            onClick = {
                                onMoveLike(like, box)
                                movingLike = null
                            }
                        ) {
                            Text(text = box.name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { movingLike = null }) {
                    Text(text = "取消")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LikeListItem(
    like: ScpLikeModel,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(6.dp),
        backgroundColor = Color(ThemeUtil.itemBg),
        elevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(30.dp),
                painter = painterResource(R.drawable.outline_note_black_24dp),
                tint = Color(ThemeUtil.darkText),
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = like.title,
                color = Color(ThemeUtil.darkText),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LikeListScreenPreview() {
    MainTheme {
        LikeListScreen(
            likes = listOf(
                ScpLikeModel("/scp-173", "SCP-173", like = true, hasRead = false),
                ScpLikeModel("/scp-682", "SCP-682", like = true, hasRead = true)
            ),
            likeBoxes = listOf(ScpLikeBox(1, "默认收藏夹")),
            onOpenLike = {},
            onDeleteLike = {},
            onMoveLike = { _, _ -> }
        )
    }
}
