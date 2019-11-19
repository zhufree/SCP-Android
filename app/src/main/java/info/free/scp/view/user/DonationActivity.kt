package info.free.scp.view.user

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import info.free.scp.R
import info.free.scp.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_donation.*

class DonationActivity :BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation)

        baseToolbar = donation_toolbar

        btn_afdian?.setOnClickListener {
            val updateIntent = Intent()
            updateIntent.action = "android.intent.action.VIEW"
            val updateUrl = Uri.parse("https://afdian.net/@zhufree")
            updateIntent.data = updateUrl
            startActivity(updateIntent)
        }
        btn_chongya?.setOnClickListener {
            val updateIntent = Intent()
            updateIntent.action = "android.intent.action.VIEW"
            val updateUrl = Uri.parse("https://www.chongya.com/@zhufree")
            updateIntent.data = updateUrl
            startActivity(updateIntent)
        }
    }
}
