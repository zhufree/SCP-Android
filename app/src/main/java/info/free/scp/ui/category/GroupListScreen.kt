package info.free.scp.ui.category

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.free.scp.R
import info.free.scp.ui.MainTheme
import info.free.scp.util.ThemeUtil

@Composable
fun GroupListScreen(
    seriesItems: List<Pair<Int, String>>,
    selectedSeries: Int,
    groupTitle: String,
    groups: List<String>,
    onSelectSeries: (Int) -> Unit,
    onOpenGroup: (String, Int) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .width(105.dp)
                .fillMaxSize()
                .padding(start = 10.dp)
        ) {
            itemsIndexed(seriesItems, key = { _, item -> item.first }) { _, item ->
                SeriesChip(
                    title = item.second,
                    selected = selectedSeries == item.first,
                    onClick = { onSelectSeries(item.first) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .padding(bottom = 24.dp)
        ) {
            item {
                Text(
                    modifier = Modifier.padding(start = 10.dp, top = 36.dp, end = 10.dp),
                    text = groupTitle,
                    color = Color(ThemeUtil.darkText),
                    fontSize = 22.sp
                )
            }
            itemsIndexed(groups, key = { index, title -> "$index-$title" }) { index, title ->
                GroupItem(
                    title = title,
                    onClick = { onOpenGroup(title, index) }
                )
            }
        }
    }
}

@Composable
private fun SeriesChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 36.dp)
            .clickable(onClick = onClick)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (selected) {
                        Modifier
                    } else {
                        Modifier
                    }
                )
                .padding(8.dp),
            text = title,
            color = if (selected) Color.White else Color(ThemeUtil.darkText),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(18.dp),
                    backgroundColor = Color(ThemeUtil.toolbarBg),
                    elevation = 0.dp
                ) {}
                Text(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun GroupItem(
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(start = 10.dp, end = 10.dp, top = 10.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(6.dp),
        backgroundColor = Color(ThemeUtil.itemBg),
        elevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(30.dp),
                painter = painterResource(R.drawable.baseline_folder_open_black_24dp),
                tint = Color(ThemeUtil.darkText),
                contentDescription = null
            )
            Spacer(Modifier.width(12.dp))
            Text(
                modifier = Modifier.weight(1f),
                text = title,
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
private fun GroupListScreenPreview() {
    MainTheme {
        GroupListScreen(
            seriesItems = listOf(1 to "SCP Series", 2 to "SCP Series 2"),
            selectedSeries = 1,
            groupTitle = "SCP Series",
            groups = listOf("SCP Series 0+", "SCP Series 100+"),
            onSelectSeries = {},
            onOpenGroup = { _, _ -> }
        )
    }
}
