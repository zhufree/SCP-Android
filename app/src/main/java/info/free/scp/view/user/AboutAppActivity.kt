package info.free.scp.view.user

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import info.free.scp.BuildConfig
import info.free.scp.ui.MainTheme
import info.free.scp.ui.user.AboutAppScreen
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.Utils
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.home.PrivacyActivity

class AboutAppActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainTheme {
                AboutAppScreen(
                    notice = PreferenceUtil.getNotice(),
                    versionName = BuildConfig.VERSION_NAME,
                    onBackPressed = { finish() },
                    onOpenSite = { Utils.openUrl("http://scp-wiki-cn.wikidot.com/") },
                    onOpenQqGroup = { Utils.openUrl("https://jq.qq.com/?_wv=1027&k=7wWIlcxZ") },
                    onOpenQqChannel = { Utils.openUrl("https://pd.qq.com/s/4tqu31") },
                    onOpenPrivacy = { startActivity(Intent(this, PrivacyActivity::class.java)) },
                    onOpenCopyrightLink = { url -> startActivity(Utils.getUrlIntent(url)) }
                )
            }
        }
    }
}
