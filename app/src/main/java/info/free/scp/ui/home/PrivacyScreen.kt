package info.free.scp.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.free.scp.ui.MainTheme
import info.free.scp.util.ThemeUtil

@Composable
fun PrivacyScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "\u0053\u0043\u0050\u57fa\u91d1\u4f1a\u0041\u0050\u0050\u9690\u79c1\u534f\u8bae",
            color = Color(0xFF111111),
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            text = "\u672c\u0041\u0050\u0050\u4f7f\u7528\u7b2c\u4e09\u65b9\u6570\u636e\u76d1\u6d4b\u0053\u0044\u004b\u7528\u4e8e\u76d1\u6d4b\u4f7f\u7528\u4eba\u6570\u53d8\u5316\uff0c\u95ea\u9000\u65f6\u7684\u0042\u0075\u0067\u62a5\u544a\u7b49\u3002\u8be5\u7b2c\u4e09\u65b9\u0053\u0044\u004b\u53ef\u80fd\u4f1a\u6536\u96c6\u7528\u6237\u7684\u624b\u673a\u578b\u53f7\u3001\u5730\u7406\u4f4d\u7f6e\u7b49\u4fe1\u606f\u3002\u9664\u6b64\u4e4b\u5916\uff0c\u672c\u0041\u0050\u0050\u4e0d\u6536\u96c6\u4efb\u4f55\u7528\u6237\u9690\u79c1\u4fe1\u606f\uff0c\u7528\u6237\u4e0a\u4f20\u7684\u5934\u50cf\u3001\u8bbe\u7f6e\u7684\u540d\u79f0\u4ec5\u4e3a\u672c\u5730\u4fdd\u5b58\u53ca\u663e\u793a\u3002",
            color = Color(0xFF333333),
            fontSize = 18.sp,
            lineHeight = 28.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PrivacyScreenPreview() {
    MainTheme {
        PrivacyScreen()
    }
}
