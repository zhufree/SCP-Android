package info.free.scp.ui.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.free.scp.R
import info.free.scp.components.CommonTopBar
import info.free.scp.ui.MainTheme
import info.free.scp.util.ThemeUtil

@Composable
fun AboutAppScreen(
    notice: String,
    versionName: String,
    onBackPressed: () -> Unit,
    onOpenSite: () -> Unit,
    onOpenQqGroup: () -> Unit,
    onOpenQqChannel: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenCopyrightLink: (String) -> Unit
) {
    var showCopyright by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            CommonTopBar(
                title = stringResource(R.string.instructions),
                onBackPressed = onBackPressed
            )
        },
        backgroundColor = Color(ThemeUtil.containerBg)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 32.dp)
        ) {
            PrimaryActionButton(
                text = stringResource(R.string.scp_wiki_site),
                onClick = onOpenSite
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PrimaryActionButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.contact_qq_group),
                    onClick = onOpenQqGroup
                )
                PrimaryActionButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.scp_channel),
                    onClick = onOpenQqChannel
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.user_notice),
                color = Color(ThemeUtil.darkText),
                fontSize = 18.sp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "最新公告：$notice",
                color = Color(ThemeUtil.darkText)
            )
            Spacer(Modifier.height(32.dp))
            SecondaryActionButton(
                text = stringResource(R.string.copyright),
                onClick = { showCopyright = true }
            )
            Spacer(Modifier.height(16.dp))
            SecondaryActionButton(
                text = stringResource(R.string.privacy_agreement),
                onClick = onOpenPrivacy
            )
            Spacer(Modifier.height(36.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "version: $versionName",
                color = Color(ThemeUtil.darkText)
            )
        }
    }

    if (showCopyright) {
        CopyrightDialog(
            onDismiss = { showCopyright = false },
            onOpenLink = onOpenCopyrightLink
        )
    }
}

@Composable
fun DonationScreen(
    onBackPressed: () -> Unit,
    onOpenAfdian: () -> Unit,
    onOpenMianbaoduo: () -> Unit
) {
    Scaffold(
        topBar = {
            CommonTopBar(
                title = stringResource(R.string.donation),
                onBackPressed = onBackPressed
            )
        },
        backgroundColor = Color(ThemeUtil.containerBg)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 64.dp, vertical = 64.dp)
        ) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF8A62DC)),
                onClick = onOpenAfdian,
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.go_afdian),
                    color = Color.White,
                    fontSize = 20.sp
                )
            }
            Spacer(Modifier.height(32.dp))
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFED04D)),
                onClick = onOpenMianbaoduo,
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.go_mianbaoduo),
                    color = Color.White,
                    fontSize = 20.sp
                )
            }
            Spacer(Modifier.height(30.dp))
            Text(
                text = stringResource(R.string.donation_introduction),
                color = Color(0xFFB9B9B9),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun DonationQrScreen(
    selectedPayType: Int,
    onPayTypeSelected: (Int) -> Unit,
    onBackPressed: () -> Unit,
    onDonate: () -> Unit
) {
    val qrRes = if (selectedPayType == 0) {
        R.drawable.img_donation_wechat
    } else {
        R.drawable.img_donation_alipay
    }
    Scaffold(
        topBar = {
            CommonTopBar(
                title = stringResource(R.string.donation),
                onBackPressed = onBackPressed
            )
        },
        backgroundColor = Color(ThemeUtil.containerBg)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 27.dp, vertical = 32.dp)
        ) {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(horizontal = 37.dp),
                painter = painterResource(qrRes),
                contentDescription = stringResource(R.string.app_name)
            )
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QrTypeButton(
                    text = stringResource(R.string.wechat_qr),
                    selected = selectedPayType == 0,
                    onClick = { onPayTypeSelected(0) }
                )
                QrTypeButton(
                    text = stringResource(R.string.alipay_qr),
                    selected = selectedPayType == 1,
                    onClick = { onPayTypeSelected(1) }
                )
            }
            Spacer(Modifier.height(30.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.donation_introduction),
                color = Color(0xFFB9B9B9),
                fontSize = 14.sp
            )
            Spacer(Modifier.height(60.dp))
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF5D6BE8)),
                onClick = onDonate,
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.donation_notice),
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
private fun QrTypeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    TextButton(
        modifier = Modifier.size(width = 96.dp, height = 44.dp),
        onClick = onClick
    ) {
        Text(
            text = text,
            color = if (selected) Color(0xFF666666) else Color(ThemeUtil.darkText),
            fontSize = 16.sp
        )
    }
}

@Composable
private fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = Color(ThemeUtil.linkBlue)),
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        Text(text = text, color = MaterialTheme.colors.onBackground)
    }
}

@Composable
private fun SecondaryActionButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        modifier = Modifier
            .width(150.dp)
            .height(48.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = Color(ThemeUtil.todoYellow)),
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        Text(text = text, fontSize = 18.sp, color = Color(ThemeUtil.darkText))
    }
}

@Composable
private fun CopyrightDialog(
    onDismiss: () -> Unit,
    onOpenLink: (String) -> Unit
) {
    val link1 = "http://scp-wiki-cn.wikidot.com/"
    val link2 = "https://creativecommons.org/licenses/by-sa/3.0/deed.zh"
    val link3 = "http://scp-wiki-cn.wikidot.com/licensing-guide"
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.copyright)) },
        text = {
            Column {
                CopyrightLine(
                    text = stringResource(R.string.copyright_notice_1),
                    url = link1,
                    onOpenLink = onOpenLink
                )
                Spacer(Modifier.height(8.dp))
                CopyrightLine(
                    text = stringResource(R.string.copyright_notice_2),
                    url = link2,
                    onOpenLink = onOpenLink
                )
                Spacer(Modifier.height(8.dp))
                CopyrightLine(
                    text = stringResource(R.string.copyright_notice_3),
                    url = link3,
                    onOpenLink = onOpenLink
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Composable
private fun CopyrightLine(
    text: String,
    url: String,
    onOpenLink: (String) -> Unit
) {
    Column {
        Text(text = text, color = Color(ThemeUtil.darkText))
        TextButton(onClick = { onOpenLink(url) }) {
            Text(url)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AboutAppScreenPreview() {
    MainTheme {
        AboutAppScreen(
            notice = "欢迎来到 SCP 基金会中国分部移动端阅读工具。",
            versionName = "0.3.2",
            onBackPressed = {},
            onOpenSite = {},
            onOpenQqGroup = {},
            onOpenQqChannel = {},
            onOpenPrivacy = {},
            onOpenCopyrightLink = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DonationScreenPreview() {
    MainTheme {
        DonationScreen(
            onBackPressed = {},
            onOpenAfdian = {},
            onOpenMianbaoduo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DonationQrScreenPreview() {
    MainTheme {
        DonationQrScreen(
            selectedPayType = 0,
            onPayTypeSelected = {},
            onBackPressed = {},
            onDonate = {}
        )
    }
}
