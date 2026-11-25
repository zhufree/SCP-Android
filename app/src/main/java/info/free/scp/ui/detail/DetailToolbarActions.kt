package info.free.scp.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Cookie
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import info.free.scp.R
import info.free.scp.ui.common.ScpDropdownMenu
import info.free.scp.ui.common.ScpMenuItem
import info.free.scp.util.ThemeUtil

/**
 * 详情页标题栏右侧：收藏星标 + 溢出菜单（M3 DropdownMenu，位置与旧 Toolbar 菜单一致）。
 * 菜单顺序与旧版相同；分组线把“阅读相关 / 向外分享 / 设置”隔开。标题随状态变化：
 * 日夜项显示切换后的模式，阅读模式项显示切换目标。
 */
@Composable
fun DetailToolbarActions(
    liked: Boolean,
    webMode: Boolean,
    onAction: (DetailMenuAction) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val tint = Color(ThemeUtil.onToolbar)
    val night = ThemeUtil.currentTheme == ThemeUtil.NIGHT_THEME
    fun pick(action: DetailMenuAction) {
        expanded = false
        onAction(action)
    }
    Row {
        IconButton(onClick = { onAction(DetailMenuAction.TOGGLE_LIKE) }) {
            Icon(
                imageVector = if (liked) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = stringResource(R.string.menu_like),
                tint = tint
            )
        }
        Box {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.detail_more_actions),
                    tint = tint
                )
            }
            ScpDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                ScpMenuItem(
                    text = stringResource(if (night) R.string.day_mode else R.string.dark_mode),
                    icon = if (night) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                    onClick = { pick(DetailMenuAction.CHANGE_THEME) }
                )
                ScpMenuItem(
                    text = stringResource(R.string.menu_add_read_later),
                    icon = Icons.Outlined.BookmarkAdd,
                    onClick = { pick(DetailMenuAction.ADD_READ_LATER) }
                )
                ScpMenuItem(
                    text = stringResource(if (webMode) R.string.offline_mode else R.string.online_mode),
                    icon = if (webMode) Icons.Outlined.SaveAlt else Icons.Outlined.Public,
                    onClick = { pick(DetailMenuAction.SWITCH_READ_MODE) }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                ScpMenuItem(
                    text = stringResource(R.string.menu_open_in_browser),
                    icon = Icons.Outlined.OpenInBrowser,
                    onClick = { pick(DetailMenuAction.OPEN_IN_BROWSER) }
                )
                ScpMenuItem(
                    text = stringResource(R.string.menu_copy_link),
                    icon = Icons.Outlined.ContentCopy,
                    onClick = { pick(DetailMenuAction.COPY_LINK) }
                )
                ScpMenuItem(
                    text = stringResource(R.string.share_with_picture),
                    icon = Icons.Outlined.Image,
                    onClick = { pick(DetailMenuAction.SHARE_PICTURE) }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                ScpMenuItem(
                    text = stringResource(R.string.set_cookie),
                    icon = Icons.Outlined.Cookie,
                    onClick = { pick(DetailMenuAction.SET_COOKIE) }
                )
            }
        }
    }
}
