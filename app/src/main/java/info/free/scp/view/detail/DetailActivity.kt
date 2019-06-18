package info.free.scp.view.detail

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface.BUTTON_POSITIVE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_BACK
import android.view.LayoutInflater
import android.view.Menu
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewTreeObserver
import android.webkit.WebView
import android.webkit.WebViewClient
import com.umeng.analytics.MobclickAgent
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.HISTORY_TYPE
import info.free.scp.bean.ScpLikeModel
import info.free.scp.bean.ScpModel
import info.free.scp.db.AppInfoDatabase
import info.free.scp.db.ScpDatabase
import info.free.scp.db.ScpDataHelper
import info.free.scp.util.*
import info.free.scp.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.layout_dialog_report.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.info
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import taobe.tec.jcc.JChineseConvertor
import java.io.IOException


class DetailActivity : BaseActivity() {

    private var onlineMode = 0 // 0 离线 1 网页
    private var readType = 0 // 0 普通（按顺序） 1 随机 2 TODO 未读列表
    private var randomType = 0 // 0 所有，1仅scp，2 故事，3 joke
    private var itemType = 0 //
    private var url = ""
        set(value) {
            field = value
            fullUrl = if (value.contains("http")) value else "http://scp-wiki-cn.wikidot.com$value"
        }
    private var scp: ScpModel? = null
    private var detailHtml = ""
    private var textSizeList = arrayOf("12px", "14px", "16px", "18px", "20px")
    private var currentTextSizeIndex = textSizeList.indexOf(PreferenceUtil.getDetailTextSize())
        set(value) {
            field = value
            currentTextSize = textSizeList[value]
        }
    private var currentTextSize = PreferenceUtil.getDetailTextSize()
        set(value) {
            field = value
            PreferenceUtil.setDetailTextSize(value)
            nightTextStyle = "<style>body{background-color:#222;}p {font-size:" +
                    "$currentTextSize;}* {color:#aaa;}</style>"
            dayTextStyle = "<style>p {font-size:$currentTextSize}" +
                    ";}* {color:#000;}</style>"
        }
    private var nightTextStyle = "<style>body{background-color:#222;}p {font-size:" +
            "$currentTextSize;}* {color:#aaa;}</style>"
    private var dayTextStyle = "<style>p {font-size:$currentTextSize;}* {color:#000;}</style>"
    private val siteStyle = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />"
    private var currentTextStyle = siteStyle + (if (ThemeUtil.currentTheme == 1) nightTextStyle else dayTextStyle)
    private val jqScript = "<script type=\"text/javascript\" src=\"jquery-ui.min.js\"></script>\n"
    private val initScript = "<script type=\"text/javascript\" src=\"init.combined.js\"></script>"
    private val tabScript = "<script type=\"text/javascript\" src=\"tabview-min.js\"></script>"
    private val wikiScript = "<script type=\"text/javascript\" src=\"WIKIDOT.combined.js\"></script>"
    private val jsScript = "$jqScript$initScript$tabScript$wikiScript"
    private var screenHeight = 0
    private val historyList: MutableList<ScpModel> = emptyList<ScpModel>().toMutableList()
    private val randomList: MutableList<ScpModel> = emptyList<ScpModel>().toMutableList()
    private var randomIndex = 0
    private var historyIndex = 0
    private var fullUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        screenHeight = Utils.getScreenHeight(this)

        initToolbar()
        initSwitchBtn()
        EventUtil.onEvent(this, EventUtil.clickReadDetail)
        webView?.setBackgroundColor(0) // 设置背景色
        webView?.background?.alpha = 0 // 设置填充透明度 范围：0-255
        webView?.setBackgroundColor(ThemeUtil.containerBg)
        webView?.settings?.javaScriptEnabled = true

        url = intent.getStringExtra("link") ?: ""
        // 0 普通（按顺序） 1 随机 2 TODO 未读列表
        readType = intent.getIntExtra("read_type", 0)
        // 0 所有，1仅scp，2 故事，3 joke
        randomType = intent.getIntExtra("random_type", 0)
        itemType = intent.getIntExtra("scp_type", 0)

