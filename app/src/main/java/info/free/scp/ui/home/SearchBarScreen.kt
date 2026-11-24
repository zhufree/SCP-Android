package info.free.scp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.free.scp.util.ThemeUtil

@Composable
fun SearchBarScreen(
    onDirect: () -> Unit,
    onRandom: () -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var keyword by remember { mutableStateOf("") }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(ThemeUtil.itemBg))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Button(
            modifier = Modifier.height(48.dp),
            onClick = onDirect,
        ) {
            Text(text = "\u76f4\u8fbe")
        }
        OutlinedTextField(
            value = keyword,
            onValueChange = { keyword = it },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            placeholder = { Text(text = "\u641c\u7d22") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { onSearch(keyword.trim()) }
            )
        )
        Button(
            modifier = Modifier.height(48.dp),
            onClick = onRandom,
        ) {
            Text(text = "\u968f\u673a")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchBarScreenPreview() {
    SearchBarScreen(onDirect = {}, onRandom = {}, onSearch = {})
}
