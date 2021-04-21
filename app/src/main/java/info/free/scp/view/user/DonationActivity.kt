package info.free.scp.view.user

import android.graphics.Color
import android.os.Bundle
import info.free.scp.R
import info.free.scp.util.ThemeUtil
import info.free.scp.util.Utils
import info.free.scp.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_donation.*
import org.jetbrains.anko.dip

class DonationActivity :BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation)

        baseToolbar = donation_toolbar

        btn_afdian?.background = ThemeUtil.customShape(Color.parseColor("#8A62DC"), 0, 0, dip(32))
        btn_mianbaoduo?.background = ThemeUtil.customShape(Color.parseColor("#FED04D"), 0, 0, dip(32))

        btn_afdian?.setOnClickListener {
            startActivity(Utils.getUrlIntent("https://afdian.net/@zhufree"))
        }
        btn_mianbaoduo?.setOnClickListener {
            startActivity(Utils.getUrlIntent("https://mianbaoduo.com/o/author-b2iUl3Bo"))
        }
    }
}
