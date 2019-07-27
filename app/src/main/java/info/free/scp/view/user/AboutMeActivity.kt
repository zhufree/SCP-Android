package info.free.scp.view.user

import android.Manifest
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
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
import android.os.Handler
import com.soulgame.sgsdk.tgsdklib.TGSDK
import info.free.scp.util.EventUtil
import info.free.scp.util.PreferenceUtil
//import com.qq.e.ads.interstitial.AbstractInterstitialADListener
//import com.qq.e.ads.interstitial.InterstitialAD
//import com.qq.e.comm.util.AdError
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.AD_APP_ID
import info.free.scp.SCPConstants.CUT_VIDEO_AD_ID
import info.free.scp.SCPConstants.STATIC_AD_ID
import info.free.scp.SCPConstants.VIDEO_AD_ID
import org.jetbrains.anko.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions


class AboutMeActivity : BaseActivity(), EasyPermissions.PermissionCallbacks {
    private var payType = 0 // 0 wechat 1 zhi
//    private var ad: InterstitialAD? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventUtil.onEvent(this, EventUtil.clickAboutMe)
        setContentView(R.layout.activity_about_me)

        TGSDK.initialize(this, AD_APP_ID, null)
        about_me_toolbar?.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        about_me_toolbar?.setNavigationOnClickListener { finish() }
        tv_donation_wechat?.setOnClickListener {
            payType = 0
            iv_about_me?.setImageResource(R.drawable.img_donation_wechat)
        }
        tv_donation_alipay?.setOnClickListener {
            payType = 1
            iv_about_me?.setImageResource(R.drawable.img_donation_alipay)
        }

        tv_show_ad?.setOnClickListener {
            requireFilePermission()
        }
        tv_ad_point?.text = "广告积分：${PreferenceUtil.getAdPoint()}"

        iv_about_me?.setOnLongClickListener {
            EventUtil.onEvent(this, EventUtil.clickDonation)
            PreferenceUtil.addPoints(2)
            Utils.saveBitmapFile((iv_about_me.drawable as BitmapDrawable).bitmap, "scp_donation")
            MediaScannerConnection.scanFile(this, arrayOf(Utils.getAlbumStorageDir("SCP").path + "/scp_donation.jpg"),
                    null, null)
            longToast(R.string.jump_notice)
            Handler().postDelayed({
                if (payType == 0) startWechatScan(this) else openAlipayScan(this)
            }, 500)
        }

        tv_show_ad?.setOnClickListener {
            if (System.currentTimeMillis() - PreferenceUtil.getLastShowAdTime() < 1800000) {
                alert("你点击广告太频繁了，请待会再来吧","感谢你的支持") {
                    yesButton {}
                }.show()
                return@setOnClickListener
            }
            if (!PreferenceUtil.getShowAdNotice()) {
                alert("如果你想支持开发者但没有闲钱的话，可以选择看广告。" +
                        "开发者承诺不在本APP内任何地方插入侵入式（强制观看）的广告，" +
                        "仅在用户知情同意的情况下显示广告。","感谢你的支持") {

                    yesButton {
                        PreferenceUtil.setShowAdNotice()
                        showAdSelector()
                    }
                }.show()
                return@setOnClickListener
            }
            showAdSelector()
        }
    }

    private fun showAdSelector() {
        val adList = listOf("图片广告：+1分", "可以跳过的视频广告：+3分", "不可跳过的视频广告：+5分")
        selector("选择你要看的广告类型", adList) { _, i ->
            when (i) {
                0 -> {
                    if (TGSDK.couldShowAd(STATIC_AD_ID)) {
                        PreferenceUtil.addAdPoints(1)
                        TGSDK.showAd(this, STATIC_AD_ID)
                        tv_ad_point?.text = "广告积分：${PreferenceUtil.getAdPoint()}"
                    }
                }
                1 -> {
                    if (TGSDK.couldShowAd(CUT_VIDEO_AD_ID)) {
                        PreferenceUtil.addAdPoints(3)
                        TGSDK.showAd(this, CUT_VIDEO_AD_ID)
                        tv_ad_point?.text = "广告积分：${PreferenceUtil.getAdPoint()}"
                    }
                }
                2 -> {
                    if (TGSDK.couldShowAd(VIDEO_AD_ID)) {
                        PreferenceUtil.addAdPoints(5)
                        TGSDK.showAd(this, VIDEO_AD_ID)
                        tv_ad_point?.text = "广告积分：${PreferenceUtil.getAdPoint()}"

                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        TGSDK.preloadAd(this)
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

    @AfterPermissionGranted(SCPConstants.RequestCode.REQUEST_FILE_PERMISSION)
    private fun requireFilePermission() {
        val perms = arrayOf(Manifest.permission.READ_PHONE_STATE)
        if (EasyPermissions.hasPermissions(this, *perms)) {
//            ad?.loadAD()
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.request_permission_notice),
                    SCPConstants.RequestCode.REQUEST_FILE_PERMISSION, *perms)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
//        ad?.loadAD()
    }
}
