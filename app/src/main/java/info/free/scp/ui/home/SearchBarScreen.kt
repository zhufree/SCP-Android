package info.free.scp.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Dialpad
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SentimentSatisfied
import info.free.scp.ui.common.ScpDropdownMenu
import info.free.scp.ui.common.ScpMenuItem
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.free.scp.R

private val randomTypeIcons = listOf(
    Icons.Outlined.Casino,
    Icons.Outlined.Description,
    Icons.Outlined.AutoStories,
    Icons.Outlined.SentimentSatisfied
)

/** 随机类型下标，顺序与 RandomActivity 的 `random_type` 一致。 */
internal val randomTypeLabels = listOf(
    R.string.random_all,
    R.string.random_scp,
    R.string.random_tale,
    R.string.random_joke
)

/** 胶囊搜索框：搜索图标 + 输入 + 右侧“编号直达”“随机”两个 48dp 图标按钮。 */
@Composable
fun SearchBarScreen(
    onDirect: () -> Unit,
    onRandom: (Int) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var keyword by rememberSaveable { mutableStateOf("") }
    var randomMenuOpen by remember { mutableStateOf(false) }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            BasicTextField(
                value = keyword,
                onValueChange = { keyword = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch(keyword.trim()) }),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (keyword.isEmpty()) {
                            Text(
                                text = stringResource(R.string.home_search_hint),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        inner()
                    }
                }
            )
            if (keyword.isNotEmpty()) {
                IconButton(onClick = { keyword = "" }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.home_search_clear),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            VerticalDivider(
                modifier = Modifier.height(24.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            TooltipIconButton(
                label = stringResource(R.string.home_direct),
                icon = Icons.Outlined.Dialpad,
                onClick = onDirect
            )
            Box {
                TooltipIconButton(
                    label = stringResource(R.string.home_random),
                    icon = Icons.Outlined.Shuffle,
                    onClick = { randomMenuOpen = true }
                )
                ScpDropdownMenu(
                    expanded = randomMenuOpen,
                    onDismissRequest = { randomMenuOpen = false }
                ) {
                    randomTypeLabels.forEachIndexed { index, labelRes ->
                        ScpMenuItem(
                            text = stringResource(labelRes),
                            icon = randomTypeIcons[index],
                            onClick = {
                                randomMenuOpen = false
                                onRandom(index)
                            }
                        )
                    }
                }
            }
        }
    }
}

/** 48dp 图标按钮，长按显示文字提示，同时作为 contentDescription。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TooltipIconButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
        tooltip = { PlainTooltip { Text(label) } },
        state = rememberTooltipState()
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchBarScreenPreview() {
    SearchBarScreen(onDirect = {}, onRandom = {}, onSearch = {})
}
