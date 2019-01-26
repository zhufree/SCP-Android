package info.free.scp.view.detail

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface.BUTTON_POSITIVE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.support.constraint.ConstraintLayout
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
import com.tendcloud.tenddata.TCAgent
import com.umeng.analytics.MobclickAgent
import info.free.scp.R
import info.free.scp.R.style.AppTheme
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.HISTORY_TYPE
import info.free.scp.bean.ScpModel
import info.free.scp.db.ScpDao
import info.free.scp.util.*
import info.free.scp.view.base.BaseActivity
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.layout_dialog_report.view.*


class DetailActivity : BaseActivity() {

    private var onlineMode = 0 // 0 离线 1 网页
    private var readType = 0 // 0 普通（按顺序） 1 随机 2 TODO 未读列表
    private var url = ""
    private var sId = ""
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
                    "$currentTextSize;line-height:30px;}* {color:#a0a0a0;}</style>"
            dayTextStyle = "<style>p {font-size:$currentTextSize}" +
                    ";line-height:30px;}* {color:#000;}</style>"
        }
    private var nightTextStyle = "<style>body{background-color:#222;}p {font-size:" +
            "$currentTextSize;line-height:30px;}* {color:#a0a0a0;}</style>"
    private var dayTextStyle = "<style>p {font-size:$currentTextSize;line-height:30px;}* {color:#000;}</style>"
    private val siteStyle = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />"
    private var currentTextStyle = siteStyle + (if (ThemeUtil.currentTheme == 1) nightTextStyle else dayTextStyle)
    private val jqScript = "<script type=\"text/javascript\" src=\"jquery-ui.min.js\"></script>\n"
    private val initScript = "<script type=\"text/javascript\" src=\"init.combined.js\"></script>"
    private val tabScript = "<script type=\"text/javascript\" src=\"tabview-min.js\"></script>"
    private val wikiScript = "<script type=\"text/javascript\" src=\"WIKIDOT.combined.js\"></script>"
    private val jsScript = "$jqScript$initScript$tabScript$wikiScript"

    private val copyRightHtml = "<div id=\"license-area\" class=\"license-area\">除非特别注明，" +
            "本页内容采用以下授权方式： <a rel=\"license\" href=\"http://creativecommons.org/licenses/" +
            "by-sa/3.0/\">Creative Commons Attribution-ShareAlike 3.0 License</a></div>"
    private var screenHeight = 0
    private val historyList: MutableList<ScpModel> = emptyList<ScpModel>().toMutableList()
    private val randomList: MutableList<ScpModel> = emptyList<ScpModel>().toMutableList()
    private var randomIndex = 0
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
            sId = intent?.getStringExtra("sId") ?: ""
            scp = if (sId.isNotEmpty()) ScpDao.getInstance().getScpModelById(sId) else
                ScpDao.getInstance().getOneScpModelByLink(url)
            url = if (url.contains("http")) url else "http://scp-wiki-cn.wikidot.com$url"
        }

        scp?.let {
            historyIndex = historyList.size
            historyList.add(it)
            if (readType == 1) {
                randomList.add(it)
            }
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
                                historyIndex = historyList.size
                                historyList.add(it)
                            }
                            setData(scp)
                        } else {
                            pbLoading.visibility = VISIBLE
                            view.loadUrl(url)
                        }
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

    private fun setData(scp: ScpModel?) {
        scp?.let {
            ScpDao.getInstance().insertViewListItem(it.link, it.title, HISTORY_TYPE)
            // 刷新toolbar（收藏状态
            invalidateOptionsMenu()
            refreshReadBtnStatus(it.hasRead)
            // 更新标题
            supportActionBar?.setDisplayShowTitleEnabled(false)
            detail_toolbar?.title = it.title
            detailHtml = ScpDao.getInstance().getDetailByLink(it.link)
            if (detailHtml.isEmpty()) {
                pbLoading.visibility = VISIBLE
                url = if (it.link.contains("http")) it.link else "http://scp-wiki-cn.wikidot.com${it.link}"
                webView.loadUrl(url)
            } else {
                pbLoading.visibility = GONE
                webView.loadDataWithBaseURL("file:///android_asset/", currentTextStyle
                        + jsScript + detailHtml,
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
        webView.loadDataWithBaseURL("file:///android_asset/", currentTextStyle
                + jsScript + detailHtml,
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
                                webView?.loadUrl(url)
                            } else {
                                Toaster.show("请先开启网络")
                            }
                        } else {
                            onlineMode = 0
                            it.setTitle(R.string.online_mode)
                            webView?.loadDataWithBaseURL("file:///android_asset/",
                                    currentTextStyle + detailHtml,
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
                            TCAgent.onError(this@DetailActivity, Throwable("url: $url, detail: $reportString"))
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
                        likeScp()
                    }
                    R.id.share_picture -> {
                        // 截屏分享
                        EventUtil.onEvent(this, EventUtil.clickShareByPicture, s.link)
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
                                    Utils.saveBitmapFile(bitmap, scp?.title?.replace(" ", "") ?: "")
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
                    else -> {
                    }
                }
            }
            true
        }
    }

    private fun likeScp() {
        scp?.let { s ->
            PreferenceUtil.addPoints(2)
            s.like = if (s.like == 1) 0 else 1
            ScpDao.getInstance().insertLikeAndReadInfo(s)
        }
        invalidateOptionsMenu()
    }

    private fun toNextArticle() {
        PreferenceUtil.addPoints(1)
        when (readType) {
            0 -> {
                scp?.let { s ->
                    scp = ScpDao.getInstance().getNextScp(s.index)
                    scp?.let {
                        setData(scp)
                    } ?: Toaster.show("已经是最后一篇了")
                }
            }
            1 -> {
                if (randomIndex < randomList.size - 1) {
                    scp = randomList[++randomIndex]
                    scp?.let {
                        setData(it)
                    }
                } else {
                    scp = ScpDao.getInstance().getRandomScp()
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
        PreferenceUtil.addPoints(1)
        when (readType) {
            0 -> {
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
            1 -> {
                if (randomList.isNotEmpty() && randomIndex - 1 >= 0) {
                    scp = randomList[--randomIndex]
                    Log.i("random", randomIndex.toString())
                    setData(scp)
                } else {
                    Toaster.show("已经是第一篇了")
                }
            }
            else -> {
            }
        }

    }

    private var readBtnLp: ConstraintLayout.LayoutParams? = null

    private fun setHasRead() {
        scp?.let { s ->
            if (s.hasRead == 0) {
                // 标记已读
                PreferenceUtil.addPoints(5)
                s.hasRead = 1
                ScpDao.getInstance().insertLikeAndReadInfo(s)
                Logger.i(ThemeUtil.lightText.toString())
                refreshReadBtnStatus(1)
            } else {
                // 取消已读
                PreferenceUtil.reducePoints(5)
                s.hasRead = 0
                ScpDao.getInstance().insertLikeAndReadInfo(s)
                refreshReadBtnStatus(0)
            }
        }
    }

    private fun refreshReadBtnStatus(hasRead: Int) {
        readBtnLp = tv_bottom_set_has_read?.layoutParams as ConstraintLayout.LayoutParams?
        if (hasRead == 1) {
            tv_bottom_set_has_read?.setText(R.string.set_has_not_read)
            tv_bottom_set_has_read?.setBackgroundColor(resources.getColor(R.color.disabledBg))
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
            if (historyIndex > 0) {
                scp = historyList[historyIndex - 1]
                setData(scp)
                historyList.removeAt(historyIndex)
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
            tv_bottom_like?.text = if (it.like == 1) "取消收藏" else "收藏"
            tv_bottom_like?.setBackgroundColor(resources.getColor(if (it.like == 1)
                R.color.disabledBg else R.color.itemBg))
        }
        return super.onPrepareOptionsMenu(menu)
    }
}
