package info.free.scp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.free.scp.util.ThemeUtil

/**
 * 首页绿色顶栏：搜索胶囊 + 一级 Tab。状态栏区域由 Activity 的窗口背景（ActivityInsets）画成同色，
 * 所以这里不再自己垫状态栏高度，三者连成一块。
 */
@Composable
fun HomeTopBar(
    titles: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onDirect: () -> Unit,
    onRandom: (Int) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val onToolbar = Color(ThemeUtil.onToolbar)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(ThemeUtil.toolbarBg))
    ) {
        SearchBarScreen(
            onDirect = onDirect,
            onRandom = onRandom,
            onSearch = onSearch,
            modifier = Modifier.padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 4.dp)
        )
        PrimaryScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(ThemeUtil.toolbarBg),
            contentColor = onToolbar,
            edgePadding = 8.dp,
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(selectedTab, matchContentSize = true),
                    width = Dp.Unspecified,
                    height = 3.dp,
                    color = onToolbar
                )
            },
            divider = {}
        ) {
            titles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { onTabSelected(index) },
                    selectedContentColor = onToolbar,
                    unselectedContentColor = onToolbar.copy(alpha = 0.72f),
                    text = {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }
    }
}
