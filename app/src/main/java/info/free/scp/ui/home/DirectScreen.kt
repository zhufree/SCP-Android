package info.free.scp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.free.scp.util.ThemeUtil

@Composable
fun DirectScreen(
    expression: String,
    onToggleCn: () -> Unit,
    onToggleJoke: () -> Unit,
    onBackspace: () -> Unit,
    onNumber: (String) -> Unit,
    onGo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(ThemeUtil.containerBg))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 40.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = expression,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            color = Color(ThemeUtil.darkText),
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
        Spacer(modifier = Modifier.height(16.dp))
        DirectKeyRow(
            items = listOf(
                DirectKey("CN", onToggleCn, KeyTone.Neutral),
                DirectKey("J", onToggleJoke, KeyTone.Neutral),
                DirectKey("<", onBackspace, KeyTone.Neutral)
            )
        )
        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9")
        ).forEach { row ->
            DirectKeyRow(
                items = row.map { number ->
                    DirectKey(number, { onNumber(number) }, KeyTone.Number)
                },
                topPadding = 20
            )
        }
        DirectKeyRow(
            items = listOf(
                DirectKey("0", { onNumber("0") }, KeyTone.Number),
                DirectKey("GO", onGo, KeyTone.Go, weight = 2f)
            ),
            topPadding = 20
        )
    }
}

@Composable
private fun DirectKeyRow(items: List<DirectKey>, topPadding: Int = 0) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPadding.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        items.forEach { item ->
            DirectKeyButton(
                label = item.label,
                tone = item.tone,
                modifier = Modifier.weight(item.weight),
                onClick = item.onClick
            )
        }
    }
}

@Composable
private fun DirectKeyButton(
    label: String,
    tone: KeyTone,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor = when (tone) {
        KeyTone.Neutral -> Color(ThemeUtil.disabledBg)
        KeyTone.Number -> Color(ThemeUtil.accentColor)
        KeyTone.Go -> MaterialTheme.colors.primary
    }
    val textColor = when (tone) {
        KeyTone.Neutral -> Color(ThemeUtil.lightText)
        KeyTone.Number, KeyTone.Go -> MaterialTheme.colors.onSurface
    }
    Box(
        modifier = modifier
            .height(80.dp)
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 34.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private data class DirectKey(
    val label: String,
    val onClick: () -> Unit,
    val tone: KeyTone,
    val weight: Float = 1f
)

private enum class KeyTone {
    Neutral,
    Number,
    Go
}

@Preview(showBackground = true)
@Composable
private fun DirectScreenPreview() {
    DirectScreen(
        expression = "SCP-CN-173-J",
        onToggleCn = {},
        onToggleJoke = {},
        onBackspace = {},
        onNumber = {},
        onGo = {}
    )
}
