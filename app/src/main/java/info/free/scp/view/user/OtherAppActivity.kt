package info.free.scp.view.user

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import info.free.scp.components.CommonTopBar
import info.free.scp.ui.MainTheme
import info.free.scp.util.Utils
import info.free.scp.view.base.BaseActivity

class OtherAppActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainTheme {
                OtherAppPage { finish() }
            }
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun OtherAppPage(finish: () -> Unit) {
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Open))
    Scaffold(
        scaffoldState = scaffoldState,
        backgroundColor = MaterialTheme.colors.background,
        topBar = {
            CommonTopBar(title = "开发者其他作品") {
                finish()
            }
        },
    ) { _ ->
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            val cd6 = CornerSize(6.dp)
            Card(
                elevation = 2.dp,
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
                    .height(200.dp),
                backgroundColor = MaterialTheme.colors.background,
                shape = RoundedCornerShape(cd6, cd6, cd6, cd6),
            ) {
                Button(
                    onClick = {
                        Utils.openUrl("https://www.bilibili.com/video/BV1m5411971n")
                    }, colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.background
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = "https://ali-fir-pro-icon.firim.ink/819cb77ccd7b4c181679b80b008ae6cdfb57b0d0?auth_key=1694842946-0-0-042f7d246aa5cd2f96cd6c85c5d6e676&tmp=1663306946.4285276",
                            modifier = Modifier
                                .padding(top = 16.dp, bottom = 16.dp)
                                .size(120.dp)
                                .align(Alignment.CenterHorizontally),
                            contentDescription = ""
                        )
                        Text(text = "场所码捷径", fontSize = 18.sp)
                    }
                }
            }
            Card(
                elevation = 2.dp,
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
                    .height(200.dp),
                backgroundColor = MaterialTheme.colors.background,
                shape = RoundedCornerShape(cd6, cd6, cd6, cd6),
            ) {
                Button(
                    onClick = {
                        Utils.openUrl("https://www.coolapk.com/apk/231900")
                    }, colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.background
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = "https://s1.328888.xyz/2022/05/18/Dl4H0.jpg",
                            modifier = Modifier
                                .padding(top = 16.dp, bottom = 16.dp)
                                .size(120.dp)
                                .align(Alignment.CenterHorizontally),
                            contentScale = ContentScale.Fit,
                            contentDescription = ""
                        )
                        Text(text = "批量图片处理工具", fontSize = 18.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
