package info.free.scp.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.free.scp.R
import info.free.scp.util.ThemeUtil

@Composable
fun DownloadScreen(
    onOpenDownload: () -> Unit,
    onSelectDatabase: () -> Unit,
    onOpenSpiderDoc: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(ThemeUtil.containerBg))
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        SectionTitle(text = "\u79bb\u7ebf\u6587\u6863")
        BodyText(text = stringResource(R.string.download_notice))
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DownloadActionButton(
                text = "\u524d\u5f80\u7f51\u9875\u4e0b\u8f7d\u6570\u636e\u5e93",
                onClick = onOpenDownload,
                modifier = Modifier.weight(1f)
            )
            DownloadActionButton(
                text = "\u9009\u62e9\u6570\u636e\u5e93\u6587\u4ef6",
                onClick = onSelectDatabase,
                modifier = Modifier.weight(1f)
            )
        }
        DownloadActionButton(
            text = "\u5982\u4f55\u66f4\u65b0\u6570\u636e\u5e93",
            onClick = onOpenSpiderDoc,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        )
        SectionTitle(text = "\u5907\u4efd\u4e2a\u6027\u5316\u6570\u636e", topPadding = 24)
        BodyText(text = stringResource(R.string.backup_notice))
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DownloadActionButton(
                text = "\u521b\u5efa\u5907\u4efd\u6570\u636e",
                onClick = onBackup,
                modifier = Modifier.weight(1f)
            )
            DownloadActionButton(
                text = "\u8bfb\u53d6\u5907\u4efd\u6570\u636e",
                onClick = onRestore,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String, topPadding: Int = 15) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPadding.dp),
        color = Color(ThemeUtil.darkText),
        fontSize = 24.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun BodyText(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(horizontal = 32.dp, vertical = 10.dp),
        color = Color(ThemeUtil.darkText),
        fontSize = 18.sp,
        lineHeight = 24.sp
    )
}

@Composable
private fun DownloadActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        modifier = modifier.height(48.dp),
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = Color(ThemeUtil.linkBlue))
    ) {
        Text(text = text, color = Color.White, fontSize = 16.sp, textAlign = TextAlign.Center)
    }
}

@Preview(showBackground = true)
@Composable
private fun DownloadScreenPreview() {
    DownloadScreen(
        onOpenDownload = {},
        onSelectDatabase = {},
        onOpenSpiderDoc = {},
        onBackup = {},
        onRestore = {}
    )
}
