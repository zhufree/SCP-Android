package info.free.scp.view.home

import android.os.Bundle
import info.free.scp.R
import info.free.scp.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_privacy.*

class PrivacyActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)
        baseToolbar = privacy_toolbar
    }
}