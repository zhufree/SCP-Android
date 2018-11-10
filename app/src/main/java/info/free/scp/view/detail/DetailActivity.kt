package info.free.scp.view.detail

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface.BUTTON_POSITIVE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.widget.NestedScrollView
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_BACK
import android.view.LayoutInflater
import android.view.Menu
import android.view.View.GONE
import android.view.View.VISIBLE
import android.webkit.WebView
import android.webkit.WebViewClient
import com.umeng.analytics.MobclickAgent
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.bean.ScpModel
import info.free.scp.db.ScpDao
import info.free.scp.util.*
import info.free.scp.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.layout_dialog_report.view.*


class DetailActivity : BaseActivity() {

    private var readMode = 0 // 0 离线 1 网页
    private var showSecretContent = false
    private var url = ""
    private var sId = ""
    private var scp: ScpModel? = null
    private var detailHtml = ""
    private val nightTextStyle = "<style>p {font-size:16px;line-height:30px;}* {color:#a0a0a0;}</style>"
    private val dayTextStyle = "<style>p {font-size:16px;line-height:30px;}* {color:#000;}</style>"
    private var currentTextStyle = if (ThemeUtil.currentTheme == 1) nightTextStyle else dayTextStyle
    private var screenHeight = 0
    private val history: MutableList<ScpModel> = emptyList<ScpModel>().toMutableList()
    private var historyIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        screenHeight = Utils.getScreenHeight(this)

        initToolbar()
        initSwitchBtn()

        webView?.setBackgroundColor(0) // 设置背景色
        webView?.background?.alpha = 0 // 设置填充透明度 范围：0-255
        webView?.setBackgroundColor(ThemeUtil.containerBg)
        currentTextStyle = if (ThemeUtil.currentTheme == 1) nightTextStyle else dayTextStyle

        url = intent.getStringExtra("link") ?: ""
        // 有些不是以/开头的而是完整链接
        if (url.isEmpty()) {
            // 随机文档
            scp = ScpDao.getInstance().getRandomScp()
        } else {
            url = if (url.contains("http")) url else "http://scp-wiki-cn.wikidot.com$url"
            sId = intent.getStringExtra("sId")
            scp = ScpDao.getInstance().getScpModelById(sId)
        }

        scp?.let {
            historyIndex = history.size
            history.add(it)
        }

        setData(scp)
        webView?.requestFocus()

