package info.free.scp.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.data.settings.DETAIL_TEXT_SIZES
import info.free.scp.data.update.CatalogVersion
import info.free.scp.data.update.DataUpdateState
import info.free.scp.data.update.DetailPart
import info.free.scp.data.update.MIB
import info.free.scp.ui.common.ContainerShape
import info.free.scp.ui.common.outlinedContainerBorder
import info.free.scp.ui.common.outlinedContainerColors

@Composable
internal fun SettingsScreen(
    hideFinished: Boolean,
    onHideFinishedChange: (Boolean) -> Unit,
    textSize: Int,
    onTextSizeChange: (Int) -> Unit,
    onTextSizeChangeFinished: (Int) -> Unit,
    hanzType: Int,
    onHanzTypeChange: (Int) -> Unit,
    updateState: DataUpdateState,
    onCheckUpdate: () -> Unit,
    wifiOnly: Boolean,
    onWifiOnlyChange: (Boolean) -> Unit,
    onInstallPart: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item { GroupLabel(stringResource(R.string.category_settings)) }
        item {
            GroupCard {
                SwitchRow(
                    title = stringResource(R.string.hide_finished_article),
                    summary = stringResource(R.string.settings_hide_finished_summary),
                    checked = hideFinished,
                    onCheckedChange = onHideFinishedChange
                )
            }
        }
        item { GroupLabel(stringResource(R.string.settings_group_reading)) }
        item {
            GroupCard {
                FontSizeRow(
                    textSize = textSize,
                    onChange = onTextSizeChange,
                    onChangeFinished = onTextSizeChangeFinished
                )
                RowDivider()
                HanzRow(hanzType = hanzType, onChange = onHanzTypeChange)
            }
        }
        item { GroupLabel(stringResource(R.string.settings_group_data_update)) }
        item {
            GroupCard {
                UpdateRow(state = updateState, onCheck = onCheckUpdate)
                RowDivider()
                SwitchRow(
                    title = stringResource(R.string.settings_wifi_only),
                    summary = stringResource(R.string.settings_wifi_only_summary),
                    checked = wifiOnly,
                    onCheckedChange = onWifiOnlyChange
                )
            }
        }
        item {
            GroupLabel(
                text = stringResource(R.string.settings_group_detail_parts),
                trailing = if (updateState.parts.isNotEmpty()) {
                    stringResource(
                        R.string.settings_parts_installed_count,
                        installedPartCount(updateState),
                        updateState.parts.size
                    )
                } else null
            )
        }
        item {
            GroupCard {
                partsBanners(updateState).forEach { banner ->
                    PartsBannerRow(banner)
                    RowDivider()
                }
                if (updateState.parts.isEmpty()) {
                    EmptyParts()
                }
            }
        }
        if (updateState.parts.isNotEmpty()) {
            // 分块列表单独成块，避免 LazyColumn 里一个超高 item
            item {
                GroupCard {
                    updateState.parts.forEachIndexed { index, part ->
                        if (index > 0) RowDivider()
                        PartRow(
                            state = updateState,
                            part = part,
                            onInstall = { onInstallPart(part.id) }
                        )
                    }
                }
            }
        }
        item {
            Text(
                text = stringResource(R.string.settings_parts_source),
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun GroupLabel(text: String, trailing: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        if (trailing != null) {
            Text(
                text = trailing,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun GroupCard(content: @Composable ColumnScope.() -> Unit) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = ContainerShape,
        colors = outlinedContainerColors(),
        border = outlinedContainerBorder(),
        content = content
    )
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun SwitchRow(
    title: String,
    summary: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
            if (summary != null) {
                Text(
                    text = summary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        }
        // 点击由整行 toggleable 处理，Switch 自身不再单独响应
        Switch(
            checked = checked,
            onCheckedChange = null,
            modifier = Modifier.padding(start = 16.dp),
            thumbContent = if (checked) {
                { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(SwitchDefaultsIconSize)) }
            } else null
        )
    }
}

private val SwitchDefaultsIconSize = 16.dp

@Composable
private fun FontSizeRow(textSize: Int, onChange: (Int) -> Unit, onChangeFinished: (Int) -> Unit) {
    val lastIndex = DETAIL_TEXT_SIZES.lastIndex
    val selectedIndex = DETAIL_TEXT_SIZES.indexOf(textSize).coerceAtLeast(0)
    // 有 steps 时 Slider 只会给出档位上的值，所以直接以档位下标作为 value
    val shownIndex = selectedIndex
    // onValueChangeFinished 在重组之前触发（点按时紧接着 onValueChange），不能读取重组后的 textSize，
    // 否则会把上一档写进偏好；这里同步记录最新选择。
    var latestSize by remember { mutableIntStateOf(textSize) }
    Column(modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.font_size_setting),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = stringResource(R.string.settings_font_size_value, DETAIL_TEXT_SIZES[shownIndex]),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Slider(
            value = shownIndex.toFloat(),
            onValueChange = {
                latestSize = DETAIL_TEXT_SIZES[Math.round(it).coerceIn(0, lastIndex)]
                onChange(latestSize)
            },
            onValueChangeFinished = { onChangeFinished(latestSize) },
            valueRange = 0f..lastIndex.toFloat(),
            steps = lastIndex - 1
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DETAIL_TEXT_SIZES.forEachIndexed { index, size ->
                val current = index == shownIndex
                Text(
                    text = size.toString(),
                    color = if (current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (current) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun HanzRow(hanzType: Int, onChange: (Int) -> Unit) {
    val options = listOf(
        SCPConstants.SIMPLE to stringResource(R.string.settings_hanz_simplified),
        SCPConstants.TRADITIONAL to stringResource(R.string.settings_hanz_traditional)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.traditional_setting),
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp
        )
        SingleChoiceSegmentedButtonRow {
            options.forEachIndexed { index, (type, label) ->
                SegmentedButton(
                    selected = hanzType == type,
                    onClick = { onChange(type) },
                    shape = SegmentedButtonDefaults.itemShape(index, options.size)
                ) {
                    Text(text = label, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun UpdateRow(state: DataUpdateState, onCheck: () -> Unit) {
    val mono = FontFamily.Monospace
    val catalogLine = when (val catalog = state.catalog) {
        CatalogVersion.Legacy -> stringResource(R.string.settings_catalog_legacy)
        null -> stringResource(R.string.settings_catalog_unknown)
        else -> stringResource(R.string.settings_catalog_version, catalog.exportId, catalog.exportedAt.take(10))
    }
    val pendingLine = state.stagedCatalog?.let {
        stringResource(R.string.settings_catalog_pending, it.exportId, it.exportedAt.take(10))
    }
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .clickable(enabled = !state.busy, role = Role.Button, onClick = onCheck)
                .padding(start = 16.dp, top = 8.dp, end = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.settings_check_update),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
                Text(
                    text = catalogLine,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontFamily = mono
                )
                if (pendingLine != null) {
                    Text(
                        text = pendingLine,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        fontFamily = mono
                    )
                }
                if (state.status.isNotEmpty()) {
                    Text(
                        text = state.status,
                        color = if (state.failed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }
            Box(modifier = Modifier.padding(start = 12.dp), contentAlignment = Alignment.Center) {
                if (state.busy) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                } else {
                    FilledTonalButton(onClick = onCheck) {
                        Text(
                            text = stringResource(
                                if (state.failed) R.string.settings_check_retry else R.string.settings_check_action
                            )
                        )
                    }
                }
            }
        }
        if (state.busy) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            )
        }
    }
}

@Composable
private fun PartsBannerRow(banner: PartsBanner) {
    val (icon: ImageVector, textRes) = when (banner) {
        PartsBanner.INVENTORY_UNAVAILABLE -> Icons.Outlined.Warning to R.string.settings_parts_banner_inventory_unavailable
        PartsBanner.FULL_IMPORT -> Icons.Outlined.Info to R.string.settings_parts_banner_full_import
        PartsBanner.LEGACY_PENDING -> Icons.Outlined.Info to R.string.settings_parts_banner_legacy_pending
        PartsBanner.REINSTALL -> Icons.Outlined.Warning to R.string.settings_parts_banner_reinstall
        PartsBanner.OBSOLETE -> Icons.Outlined.Warning to R.string.settings_parts_banner_obsolete
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .heightIn(min = 48.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(textRes),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun EmptyParts() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Inventory2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = stringResource(R.string.settings_parts_empty),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun PartRow(state: DataUpdateState, part: DetailPart, onInstall: () -> Unit) {
    val status = partRowStatus(state, part.id)
    val enabled = isPartActionEnabled(state, part.id)
    val statusLabel = when (status) {
        PartRowStatus.UNKNOWN -> stringResource(R.string.settings_part_unknown)
        PartRowStatus.INSTALLED -> stringResource(R.string.settings_part_installed)
        PartRowStatus.UPDATE -> stringResource(R.string.settings_part_update_available)
        PartRowStatus.NOT_INSTALLED -> stringResource(R.string.settings_part_not_installed)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(enabled = enabled, role = Role.Button, onClick = onInstall)
            .padding(start = 16.dp, top = 8.dp, end = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = part.label, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
            Text(
                text = stringResource(R.string.detail_part_summary, part.file.size.toDouble() / MIB, statusLabel),
                color = if (status == PartRowStatus.UPDATE) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Box(modifier = Modifier.padding(start = 12.dp), contentAlignment = Alignment.Center) {
            when (status) {
                PartRowStatus.INSTALLED -> Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = stringResource(R.string.settings_part_installed),
                    modifier = Modifier.padding(8.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                PartRowStatus.UPDATE -> Button(onClick = onInstall, enabled = enabled) {
                    Text(stringResource(R.string.settings_part_action_update))
                }
                else -> FilledTonalButton(onClick = onInstall, enabled = enabled) {
                    Text(stringResource(R.string.settings_part_action_install))
                }
            }
        }
    }
}
