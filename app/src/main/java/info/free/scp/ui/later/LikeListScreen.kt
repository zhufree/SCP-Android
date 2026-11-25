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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FolderOpen
import info.free.scp.ui.common.ScpActionSheet
import info.free.scp.ui.common.ScpConfirmDialog
import info.free.scp.ui.common.SheetAction
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
        ScpActionSheet(
            title = like.title,
            actions = listOf(
                SheetAction(
                    label = "移动",
                    icon = Icons.AutoMirrored.Outlined.DriveFileMove,
                    onClick = { movingLike = like }
                ),
                SheetAction(
                    label = "删除",
                    icon = Icons.Outlined.Delete,
                    destructive = true,
                    onClick = { deletingLike = like }
                )
            ),
            onDismiss = { selectedLike = null }
        )
    }

    deletingLike?.let { like ->
        ScpConfirmDialog(
            title = "删除收藏",
            message = "确定删除“${like.title}”吗？",
            confirmText = "确定",
            dismissText = "取消",
            destructive = true,
            onConfirm = {
                onDeleteLike(like)
                deletingLike = null
            },
            onDismiss = { deletingLike = null }
        )
    }

    movingLike?.let { like ->
        ScpActionSheet(
            title = "选择收藏夹",
            actions = likeBoxes.map { box ->
                SheetAction(
                    label = box.name,
                    icon = Icons.Outlined.FolderOpen,
                    onClick = { onMoveLike(like, box) }
                )
            },
            onDismiss = { movingLike = null }
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
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color(ThemeUtil.itemBg)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
