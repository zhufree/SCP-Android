package info.free.scp.view.detail

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface.BUTTON_POSITIVE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.ViewTreeObserver
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class DetailActivity : BaseActivity() {

    private var onlineMode = 0 // 0 离线 1 网页
    private var readType = 0 // 0 普通（按顺序） 1 随机 2 未读列表
    private var url = ""
    private var sId = ""
    private var scp: ScpModel? = null
    private var detailHtml = ""
    private var textSizeList = arrayOf("12px", "14px", "16px", "18px", "20px")
    private var currentTextSizeIndex = 2
        set(value) {
            field = value
            nightTextStyle = "<style>body{background-color:#222;}p {font-size:" +
                    "${textSizeList[currentTextSizeIndex]};line-height:30px;}* {color:#a0a0a0;}</style>"
            dayTextStyle = "<style>p {font-size:${textSizeList[currentTextSizeIndex]}" +
                    ";line-height:30px;}* {color:#000;}</style>"
        }
    private var nightTextStyle = "<style>body{background-color:#222;}p {font-size:" +
            "${textSizeList[currentTextSizeIndex]};line-height:30px;}* {color:#a0a0a0;}</style>"
    private var dayTextStyle = "<style>p {font-size:16px;line-height:30px;}* {color:#000;}</style>"
    private val siteStyle = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />"
    private var currentTextStyle = siteStyle + (if (ThemeUtil.currentTheme == 1) nightTextStyle else dayTextStyle)
    private val jqScript = "<script type=\"text/javascript\" src=\"jquery-ui.min.js\"></script>\n"
    private val initScript = "<script type=\"text/javascript\" src=\"init.combined.js\"></script>"
    private val jsScript = jqScript + initScript + "<script type=\"text/javascript\" src=\"WIKIDOT.combined.js\"></script>"

    private val copyRightHtml = "<div id=\"license-area\" class=\"license-area\">除非特别注明，" +
            "本页内容采用以下授权方式： <a rel=\"license\" href=\"http://creativecommons.org/licenses/" +
            "by-sa/3.0/\">Creative Commons Attribution-ShareAlike 3.0 License</a></div>"
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
        webView?.settings?.javaScriptEnabled = true

        url = intent.getStringExtra("link") ?: ""
        // 有些不是以/开头的而是完整链接
        if (url.isEmpty()) {
            // 随机文档
            readType = 1
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
                if (onlineMode == 1) {
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

    private fun setData(scp: ScpModel?) {
        scp?.let {

            // 刷新toolbar（收藏状态
            invalidateOptionsMenu()
            // 更新标题
            supportActionBar?.setDisplayShowTitleEnabled(false)
            tv_bottom_set_has_read?.setText(if (it.hasRead == 1) R.string.set_has_not_read else R.string.set_has_read)
            detail_toolbar?.title = scp.title
            detailHtml = ScpDao.getInstance().getDetailById(scp.sId)
            if (detailHtml.isEmpty()) {
                webView.loadUrl(url)
            } else {
                pbLoading.visibility = GONE
                webView.loadDataWithBaseURL( "file:///android_asset/", currentTextStyle
                        + jsScript + detailHtml + copyRightHtml,
                        "text/html", "utf-8", null)
            }
            nsv_web_wrapper?.scrollTo(0, 0)
        }
    }

    /**
     * 不改变网页内容，只刷新样式
     */
    private fun refreshStyle() {
        currentTextStyle = siteStyle + (if (ThemeUtil.currentTheme == 1) nightTextStyle else dayTextStyle)
        webView.loadDataWithBaseURL( "file:///android_asset/", currentTextStyle
                + jsScript + detailHtml + copyRightHtml,
                "text/html", "utf-8", null)
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
                        if (onlineMode == 0) {
                            if (enabledNetwork()) {
                                pbLoading.visibility = VISIBLE
                                onlineMode = 1
                                it.setTitle(R.string.offline_mode)
                                webView?.loadUrl(url)
                            } else {
                                Toaster.show("请先开启网络")
                            }
                        } else {
                            onlineMode = 0
                            it.setTitle(R.string.online_mode)
                            webView?.loadDataWithBaseURL("file:///android_asset/",
                                    currentTextStyle + detailHtml + copyRightHtml,
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
                    R.id.share_picture -> {
                        // 截屏分享
                        Toaster.show("生成图片中...", context = this)
                        gp_share_content?.visibility = VISIBLE
                        cl_detail_container?.viewTreeObserver?.addOnGlobalLayoutListener(object :
                                ViewTreeObserver.OnGlobalLayoutListener {
                            override fun onGlobalLayout() {
                                cl_detail_container?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                                Flowable.create<String>({ emitter ->
                                    val bitmap = Bitmap.createBitmap(webView.width, cl_detail_container.height,
                                            Bitmap.Config.RGB_565)
                                    //使用Canvas，调用自定义view控件的onDraw方法，绘制图片
                                    val canvas = Canvas(bitmap)
                                    cl_detail_container.draw(canvas)
                                    Utils.saveBitmapFile(bitmap, scp?.title?.replace(" ", "")?:"")
                                    emitter.onNext("finish")
                                    emitter.onComplete()
                                }, BackpressureStrategy.BUFFER).subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe {
                                            Toaster.show("图片已保存")
                                            gp_share_content?.visibility = GONE
                                        }
                            }
                        })
                    }
                    R.id.big_text -> {
                        if (currentTextSizeIndex < 4) {
                            currentTextSizeIndex++
                            refreshStyle()
                        } else {

                        }
                    }
                    R.id.small_text -> {
                        if (currentTextSizeIndex > 0) {
                            currentTextSizeIndex--
                            refreshStyle()
                        } else {

                        }
                    }
                    else -> {}
                }
            }
            true
        }
    }
    private fun toNextArticle() {
        EventUtil.onEvent(this, EventUtil.clickNextArticle)
        PreferenceUtil.addPoints(1)
        scp?.let { s ->
            when(s.index) {
                15400 -> Toaster.show("已经是最后一篇了")
                else -> {
                    scp = if (readType == 0) ScpDao.getInstance().getNextScp(s.index)
                    else ScpDao.getInstance().getRandomScp()
                    setData(scp)
                }
            }
        }
    }
    private fun toPreviewArticle() {
        EventUtil.onEvent(this, EventUtil.clickLastArticle)
        PreferenceUtil.addPoints(1)
        scp?.let { s ->
            when (s.index) {
                0 -> Toaster.show("已经是第一篇了")
                else -> {
                    scp = if (readType == 0) ScpDao.getInstance().getPreviewScp(s.index)
                    else ScpDao.getInstance().getRandomScp()
                    setData(scp)
                }
            }
        }
    }
    private fun setHasRead() {
        scp?.let {s ->
            if (s.hasRead == 0) {
                // 标记已读
                EventUtil.onEvent(this, EventUtil.finishDetail)
                PreferenceUtil.addPoints(5)
                s.hasRead = 1
                ScpDao.getInstance().insertLikeAndReadInfo(s)
                tv_bottom_set_has_read?.setText(R.string.set_has_not_read)
            } else {
                // 取消已读
                EventUtil.onEvent(this, EventUtil.cancelRead)
                PreferenceUtil.reducePoints(5)
                s.hasRead = 0
                ScpDao.getInstance().insertLikeAndReadInfo(s)
                tv_bottom_set_has_read?.setText(R.string.set_has_read)
            }
        }
    }

    private fun initSwitchBtn() {
        tv_bottom_preview?.setOnClickListener {
            toPreviewArticle()
        }
        tv_top_preview?.setOnClickListener {
            toPreviewArticle()
        }
        tv_bottom_next?.setOnClickListener {
            toNextArticle()
        }
        tv_top_next?.setOnClickListener {
            toNextArticle()
        }
        tv_bottom_set_has_read?.setOnClickListener {
            setHasRead()
        }
        tv_top_set_has_read?.setOnClickListener {
            setHasRead()
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
                val menuItem = menu?.getItem(0)
                menuItem?.setIcon(R.drawable.ic_star_white_24dp)
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }
}
