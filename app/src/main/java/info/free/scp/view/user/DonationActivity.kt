package info.free.scp.view.user

import android.os.Bundle
import androidx.activity.compose.setContent
import info.free.scp.ui.MainTheme
import info.free.scp.ui.user.DonationScreen
import info.free.scp.util.Utils
import info.free.scp.view.base.BaseActivity

class DonationActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainTheme {
                DonationScreen(
                    onBackPressed = { finish() },
                    onOpenAfdian = { startActivity(Utils.getUrlIntent("https://afdian.net/@zhufree")) },
                    onOpenMianbaoduo = { startActivity(Utils.getUrlIntent("https://mbd.pub/o/author-b2iUl3Bo")) }
                )
            }
        }
    }
}
