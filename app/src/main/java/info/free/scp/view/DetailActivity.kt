package info.free.scp.view

import android.app.AlertDialog
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
import info.free.scp.R
import info.free.scp.bean.ScpModel
import info.free.scp.db.ScpDao
import info.free.scp.util.EventUtil
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.util.Toaster
import info.free.scp.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.layout_dialog_report.view.*


class DetailActivity : BaseActivity() {

    private var readMode = 0 // 0 离线 1 网页
    private var url = ""
    private var sId = ""
    private var scp: ScpModel? = null
    private var detailHtml = ""
    private val nightTextStyle = "<style>p {font-size:16px;line-height:30px;}* {color:#a0a0a0;}</style>"
    private val dayTextStyle = "<style>p {font-size:16px;line-height:30px;}* {color:#000;}</style>"
    private var currentTextStyle = if (ThemeUtil.currentTheme == 1) nightTextStyle else dayTextStyle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        initToolbar()

        url = intent.getStringExtra("link")
        // 有些不是以/开头的而是完整链接
        url = if (url.contains("http")) url else "http://scp-wiki-cn.wikidot.com$url"
        sId = intent.getStringExtra("sId")
        scp = ScpDao.getInstance().getScpModelById(sId)

        webView.setBackgroundColor(0) // 设置背景色
        webView?.background?.alpha = 0 // 设置填充透明度 范围：0-255
        webView.setBackgroundColor(ThemeUtil.containerBg)
        currentTextStyle = if (ThemeUtil.currentTheme == 1) nightTextStyle else dayTextStyle

        scp?.let {
            detailHtml = ScpDao.getInstance().getDetailById(it.sId)
            if (detailHtml.isEmpty()) {
                webView.loadUrl(url) //可以使用本地文件 file:///android_asset/xyz.html
            } else {
                pbLoading.visibility = GONE
                webView.loadDataWithBaseURL(null, currentTextStyle + detailHtml,
                        "text/html", "utf-8", null)
            }
        }
        webView.requestFocus()

        //覆盖WebView默认通过第三方或系统浏览器打开网页的行为
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (readMode == 1) {
                    view.loadUrl(url)
                } else {
                    if (url.startsWith("http://scp-wiki-cn.wikidot.com/")) {
                        val postString = url.subSequence(30, url.length)
                        Log.i("detail", "url = $postString")
                        detailHtml = ScpDao.getInstance().getDetailByLink(postString.toString())
                        webView.loadDataWithBaseURL(null, currentTextStyle + detailHtml,
                                "text/html", "utf-8", null)
                        webView.scrollTo(0, 0)
                    }
                }
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
                            "4.图片依然需要网络才能显示出来，另有一些网页上依赖代码等复杂的文章，请切换跳转网页查看\n" +
                            "5.底部的切换上下章的链接暂时不能使用，以后会开发相关功能")
                    .setPositiveButton("OK") { dialog, _ ->
                        PreferenceUtil.setShownDetailNotice()
                        dialog.dismiss()
                    }
                    .create().show()
        }

    }

    private fun initToolbar() {
        setSupportActionBar(detail_toolbar)
        detail_toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        detail_toolbar.setNavigationOnClickListener { finish() }
        detail_toolbar.inflateMenu(R.menu.detail_menu) //设置右上角的填充菜单
        detail_toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.switch_read_mode -> {
                    EventUtil.onEvent(this@DetailActivity, EventUtil.clickChangeReadMode)
                    if (readMode == 0) {
                        if (enabledNetwork()) {
                            pbLoading.visibility = VISIBLE
                            readMode = 1
                            it.setTitle(R.string.offline_mode)
                            webView.loadUrl(url)
                        } else {
                            Toaster.show("请先开启网络")
                        }
                    } else {
                        readMode = 0
                        it.setTitle(R.string.online_mode)
                        webView.loadDataWithBaseURL(null, currentTextStyle + detailHtml,
                                "text/html", "utf-8", null)
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
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.detail_menu, menu)
        return true
    }
}