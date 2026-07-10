package info.free.scp.ui.category

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.free.scp.R
import info.free.scp.ui.MainTheme
import info.free.scp.util.ThemeUtil

@Composable
fun CategoryEntryScreen(
    categories: List<String>,
    onOpenCategory: (String, Int) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(categories, key = { index, title -> "$index-$title" }) { index, title ->
            CategoryEntryItem(
                title = title,
                onClick = { onOpenCategory(title, index) }
            )
        }
    }
}

@Composable
private fun CategoryEntryItem(
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(6.dp),
        backgroundColor = Color(ThemeUtil.itemBg),
        elevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(30.dp),
                painter = painterResource(
                    if (ThemeUtil.currentTheme == ThemeUtil.NIGHT_THEME) {
                        R.drawable.baseline_folder_open_gray_24dp
                    } else {
                        R.drawable.baseline_folder_open_black_24dp
                    }
                ),
                tint = Color.Unspecified,
                contentDescription = null
            )
            Spacer(Modifier.width(13.dp))
            Text(
                modifier = Modifier.weight(1f),
                text = title,
                color = Color(ThemeUtil.darkText),
                fontSize = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryEntryScreenPreview() {
    MainTheme {
        CategoryEntryScreen(
            categories = listOf("Russia Branch", "Korea Branch", "France Branch"),
            onOpenCategory = { _, _ -> }
        )
    }
}