        // 有些不是以/开头的而是完整链接
        if (url.isEmpty()) {
            // 入口都确定了有url，没有的话直接finish
            finish()
        } else {
            scp = if (itemType == 0) ScpDatabase.getInstance()?.scpDao()
                    ?.getScpByLink(url) else ScpDatabase.getInstance()?.scpDao()
                    ?.getCollectionByLink(url)
        }

        fullUrl = if (url.contains("http")) url else "http://scp-wiki-cn.wikidot.com$url"

        scp?.let {
            // 数据库取到
            if (readType == 1) {
                randomList.add(it)
            }
            setData(it)
        } ?: run {
            // 数据库没有，加载链接
            pbLoading.visibility = VISIBLE
            webView.loadUrl(fullUrl)
            nsv_web_wrapper?.scrollTo(0, 0)
        }

        webView?.requestFocus()

        //覆盖WebView默认通过第三方或系统浏览器打开网页的行为
        webView?.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, requestUrl: String): Boolean {
                info(requestUrl)
                if (requestUrl.startsWith("http://scp-wiki-cn.wikidot.com/") and requestUrl.contains("/html/")) {
                    return false
                }
                if (requestUrl.startsWith("http://scp-wiki-cn.wdfiles.com/")) {
                    return false
                }

                if (onlineMode == 1) {
                    view.loadUrl(requestUrl)
                } else {
                    if (requestUrl.startsWith("http://scp-wiki-cn.wikidot.com/")) {
                        url = requestUrl.subSequence(30, requestUrl.length).toString()
                    } else if (requestUrl.startsWith("file:")) {
                        url = requestUrl.subSequence(7, requestUrl.length).toString()
                    } else {
                        url = requestUrl
                    }
                    info(url)
                    val scp = ScpDatabase.getInstance()?.scpDao()?.getScpByLink(url)
                    scp?.let {

                        setData(scp)
                    } ?: run {
                        pbLoading.visibility = VISIBLE
                        view.loadUrl(fullUrl)
                    }
                }
                return false
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

    private fun setData(scp: ScpModel, back: Boolean = false) {
        ScpDataHelper.getInstance().insertViewListItem(scp.link, scp.title, HISTORY_TYPE)
        AppInfoDatabase.getInstance().readRecordDao().delete(scp.link, SCPConstants.LATER_TYPE)
        // 刷新toolbar（收藏状态
        invalidateOptionsMenu()
        refreshReadBtnStatus()
        // 更新标题
        supportActionBar?.setDisplayShowTitleEnabled(false)
        if (!back) {
            historyList.add(scp)
            historyIndex = historyList.size - 1
        }
        detail_toolbar?.title = scp.title
        url = scp.link
        detailHtml = ScpDatabase.getInstance()?.detailDao()?.getDetail(scp.link) ?: ""
        // 显示frame
        detailHtml = detailHtml.replace("""<iframe src="/""", """<iframe src="http://scp-wiki-cn.wikidot.com/""")
        detailHtml = detailHtml.replace("html-block-iframe", "")
        if (detailHtml.isEmpty()) {
            pbLoading.visibility = VISIBLE
            webView.loadUrl(fullUrl)
        } else {
            pbLoading.visibility = GONE
            webView.loadDataWithBaseURL("file:///android_asset/", currentTextStyle
                    + detailHtml + jsScript,
                    "text/html", "utf-8", null)
        }
        nsv_web_wrapper?.scrollTo(0, 0)
    }

    /**
     * 不改变网页内容，只刷新样式
     */
    private fun refreshStyle() {
        currentTextStyle = siteStyle + (if (ThemeUtil.currentTheme == 1) nightTextStyle else dayTextStyle)
        webView.loadDataWithBaseURL("file:///android_asset/", currentTextStyle
                + detailHtml + jsScript,
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
            scp?.let { s ->
                when (it.itemId) {
                    R.id.switch_read_mode -> {
                        PreferenceUtil.addPoints(1)
                        if (onlineMode == 0) {
                            if (enabledNetwork()) {
                                pbLoading.visibility = VISIBLE
                                onlineMode = 1
                                it.setTitle(R.string.offline_mode)
                                webView?.loadUrl(fullUrl)
                            } else {
                                toast("请先开启网络")
                            }
                        } else {
                            onlineMode = 0
                            it.setTitle(R.string.online_mode)
                            webView?.loadDataWithBaseURL("file:///android_asset/",
                                    currentTextStyle + detailHtml + jsScript,
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
                        val openUrl = Uri.parse(fullUrl)
                        openIntent.data = openUrl
                        startActivity(openIntent)
                    }
                    R.id.copy_link -> {
                        EventUtil.onEvent(this, EventUtil.clickCopyLink, s.link)
                        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                        val clipData = ClipData.newPlainText("scp_link", fullUrl)
                        clipboardManager?.primaryClip = clipData
                        toast("已复制到剪贴板")
                    }
                    R.id.like -> {
                        likeScp()
                    }
                    R.id.add_read_later -> {
                        ScpDataHelper.getInstance().insertViewListItem(s.link, s.title,
                                SCPConstants.LATER_TYPE)
                        toast("已加入待读列表")
                    }
                    R.id.share_picture -> {
                        // 截屏分享
                        EventUtil.onEvent(this, EventUtil.clickShareByPicture, s.link)
                        toast("生成图片中...")
                        gp_share_content?.visibility = VISIBLE
                        cl_detail_container?.viewTreeObserver?.addOnGlobalLayoutListener(object :
                                ViewTreeObserver.OnGlobalLayoutListener {
                            override fun onGlobalLayout() {
                                cl_detail_container?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                                val bitmap = Bitmap.createBitmap(webView.width, cl_detail_container.height,
                                        Bitmap.Config.RGB_565)
                                //使用Canvas，调用自定义view控件的onDraw方法，绘制图片
                                val canvas = Canvas(bitmap)
                                cl_detail_container.draw(canvas)
                                doAsync {
                                    // Runs in background
                                    Utils.saveBitmapFile(bitmap, scp?.title?.replace(" ", "") ?: "")
                                    // This code is executed on the UI thread
                                    uiThread {
                                        toast("图片已保存")
                                        gp_share_content?.visibility = GONE
                                    }
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
                    R.id.translate -> {
                        translate()
                    }
                    else -> {
                    }
                }
            }
            true
        }
    }

    private fun translate() {
        try {
            val converter = JChineseConvertor.getInstance()
            detailHtml = converter.t2s(detailHtml)
            webView.loadDataWithBaseURL("file:///android_asset/", currentTextStyle
                    + detailHtml + jsScript,
                    "text/html", "utf-8", null)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun likeScp() {
        scp?.let { s ->
            PreferenceUtil.addPoints(2)
            var scpInfo = AppInfoDatabase.getInstance().likeAndReadDao().getInfoByLink(s.link)
            if (scpInfo == null) {
                scpInfo = ScpLikeModel(s.link, s.title, false, hasRead = false)
            }
            scpInfo.like = !scpInfo.like
            AppInfoDatabase.getInstance().likeAndReadDao().save(scpInfo)
        }
        invalidateOptionsMenu()
    }

    private fun toNextArticle() {
        val index = scp?.index ?: 0
        val scpType = scp?.scpType ?: 0
        PreferenceUtil.addPoints(1)
        when (readType) {
            0 -> {
//                    scp = ScpDataHelper.getInstance().getNextScp(s.index)
                scp = if (itemType == 0) {
                    ScpDatabase.getInstance()?.scpDao()?.getNextScp(index, scpType)
                } else {
                    ScpDatabase.getInstance()?.scpDao()?.getNextCollection(index, scpType)
                }
                scp?.let {
                    setData(it)
                } ?: toast("已经是最后一篇了")
            }
            1 -> {
                if (randomIndex < randomList.size - 1) {
                    scp = randomList[++randomIndex]
                    scp?.let {
                        setData(it)
                    }
                } else {
                    val randomRange = when (randomType) {
                        1 -> "1,2"
                        2 -> "3,4"
                        3 -> "5,6"
                        else -> ""
                    }
                    scp = ScpDataHelper.getInstance().getRandomScp(randomRange)
                    scp?.let {
                        randomList.add(it)
                        randomIndex++
                        Log.i("random", randomIndex.toString())
                        setData(it)
                    }
                }
            }
            else -> {
            }
        }
    }

    private fun toPreviewArticle() {
        val index = scp?.index ?: 0
        val scpType = scp?.scpType ?: 0
        PreferenceUtil.addPoints(1)
        when (readType) {
            0 -> {
                when (index) {
                    0 -> toast("已经是第一篇了")
                    else -> {
                        scp = if (itemType == 0) {
                            ScpDatabase.getInstance()?.scpDao()?.getPreviewScp(index, scpType)
                        } else {
                            ScpDatabase.getInstance()?.scpDao()?.getPreviewCollection(index, scpType)
                        }
                        scp?.let {
                            setData(it)
                        }
                    }
                }
            }
            1 -> {
                if (randomList.isNotEmpty() && randomIndex - 1 >= 0) {
                    scp = randomList[--randomIndex]
                    Log.i("random", randomIndex.toString())
                    scp?.let {
                        setData(it)
                    }
                } else {
                    toast("已经是第一篇了")
                }
            }
            else -> {
            }
        }

    }

    private var readBtnLp: ConstraintLayout.LayoutParams? = null

    private fun setHasRead() {
        scp?.let { s ->
            var scpInfo = AppInfoDatabase.getInstance().likeAndReadDao().getInfoByLink(s.link)
            if (scpInfo == null) {
                scpInfo = ScpLikeModel(s.link, s.title, false, hasRead = false)
            }
            if (scpInfo.hasRead) {
                // 取消已读
                PreferenceUtil.reducePoints(5)
                scpInfo.hasRead = false
                AppInfoDatabase.getInstance().likeAndReadDao().save(scpInfo)
                refreshReadBtnStatus()
            } else {
                // 标记已读
                PreferenceUtil.addPoints(5)
                scpInfo.hasRead = true
                AppInfoDatabase.getInstance().likeAndReadDao().save(scpInfo)
                refreshReadBtnStatus()
            }
        }
    }

    private fun refreshReadBtnStatus() {
        val scpInfo = AppInfoDatabase.getInstance().likeAndReadDao().getInfoByLink(url)
        val hasRead = scpInfo != null && scpInfo.hasRead
        readBtnLp = tv_bottom_set_has_read?.layoutParams as ConstraintLayout.LayoutParams?
        if (hasRead) {
            tv_bottom_set_has_read?.setText(R.string.set_has_not_read)
            tv_bottom_set_has_read?.setBackgroundColor(ThemeUtil.disabledBg)
            readBtnLp?.endToEnd = -1
            readBtnLp?.startToStart = -1
            readBtnLp?.endToStart = R.id.gl_detail_center
            tv_bottom_set_has_read?.layoutParams = readBtnLp
            tv_bottom_like?.visibility = VISIBLE
        } else {
            tv_bottom_set_has_read?.setText(R.string.set_has_read)
            tv_bottom_set_has_read?.setBackgroundColor(ThemeUtil.itemBg)
            readBtnLp?.endToEnd = 0
            readBtnLp?.startToStart = 0
            readBtnLp?.endToStart = -1
            tv_bottom_set_has_read?.layoutParams = readBtnLp
            tv_bottom_like?.visibility = GONE
        }
    }

    private fun initSwitchBtn() {
        tv_bottom_preview?.setOnClickListener {
            toPreviewArticle()
        }

        tv_bottom_next?.setOnClickListener {
            toNextArticle()
        }

        tv_bottom_set_has_read?.setOnClickListener {
            setHasRead()
        }

        tv_bottom_like?.setOnClickListener { likeScp() }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KEYCODE_BACK && webView != null && historyIndex > 0) {
            scp = historyList[historyIndex - 1]
            scp?.let {
                setData(it, true)
            }
            historyList.removeAt(historyIndex - 1)
            historyIndex--
            return true
        } else {
            return super.onKeyDown(keyCode, event)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.detail_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val scpInfo = AppInfoDatabase.getInstance().likeAndReadDao().getInfoByLink(url)
        val menuItem = menu?.getItem(0)
        if (scpInfo == null || !scpInfo.like) {
            menuItem?.setIcon(R.drawable.ic_star_border_white_24dp)
            tv_bottom_like?.text = "收藏"
            tv_bottom_like?.setBackgroundColor(ThemeUtil.itemBg)
        } else {
            menuItem?.setIcon(R.drawable.ic_star_white_24dp)
            tv_bottom_like?.text = "取消收藏"
            tv_bottom_like?.setBackgroundColor(ThemeUtil.disabledBg)
        }
        return super.onPrepareOptionsMenu(menu)
    }
}
