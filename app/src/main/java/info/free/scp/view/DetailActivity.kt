package info.free.scp.view

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View.GONE
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_detail.*
import info.free.scp.R
import info.free.scp.db.ScpDao
import info.free.scp.view.base.BaseActivity


class DetailActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        setSupportActionBar(detail_toolbar)
        detail_toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        detail_toolbar.setNavigationOnClickListener { finish() }
        detail_toolbar.inflateMenu(R.menu.detail_menu) //设置右上角的填充菜单
        detail_toolbar.setOnMenuItemClickListener{
            when (it.itemId) {
                R.id.switch_read_mode -> {
                    // TODO 切换阅读模式
                }
                R.id.cn_page -> {
                    // TODO 反馈问题
                }
            }
            true
        }
        
        val url = intent.getStringExtra("link")
        val sId = intent.getStringExtra("sId")
        val scp = ScpDao.getInstance().getScpModelById(sId)

        scp?.let {
            Log.i("detail", scp.detailHtml)
            if (it.detailHtml.contains("该页面尚无内容")) {
                webView.loadUrl("http://scp-wiki-cn.wikidot.com$url") //可以使用本地文件 file:///android_asset/xyz.html
            } else {
                pbLoading.visibility = GONE
                webView.loadData(it.detailHtml, "text/html", null)
            }
        }

        webView.requestFocus()
        //覆盖WebView默认通过第三方或系统浏览器打开网页的行为
        webView.webViewClient = object:WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                pbLoading.visibility = GONE
                Log.i("web", url)
                view?.evaluateJavascript("document.getElementById('content-wrap').removeChild(document.getElementById('side-bar'));") {
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.detail_menu, menu)
        return true
    }
}
