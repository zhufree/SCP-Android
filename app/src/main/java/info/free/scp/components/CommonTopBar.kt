package info.free.scp.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import info.free.scp.util.ThemeUtil.toolbarBg

@Composable
fun CommonTopBar(
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
    onBackPressed: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(text = title, color = MaterialTheme.colors.onBackground)
        },
        backgroundColor = Color(toolbarBg),
        navigationIcon = {
            IconButton(
                onClick = {
                    onBackPressed()
                }
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colors.surface
                )
            }
        },
        actions = { actions() }
    )
}