        //覆盖WebView默认通过第三方或系统浏览器打开网页的行为
        webView?.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (readMode == 1) {
                    view.loadUrl(url)
                } else {
                    if (url.startsWith("http://scp-wiki-cn.wikidot.com/")) {
                        val postString = url.subSequence(30, url.length)
                        Log.i("detail", "url = $postString")
                        val scpList = ScpDao.getInstance().getScpModelByLink(postString.toString())
                        if (scpList.isNotEmpty()) {
                            scp = scpList[0]
                            scp?.let {
                                historyIndex = history.size
                                history.add(it)
                            }
                            setData(scp)
                        }
                    }
                }
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                pbLoading.visibility = GONE
                // 判断切换按钮是否可见，如果可以，说明篇幅较短，视为已读
                Handler().postDelayed({
                    checkHasRead()
                }, 2000)
                nsv_web_wrapper?.setOnScrollChangeListener { _: NestedScrollView?, _: Int, _: Int, _: Int,
                                                             _: Int  ->
                    checkHasRead()
                }
            }
        }

        if (!PreferenceUtil.getShownDetailNotice()) {
            AlertDialog.Builder(this)
                    .setTitle("Notice")
                    .setMessage("1.右上角菜单可以切换网络阅读和离线阅读模式（如果本地数据没有加载完成则离线模式可能不可用）\n" +
                            "2.所有显示尚无内容的即表示在网上是404状态，即禁止访问，可能存在数据更新不及时的情况，所以也可以切换阅读模式看原网页\n" +
                            "3.文档数量较多，如果发现有疏漏，如文不对题等，可右上角菜单选择反馈问题\n" +
                            "4.图片依然需要网络才能显示出来，另有一些网页上依赖代码等复杂的文章，请切换跳转网页查看\n")
                    .setPositiveButton("OK") { dialog, _ ->
                        PreferenceUtil.setShownDetailNotice()
                        dialog.dismiss()
                    }
                    .create().show()
        }

    }

    private fun checkHasRead() {
        scp?.let {
            if (it.hasRead == 0) {
                val location = IntArray(2)
                tv_preview?.getLocationOnScreen(location)
                if (location[1] < screenHeight) {
                    EventUtil.onEvent(this, EventUtil.finishDetail)
                    PreferenceUtil.addPoints(5)
                    it.hasRead = 1
                    Log.i("detail", "已读完")
                    ScpDao.getInstance().insertLikeAndReadInfo(it)
                }
            }
        }
    }

    private fun setData(scp: ScpModel?) {
        scp?.let {

            // 刷新toolbar（收藏状态
            invalidateOptionsMenu()
            // 更新标题
            supportActionBar?.setDisplayShowTitleEnabled(false)
            detail_toolbar?.title = scp.title
            detailHtml = ScpDao.getInstance().getDetailById(scp.sId)
            if (detailHtml.isEmpty()) {
                webView.loadUrl(url)
            } else {
                pbLoading.visibility = GONE
                webView.loadDataWithBaseURL(null, currentTextStyle + detailHtml,
                        "text/html", "utf-8", null)
            }
            nsv_web_wrapper?.setOnScrollChangeListener { _: NestedScrollView?, _: Int, _: Int, _: Int,
                                                         _: Int  -> }
            Handler().postDelayed({
                nsv_web_wrapper?.scrollTo(0, 0)
            }, 1000)

        }
    }

    private fun initToolbar() {
        setSupportActionBar(detail_toolbar)
        detail_toolbar?.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        detail_toolbar?.setNavigationOnClickListener {
            finish()
        }
        detail_toolbar?.inflateMenu(R.menu.detail_menu) //设置右上角的填充菜单
        detail_toolbar?.setOnMenuItemClickListener {
            scp?.let {s ->
                when (it.itemId) {
                    R.id.switch_read_mode -> {
                        EventUtil.onEvent(this@DetailActivity, EventUtil.clickChangeReadMode)
                        PreferenceUtil.addPoints(1)
                        if (readMode == 0) {
                            if (enabledNetwork()) {
                                pbLoading.visibility = VISIBLE
                                readMode = 1
                                it.setTitle(R.string.offline_mode)
                                webView?.loadUrl(url)
                            } else {
                                Toaster.show("请先开启网络")
                            }
                        } else {
                            readMode = 0
                            it.setTitle(R.string.online_mode)
                            webView?.loadDataWithBaseURL(null, currentTextStyle + detailHtml,
                                    "text/html", "utf-8", null)
                        }
                    }
                    // 显示隐藏内容
                    R.id.show_secret_content -> {
                        showSecretContent = !showSecretContent
                        it.setTitle(if (showSecretContent) R.string.hide_secret_content else R.string.show_secret_content)
                        webView.loadDataWithBaseURL(null, currentTextStyle +
                                (if (showSecretContent) detailHtml.replace("display:none;", "")
                                        .replace("display: none;", "")
                                        .replace("display: none", "")
                                        .replace("display:none", "")
                                        else detailHtml),
                                "text/html", "utf-8", null)
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
                    R.id.open_in_browser -> {
                        EventUtil.onEvent(this, EventUtil.clickOpenInBrowser, s.link)
                        PreferenceUtil.addPoints(1)
                        val openIntent = Intent()
                        openIntent.action = "android.intent.action.VIEW"
                        val openUrl = Uri.parse(SCPConstants.SCP_SITE_URL + s.link)
                        openIntent.data = openUrl
                        startActivity(openIntent)
                    }
                    R.id.copy_link -> {
                        EventUtil.onEvent(this, EventUtil.clickCopyLink, s.link)
                        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                        val clipData = ClipData.newPlainText("scp_link", SCPConstants.SCP_SITE_URL + s.link)
                        clipboardManager?.primaryClip = clipData
                        Toaster.show("已复制到剪贴板")
                    }
                    R.id.like -> {
                        EventUtil.onEvent(this, EventUtil.clickLike, s.link)
                        PreferenceUtil.addPoints(2)
                        s.like = if (s.like == 1) 0 else 1
                        ScpDao.getInstance().insertLikeAndReadInfo(s)
                        it.setIcon(if (s.like == 1) R.drawable.ic_star_white_24dp
                        else R.drawable.ic_star_border_white_24dp)
                    }
                    else -> {}
                }
            }
            true
        }
    }

    private fun initSwitchBtn() {
        tv_preview?.setOnClickListener {
            EventUtil.onEvent(this, EventUtil.clickLastArticle)
            PreferenceUtil.addPoints(1)
            scp?.let { s ->
                when (s.index) {
                    0 -> Toaster.show("已经是第一篇了")
                    else -> {
                        scp = ScpDao.getInstance().getPreviewScp(s.index)
                        setData(scp)
                    }
                }
            }

        }
        tv_next?.setOnClickListener {
            EventUtil.onEvent(this, EventUtil.clickNextArticle)
            PreferenceUtil.addPoints(1)
            scp?.let { s ->
                when(s.index) {
                    14001 -> Toaster.show("已经是最后一篇了")
                    else -> {
                        scp = ScpDao.getInstance().getNextScp(s.index)
                        setData(scp)
                    }
                }
            }
        }
        tv_random?.setOnClickListener {
            EventUtil.onEvent(this, EventUtil.clickArticleRandom)
            PreferenceUtil.addPoints(1)
            scp = ScpDao.getInstance().getRandomScp()
            setData(scp)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KEYCODE_BACK && webView != null && historyIndex > 0) {
            if (historyIndex > 0) {
                setData(history[historyIndex - 1])
                history.removeAt(historyIndex)
                historyIndex--
                return true
            } else if (webView.canGoBack()) {
                webView.goBack()
                return true
            } else {
                return super.onKeyDown(keyCode, event)
            }
        } else {
            return super.onKeyDown(keyCode, event)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.detail_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        scp?.let {
            if (it.like == 1) {
                val menuItem = menu?.getItem(2)
                menuItem?.setIcon(R.drawable.ic_star_white_24dp)
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }
}
