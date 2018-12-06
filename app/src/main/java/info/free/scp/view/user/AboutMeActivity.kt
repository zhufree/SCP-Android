package info.free.scp.view.user

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import info.free.scp.R
import info.free.scp.util.Utils
import info.free.scp.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_about_me.*
import android.widget.Toast
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.os.Handler
import info.free.scp.util.EventUtil
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.Toaster


class AboutMeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventUtil.onEvent(this, EventUtil.clickAboutMe)
        setContentView(R.layout.activity_about_me)

        tv_donation?.setOnClickListener {
            EventUtil.onEvent(this, EventUtil.clickDonation)
            PreferenceUtil.addPoints(2)
            Utils.saveBitmapFile((resources.getDrawable(R.drawable.img_donation) as BitmapDrawable)
                    .bitmap, "scp_donation")
            MediaScannerConnection.scanFile(this, arrayOf(Utils.getAlbumStorageDir("SCP").path + "/scp_donation.jpg"),
                    null, null)
            Toaster.showLong("正在跳转到微信扫一扫，请从相册选取赞赏二维码随意打赏", context = this)
            Handler().postDelayed({startWechatScan(this)}, 500)
        }
    }

    private fun startWechatScan(c: Context) {
        val intent = Intent()
        intent.component = ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI")
        intent.putExtra("LauncherUI.From.Scaner.Shortcut", true)
        intent.flags = 335544320
        intent.action = "android.intent.action.VIEW"

        if (isActivityAvailable(c, intent)) {
            c.startActivity(intent)
        } else {
            Toast.makeText(c, "检测到未安装微信无法打赏，但还是感谢支持", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isActivityAvailable(cxt: Context, intent: Intent): Boolean {
        val pm = cxt.packageManager ?: return false
        val list = pm.queryIntentActivities(
                intent, PackageManager.MATCH_DEFAULT_ONLY)
        return list != null && list.size > 0
    }

}
