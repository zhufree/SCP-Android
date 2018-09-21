package info.free.scp.view.base

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.umeng.analytics.MobclickAgent


open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    public override fun onResume() {
        super.onResume()
        MobclickAgent.onResume(this)
    }

    public override fun onPause() {
        super.onPause()
        MobclickAgent.onPause(this)
    }
}
