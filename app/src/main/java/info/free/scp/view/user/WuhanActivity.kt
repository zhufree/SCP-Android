package info.free.scp.view.user

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.bumptech.glide.Glide
import info.free.scp.R
import info.free.scp.util.EventUtil
import info.free.scp.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_wuhan.*

class WuhanActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventUtil.onEvent(this, EventUtil.clickWuhan)
        setContentView(R.layout.activity_wuhan)

        baseToolbar = wuhan_toolbar
        Glide.with(this).load("https://assets.dxycdn.com/gitrepo/bbs-mobile/dist/static/kv.5d3a3a1d.png")
                .into(iv_wuhan_map)
        Glide.with(this).load("https://img1.dxycdn.com/2020/0127/631/3393279815372646967-73.jpg")
                .into(iv_wuhan_donate)

        cv_wuhan_map.setOnClickListener {
            EventUtil.onEvent(this, EventUtil.clickMap)
            gotoWeb("https://3g.dxy.cn/newh5/view/pneumonia")
        }
        cv_wuhan_donate.setOnClickListener {
            EventUtil.onEvent(this, EventUtil.clickDonateWuhan)
            gotoWeb("https://shimo.im/docs/wDtQpGCgghWkgVxD/read")
        }
    }

    private fun gotoWeb(link: String) {
        val updateIntent = Intent()
        updateIntent.action = "android.intent.action.VIEW"
        val updateUrl = Uri.parse(link)
        updateIntent.data = updateUrl
        startActivity(updateIntent)
    }
}
