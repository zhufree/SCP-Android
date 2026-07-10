package info.free.scp.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
import info.free.scp.bean.ScpModel
import info.free.scp.ui.MainTheme
import info.free.scp.util.ThemeUtil

@Composable
fun SearchResultScreen(
    results: List<ScpModel>,
    loading: Boolean,
    needsOfflineData: Boolean,
    onOpenResult: (ScpModel) -> Unit,
    onOpenDownload: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 5.dp)
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(results, key = { it.link }) { result ->
                SearchResultItem(
                    result = result,
                    onClick = { onOpenResult(result) }
                )
            }
        }

        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.Center),
                color = Color(ThemeUtil.accentColor)
            )
        }

        if (needsOfflineData) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "全文搜索需要离线文档",
                    color = Color(ThemeUtil.darkText),
                    fontSize = 16.sp
                )
                Button(
                    modifier = Modifier.padding(top = 8.dp),
                    onClick = onOpenDownload
                ) {
                    Text(text = "离线文档")
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    result: ScpModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(6.dp),
        backgroundColor = Color(ThemeUtil.itemBg),
        elevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(start = 20.dp, top = 15.dp, end = 16.dp, bottom = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(30.dp),
                painter = painterResource(R.drawable.outline_note_black_24dp),
                tint = Color(ThemeUtil.darkText),
                contentDescription = null
            )
            Spacer(Modifier.width(16.dp))
            Text(
                modifier = Modifier.weight(1f),
                text = result.title,
                color = Color(ThemeUtil.darkText),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchResultScreenPreview() {
    MainTheme {
        SearchResultScreen(
            results = listOf(
                ScpModel(title = "SCP-173 - The Sculpture", link = "/scp-173"),
                ScpModel(title = "SCP-682 - Hard-to-Destroy Reptile", link = "/scp-682")
            ),
            loading = false,
            needsOfflineData = false,
            onOpenResult = {},
            onOpenDownload = {}
        )
    }
}
