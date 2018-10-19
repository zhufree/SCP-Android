package info.free.scp.view

import android.app.AlertDialog
import android.app.Presentation
import android.content.DialogInterface.BUTTON_POSITIVE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View.GONE
import android.view.View.VISIBLE
import android.webkit.WebView
import android.webkit.WebViewClient
import com.umeng.analytics.MobclickAgent
import kotlinx.android.synthetic.main.activity_detail.*
import info.free.scp.R
import info.free.scp.bean.ScpModel
import info.free.scp.db.ScpDao
import info.free.scp.util.PreferenceUtil
import info.free.scp.view.base.BaseActivity
import kotlinx.android.synthetic.main.layout_dialog_report.view.*


class DetailActivity : BaseActivity() {

    private var readMode = 0
    private var url = ""
    private var sId = ""
    private var scp: ScpModel? = null
    private var detailHtml = ""

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
                    if (readMode == 0) {
                        readMode = 1
                        webView.loadUrl("http://scp-wiki-cn.wikidot.com$url")
                    } else {
                        readMode = 0
                        webView.loadData(detailHtml, "text/html", null)
                    }
                }
                R.id.report -> {
                    val reportView = LayoutInflater.from(this@DetailActivity)
                            .inflate(R.layout.layout_dialog_report, null)
                    val reportDialog = AlertDialog.Builder(this@DetailActivity)
                            .setTitle("反馈问题")
                            .setView(reportView)
                            .setPositiveButton("OK") { _, _ -> }
                            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                            .create()
                    reportDialog.show()
                    reportDialog.getButton(BUTTON_POSITIVE).setOnClickListener {
                        val reportString = reportView.et_report.text.toString()
                        Log.i("report", reportString)
                        MobclickAgent.reportError(this@DetailActivity, "url: $url, detail: $reportString")
                        reportDialog.dismiss()
                    }
                }
            }
            true
        }
        
        url = intent.getStringExtra("link")
        sId = intent.getStringExtra("sId")
        scp = ScpDao.getInstance().getScpModelById(sId)

        scp?.let {
            detailHtml = ScpDao.getInstance().getDetailById(it.sId)
            if (detailHtml.contains("该页面尚无内容") || detailHtml.isEmpty()) {
                webView.loadUrl("http://scp-wiki-cn.wikidot.com$url") //可以使用本地文件 file:///android_asset/xyz.html
            } else {
                pbLoading.visibility = GONE
                webView.loadData(detailHtml, "text/html", null)
            }
        }

        webView.requestFocus()
        //覆盖WebView默认通过第三方或系统浏览器打开网页的行为
        webView.webViewClient = object:WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl("http://scp-wiki-cn.wikidot.com$url")
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                pbLoading.visibility = GONE
            }
        }

        if (!PreferenceUtil.getShownDetailNotice()) {
            AlertDialog.Builder(this)
                    .setTitle("Notice")
                    .setMessage("1.右上角菜单可以切换网络阅读和离线阅读模式（如果本地数据没有加载完成则离线模式可能不可用）\n" +
                            "2.所有显示尚无内容的即表示在网上是404状态，即禁止访问，可能存在数据更新不及时的情况，所以也可以切换阅读模式看原网页\n" +
                            "3.文档数量较多，如果发现有疏漏，如文不对题等，可右上角菜单选择反馈问题\n" +
                            "4.底部的切换上下章的链接暂时不能使用，以后会开发相关功能")
                    .setPositiveButton("OK") { dialog, _ ->
                        PreferenceUtil.setShownDetailNotice()
                        dialog.dismiss()
                    }
                    .create().show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.detail_menu, menu)
        return true
    }
}
