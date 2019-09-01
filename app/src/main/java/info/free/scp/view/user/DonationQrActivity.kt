package info.free.scp.view.user

import android.graphics.drawable.BitmapDrawable
import info.free.scp.R
import info.free.scp.util.Utils
import info.free.scp.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_about_me.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import info.free.scp.util.EventUtil
import info.free.scp.util.PreferenceUtil
import org.jetbrains.anko.*


class DonationQrActivity : BaseActivity() {
    private var payType = 0 // 0 wechat 1 zhi
//    private var ad: InterstitialAD? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventUtil.onEvent(this, EventUtil.clickAboutMe)
        setContentView(R.layout.activity_about_me)

        baseToolbar = about_me_toolbar
        btn_donation_wechat?.setOnClickListener {
            payType = 0
            iv_qr?.setImageResource(R.drawable.img_donation_wechat)
        }
        btn_donation_alipay?.setOnClickListener {
            payType = 1
            iv_qr?.setImageResource(R.drawable.img_donation_alipay)
        }

        iv_qr?.setOnLongClickListener {
            EventUtil.onEvent(this, EventUtil.clickDonation)
            PreferenceUtil.addPoints(2)
            Utils.saveBitmapFile((iv_qr.drawable as BitmapDrawable).bitmap, "scp_donation")
            MediaScannerConnection.scanFile(this, arrayOf(Utils.getAlbumStorageDir("SCP").path + "/scp_donation.jpg"),
                    null, null)
            longToast(R.string.jump_notice)
            Handler().postDelayed({
                if (payType == 0) startWechatScan(this) else openAlipayScan(this)
            }, 500)
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
            toast(getString(R.string.uninstall_notice, getString(R.string.wechat)))
        }
    }

    private fun openAlipayScan(context: Context) {
        try {
            val uri = Uri.parse("alipayqr://platformapi/startapp?saId=10000007")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            if (isActivityAvailable(context, intent)) {
                context.startActivity(intent)
            } else {
                toast(getString(R.string.uninstall_notice, getString(R.string.alipay)))
            }
        } catch (e: Exception) {
        }

    }

    private fun isActivityAvailable(cxt: Context, intent: Intent): Boolean {
        val pm = cxt.packageManager ?: return false
        val list = pm.queryIntentActivities(
                intent, PackageManager.MATCH_DEFAULT_ONLY)
        return list != null && list.size > 0
    }
}
