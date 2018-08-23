package info.free.scp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View.GONE
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_web.*


class WebActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)

        val url = intent.getStringExtra("link")

        webView.loadUrl("http://scp-wiki-cn.wikidot.com$url") //可以使用本地文件 file:///android_asset/xyz.html
        webView.requestFocus()
        //覆盖WebView默认通过第三方或系统浏览器打开网页的行为
        webView.webViewClient = object:WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                pbLoading.visibility = GONE
            }
        }
    }
}
