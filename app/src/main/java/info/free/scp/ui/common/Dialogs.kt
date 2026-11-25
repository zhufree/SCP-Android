package info.free.scp.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

/*
 * Compose 侧弹窗 / 面板 / 菜单的统一封装（View 侧见 util/ScpDialogs.kt）。
 * 全部是 material3 组件，颜色取 MaterialTheme.colorScheme，日夜由 MainTheme 决定。
 *
 * 选哪个：
 * - 需要用户确认一个决定（含危险操作）→ ScpConfirmDialog；危险操作 destructive = true，确认键用错误色。
 * - 需要用户输入文字 → ScpInputDialog。
 * - 从一列操作里选一个去执行（选项 ≥ 2 个，或标题较长）→ ScpActionSheet（底部面板，拇指好够）。
 * - 锚定在某个按钮旁的短选项（随机类型、文章页更多）→ ScpDropdownMenu + ScpMenuItem。
 */

@Composable
fun ScpConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = if (destructive) {
                    ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                } else {
                    ButtonDefaults.textButtonColors()
                }
            ) {
                Text(text = confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissText)
            }
        }
    )
}

/** 单行输入对话框：M3 描边输入框 + 浮动标签。[allowBlank] 为 false 时空输入会禁用确认键并显示提示。 */
@Composable
fun ScpInputDialog(
    title: String,
    label: String,
    initialValue: String,
    confirmText: String,
    dismissText: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    allowBlank: Boolean = true,
    blankHint: String = "请输入内容"
) {
    var value by remember { mutableStateOf(initialValue) }
    val blank = !allowBlank && value.isBlank()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                onValueChange = { value = it },
                label = { Text(text = label) },
                singleLine = true,
                isError = blank,
                supportingText = if (blank) ({ Text(text = blankHint) }) else null,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(value) }, enabled = !blank) {
                Text(text = confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissText)
            }
        }
    )
}

/** 底部面板里的一项操作。[destructive] 用错误色强调（删除等）。 */
class SheetAction(
    val label: String,
    val onClick: () -> Unit,
    val icon: ImageVector? = null,
    val destructive: Boolean = false
)

/** 点任一项后先关闭面板再执行；点遮罩/下滑只关闭。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScpActionSheet(
    title: String?,
    actions: List<SheetAction>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp)
                )
            }
            actions.forEach { action ->
                val tint = if (action.destructive) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
                ListItem(
                    modifier = Modifier.clickable {
                        onDismiss()
                        action.onClick()
                    },
                    headlineContent = { Text(text = action.label) },
                    leadingContent = action.icon?.let { icon ->
                        {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (action.destructive) tint else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        headlineColor = tint
                    )
                )
            }
            Spacer(Modifier.height(8.dp))
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

/** 12dp 圆角、surfaceContainerHigh 的下拉菜单；锚点由调用方放在外层 Box 里。 */
@Composable
fun ScpDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    content: @Composable () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        offset = offset,
        shape = MaterialTheme.shapes.medium,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        content()
    }
}

/**
 * 菜单项：可带前置图标；[checked] 非 null 时表示可勾选项，已勾选在行尾显示对勾。
 */
@Composable
fun ScpMenuItem(
    text: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    checked: Boolean? = null
) {
    DropdownMenuItem(
        text = { Text(text = text) },
        onClick = onClick,
        leadingIcon = icon?.let { vector -> { Icon(imageVector = vector, contentDescription = null) } },
        trailingIcon = if (checked == true) {
            { Icon(imageVector = Icons.Filled.Check, contentDescription = null) }
        } else {
            null
        },
        colors = MenuDefaults.itemColors()
    )
}
