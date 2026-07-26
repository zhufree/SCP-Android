package info.free.scp.view.user

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import info.free.scp.R
import info.free.scp.data.settings.InteractionPointsStore
import info.free.scp.data.settings.PreferenceSettingsStore
import info.free.scp.ui.MainTheme
import info.free.scp.ui.user.DonationQrScreen
import info.free.scp.util.EventUtil
import info.free.scp.util.Utils
import info.free.scp.view.base.BaseActivity

class DonationQrActivity : BaseActivity() {
    private val interactionPoints: InteractionPointsStore = PreferenceSettingsStore()
    private var payType by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainTheme {
                DonationQrScreen(
                    selectedPayType = payType,
                    onPayTypeSelected = { payType = it },
                    onBackPressed = { finish() },
                    onDonate = { donate() }
                )
            }
        }
    }

    private fun donate() {
        EventUtil.onEvent(this, EventUtil.clickDonation)
        interactionPoints.increasePoints(2)
        val qrRes = if (payType == 0) {
            R.drawable.img_donation_wechat
        } else {
            R.drawable.img_donation_alipay
        }
        Utils.saveBitmapFile(BitmapFactory.decodeResource(resources, qrRes), "scp_donation")
        Toast.makeText(this, R.string.jump_notice, Toast.LENGTH_LONG).show()
        Handler(Looper.getMainLooper()).postDelayed({
            if (payType == 0) {
                startWechatScan(this)
            } else {
                openAlipayScan(this)
            }
        }, 500)
    }

    @SuppressLint("WrongConstant")
    private fun startWechatScan(c: Context) {
        val intent = Intent()
        intent.component = ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI")
        intent.putExtra("LauncherUI.From.Scaner.Shortcut", true)
        intent.flags = 335544320
        intent.action = "android.intent.action.VIEW"

        if (isActivityAvailable(c, intent)) {
            c.startActivity(intent)
        } else {
            Toast.makeText(this, getString(R.string.uninstall_notice, getString(R.string.wechat)), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAlipayScan(context: Context) {
        try {
            val uri = Uri.parse("alipayqr://platformapi/startapp?saId=10000007")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            if (isActivityAvailable(context, intent)) {
                context.startActivity(intent)
            } else {
                Toast.makeText(this, getString(R.string.uninstall_notice, getString(R.string.alipay)), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.uninstall_notice, getString(R.string.alipay)), Toast.LENGTH_SHORT).show()
        }
    }

    private fun isActivityAvailable(cxt: Context, intent: Intent): Boolean {
        val pm = cxt.packageManager ?: return false
        val list = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return list.isNotEmpty()
    }
}
