package info.free.scp.ui.random

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.free.scp.R
import info.free.scp.bean.ScpItemModel
import info.free.scp.components.CommonTopBar
import info.free.scp.ui.MainTheme
import info.free.scp.util.ThemeUtil

@Composable
fun RandomScreen(
    title: String,
    randomList: List<ScpItemModel>,
    onBackPressed: () -> Unit,
    onRefresh: () -> Unit,
    onAddMore: () -> Unit,
    onOpenRandom: (ScpItemModel, Int) -> Unit
) {
    val listState = rememberLazyListState()
    Scaffold(
        topBar = {
            CommonTopBar(
                title = title,
                onBackPressed = onBackPressed
            )
        },
        backgroundColor = Color(ThemeUtil.containerBg)
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                content = {
                    itemsIndexed(randomList, key = { _, item -> item.link }) { index, item ->
                        RandomListItem(
                            item = item,
                            onClick = { onOpenRandom(item, index) }
                        )
                    }
                }
            )
            if (!listState.isScrollInProgress) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 80.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RandomActionButton(
                        text = "再来一组",
                        onClick = onAddMore
                    )
                    Spacer(Modifier.height(16.dp))
                    RandomActionButton(
                        text = "刷新",
                        onClick = onRefresh
                    )
                }
            }
        }
    }
}

@Composable
private fun RandomListItem(
    item: ScpItemModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(horizontal = 7.dp, vertical = 5.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(6.dp),
        backgroundColor = Color(ThemeUtil.itemBg),
        elevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(R.drawable.outline_note_black_24dp),
                tint = Color(ThemeUtil.darkText),
                contentDescription = null
            )
            Spacer(Modifier.width(13.dp))
            Text(
                text = item.title,
                color = Color(ThemeUtil.darkText),
                fontSize = 18.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RandomActionButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(32.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color(0xFF4A90E2),
            contentColor = Color.White
        ),
        onClick = onClick
    ) {
        Text(text = text, fontSize = 24.sp)
    }
}

@Preview(showBackground = true)
@Composable
private fun RandomScreenPreview() {
    MainTheme {
        RandomScreen(
            title = "随机",
            randomList = listOf(
                ScpItemModel().apply {
                    link = "/scp-173"
                    title = "SCP-173 - The Sculpture"
                },
                ScpItemModel().apply {
                    link = "/scp-682"
                    title = "SCP-682 - Hard-to-Destroy Reptile"
                }
            ),
            onBackPressed = {},
            onRefresh = {},
            onAddMore = {},
            onOpenRandom = { _, _ -> }
        )
    }
}
