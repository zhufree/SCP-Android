package info.free.scp.view.detail

import android.app.AlertDialog
import android.content.*
import android.content.DialogInterface.BUTTON_POSITIVE
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.*
import android.view.Gravity.CENTER
import android.view.KeyEvent.KEYCODE_BACK
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import info.free.scp.BuildConfig
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.AppMode.OFFLINE
import info.free.scp.SCPConstants.SCP_SITE_URL
import info.free.scp.SCPConstants.SIMPLE
import info.free.scp.SCPConstants.TRADITIONAL
import info.free.scp.bean.ScpLikeBox
import info.free.scp.bean.ScpLikeModel
import info.free.scp.bean.ScpModel
import info.free.scp.data.detail.DetailLinkResolver
import info.free.scp.db.AppInfoDatabase
import info.free.scp.db.ScpDataHelper
import info.free.scp.db.ScpDatabase
import info.free.scp.service.HtmlParserService
import info.free.scp.util.EventUtil
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.PreferenceUtil.APP_SP
import info.free.scp.util.ThemeUtil
import info.free.scp.util.ThemeUtil.DAY_THEME
import info.free.scp.util.ThemeUtil.NIGHT_THEME
import info.free.scp.util.Utils
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.tag.TagDetailActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import taobe.tec.jcc.JChineseConvertor
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


class DetailActivity : BaseActivity() {

    private val scpDao = ScpDatabase.getInstance()?.scpDao()
    private var onlineMode = 0 // 0 离线 1 网页
    private var readType = 0 // 0 普通（按顺序） 1 随机 2 TODO 未读列表

    //    private var randomType = 0 // 0 所有，1仅scp，2 故事，3 joke
    private var randomLinkList = emptyList<String>()
    private var randomTitleList = emptyList<String>()
    private var url = ""
        set(value) {
            field = value
            fullUrl = DetailLinkResolver.toFullUrl(value)
        }
    private var title = ""
    private var index = 0
    private var scpType = 0

    private var scp: ScpModel? = null
    private var detailHtml = ""
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
            "$currentTextSize;}* {color:#aaa;} blockquote, .forum-category-box  .description-block, .content-panel, .scp-image-block .scp-image-caption, table.wiki-content-table th, #toc, #lock-info, .change-textarea-size a, .mobile-top-bar li ul li a, .mobile-top-bar li ul, .top-bar li ul, .description, .btn-default, div[style*=\"background:#F5F5F5;\"]{background:#333!important;} div[style*=\"background:#f2f2c2;\"]{background:#f2f2c22b!important;} div[style*=\"background:#d3d3d3;\"]{background:#d3d3d32b!important;}</style>"
    private var dayTextStyle = "<style>body{background-color:#fff;}p {font-size:$currentTextSize;}* {color:#000;}</style>"
    private val siteStyle = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />"
    private var currentTextStyle = siteStyle + (if (ThemeUtil.currentTheme == NIGHT_THEME) nightTextStyle else dayTextStyle)
    private val jqScript = "<script type=\"text/javascript\" src=\"jquery-ui.min.js\"></script>\n"
    private val initScript = "<script type=\"text/javascript\" src=\"init.combined.js\"></script>"
    private val tabScript = "<script type=\"text/javascript\" src=\"tabview-min.js\"></script>"
    private val wikiScript = "<script type=\"text/javascript\" src=\"WIKIDOT.combined.js\"></script>"
    private val darkReaderScript = "<script type=\"text/javascript\" src=\"darkreader.min.js\"></script><script>document.addEventListener('DOMContentLoaded', ()=>{DarkReader.enable({\n" +
            "    brightness: 100,\n" +
            "    contrast: 90,\n" +
            "    sepia: 10\n" +
            "})});</script>"
    // patched Darkreader to fix some bugs
    private val jsScript = (if (ThemeUtil.currentTheme == NIGHT_THEME) darkReaderScript else "") + "$jqScript$initScript$tabScript$wikiScript"
    private var screenHeight = 0
    private val historyList: MutableList<ScpModel> = emptyList<ScpModel>().toMutableList()

    private var randomIndex = 0
    private var historyIndex = 0
    private var fullUrl = ""
    private var forceOnline = false

    private var tvLoad: TextView? = null

    private val viewModel by lazy {
        ViewModelProvider(this)[DetailViewModel::class.java]
    }

    private lateinit var binding: DetailActivityLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DetailActivityLayout.create(this)
        setContentView(binding.root)
        tvLoad = TextView(this)
        tvLoad?.text = "评论加载中..."
        tvLoad?.gravity = CENTER
        tvLoad?.setTextColor(ThemeUtil.darkText)
        screenHeight = Utils.getScreenHeight(this)

        EventUtil.onEvent(this, EventUtil.clickReadDetail)
        binding.webView.setBackgroundColor(0) // 设置背景色
        binding.webView.background?.alpha = 0 // 设置填充透明度 范围：0-255
        binding.webView.setBackgroundColor(ThemeUtil.containerBg)
        binding.webView.settings.javaScriptEnabled = true

        url = intent.getStringExtra("link") ?: ""
        title = intent.getStringExtra("title") ?: ""
        index = intent.getIntExtra("index", 0)
        scpType = intent.getIntExtra("scp_type", 0)
        // 0 普通（按顺序） 1 随机 2 TODO 未读列表
        readType = intent.getIntExtra("read_type", 0)
        if (readType == 1) {
            randomIndex = intent.getIntExtra("random_index", 0)
            randomLinkList = intent?.getStringArrayListExtra("random_link_list") ?: emptyList()
            randomTitleList = intent?.getStringArrayListExtra("random_title_list") ?: emptyList()
        }
        forceOnline = intent.getBooleanExtra("forceOnline", false)

        // 有些不是以/开头的而是完整链接
        if (url.isEmpty()) {
            // 入口都确定了有url，没有的话直接finish
            finish()
        } else if (!forceOnline) {
            viewModel.setScp(url) // 设置scp
        } else {
            binding.pbLoading.visibility = VISIBLE
            binding.webView.loadUrl(fullUrl)
        }

        viewModel.getScp().observe(this, Observer {
            // 数据库取到
            if (it != null) {
                scp = it
                scp?.let { s ->
                    setData(s)
                } ?: run {
                    // 数据库没有，加载链接
                    binding.webView.loadUrl(fullUrl)
                    binding.nsvWebWrapper.scrollTo(0, 0)
                }
            }
        })

        binding.webView.requestFocus()

        //覆盖WebView默认通过第三方或系统浏览器打开网页的行为
        binding.webView.webViewClient = object : WebViewClient() {
            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView, requestUrl: String): Boolean {
                Log.i("scp-detail", requestUrl)
                Log.i("scp-detail", "onPageShouldOverrideUrlLoading")
                if (requestUrl.startsWith(SCP_SITE_URL) and requestUrl.contains("/html/")) {
                    return false
                }
                if (requestUrl.startsWith(SCP_SITE_URL)) {
                    return false
                }
                if (requestUrl.contains("player.bilibili.com")) {
                    return false
                }
                if (requestUrl == "https://www.wikidot.com/default--flow/login__LoginPopupScreen?originSiteId=530812&openerUri=https://scp-wiki-cn.wikidot.com/") {
                    // login page is not allowed for google play
                    if (BuildConfig.FLAVOR == "GooglePlayProduction") {
                        return false
                    }
                }

                if (onlineMode == 1) {
                    view.loadUrl(requestUrl)
                } else {
                    url = DetailLinkResolver.toLocalLink(requestUrl)
                    Log.i("scp-detail", url)
                    val tmpScp = scpDao?.getScpByLink(url)
                    tmpScp?.let {
                        scp = tmpScp
                        setData(tmpScp)
                    } ?: run {
                        binding.pbLoading.visibility = VISIBLE
                        view.loadUrl(fullUrl)
                    }
                }
                return false
            }


            override fun onPageCommitVisible(view: WebView?, url: String?) {
                binding.pbLoading.visibility = GONE
            }
            private fun readJavaScriptFromAsset(fileName: String): String {
                val inputStream = assets.open(fileName)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val script = StringBuilder()
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    script.append(line).append("\n")
                }

                reader.close()
                return script.toString()
            }
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.pbLoading.visibility = VISIBLE
                if (view != null && (onlineMode == 1 || (url != null && url.startsWith("http"))) && ThemeUtil.currentTheme == NIGHT_THEME) {
                    // add dark reader to online mode
                    val darkReaderScript = readJavaScriptFromAsset("darkreader.min.js") + ""
                    view.evaluateJavascript(darkReaderScript, null)
                    // make dark reader work
                    view.evaluateJavascript("window.DarkReader.setFetchMethod((url) => {\n" +
                            "            return new Promise((resolve, reject) => {\n" +
                            "                const corsProxyUrl = \"https://cors-proxy.fringe.zone/\";\n" +
                            "                const proxiedUrl = corsProxyUrl + url;\n" +
                            "\n" +
                            "                // Now, use the proxied URL to fetch\n" +
                            "                window.fetch(proxiedUrl)\n" +
                            "                    .then(response => response.text())\n" +
                            "                    .then((responseText) => {\n" +
                            "                        resolve(new Response(responseText, {status: 200, statusText: \"OK\"}));\n" +
                            "                    })\n" +
                            "                    .catch((err) => {\n" +
                            "                        reject(err);\n" +
                            "                    });\n" +
                            "            });\n" +
                            "        });" +
                            "function runAfterDOMContentLoaded(callback) {\n" +
                            "    if (document.readyState === \"loading\") {\n" +
                                    "try{callback()}catch{}"+
                                    "document.addEventListener(\"DOMContentLoaded\", callback);\n" +
                                    "document.addEventListener(\"DOMContentLoaded\", ()=>{setTimeout(callback, 1000)});\n" +
                            "    } else {\n" +
                            "        callback();\n" +
                            "    }\n" +
                            "}\n" +
                            "\n" +
                            "runAfterDOMContentLoaded(() => {\n" +
                                "window.DarkReader.enable({ brightness: 100, contrast: 90, sepia: 10 });" +
                            "});", null)
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                binding.pbLoading.visibility = GONE
                if ((view != null && url !== null) && (url.contains("wikidot.com"))) {
                    val cookies = CookieManager.getInstance().getCookie(url)
                    PreferenceUtil.setCookie(cookies)
                    PreferenceUtil.setAgent(view.settings.userAgentString)
                }
                Log.i("scp-detail", "webView.height = ${binding.webView.height}")
                Log.i("scp-detail", "nsv_web_wrapper.height = ${binding.nsvWebWrapper.height}")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!PreferenceUtil.getShownDetailNotice()) {
            AlertDialog.Builder(this)
                    .setTitle("Notice")
                    .setMessage("！！！升级到0.2.6版本后请务必确认数据库也更新到新版本，使用旧版本数据库会导致正文页面闪退\n" +
                            "1.右上角菜单可以切换网络阅读和离线阅读模式（如果本地数据没有加载完成则离线模式可能不可用）\n" +
                            "2.所有显示尚无内容的即表示在网上是404状态，即禁止访问，可能存在数据更新不及时的情况，所以也可以切换阅读模式看原网页\n" +
                            "3.文档数量较多，如果发现有疏漏，如文不对题等，可右上角菜单选择反馈问题\n" +
                            "4.图片依然需要网络才能显示出来，另有一些网页上依赖代码等复杂的文章，请切换跳转网页查看\n")
                    .setPositiveButton("OK") { dialog, _ ->
                        PreferenceUtil.setShownDetailNotice()
                        dialog.dismiss()
                    }
                    .create().show()
        }

        var isMoving = false
        val screenHeight = Utils.getScreenHeight(this@DetailActivity)

        binding.sbDetail.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.nsvWebWrapper.scrollTo(
                        0,
                        ((binding.webView.height - screenHeight) * (progress / 100f)).toInt()
                    )
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isMoving = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isMoving = false
            }
        })
        binding.nsvWebWrapper.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            if (!isMoving) {
                binding.sbDetail.progress =
                    ((scrollY.toFloat() / (binding.webView.height - screenHeight) * 100)).toInt()
            }
        }

        initToolbar()
        initSwitchBtn()
        refreshReadBtnStatus()
    }

    private fun setDetail(detail: String) {
        detailHtml = detail
        // 显示frame
        if (!detailHtml.contains("""<iframe src="//player.bilibili.com""")) {
            detailHtml = detailHtml.replace("""<iframe src="/""", """<iframe src="http://scp-wiki-cn.wikidot.com/""")
        }
        detailHtml = detailHtml.replace("html-block-iframe", "")

        if (detailHtml.isEmpty()) {
            binding.pbLoading.visibility = VISIBLE
            binding.webView.loadUrl(fullUrl)
        } else {
            binding.pbLoading.visibility = GONE
            binding.webView.loadDataWithBaseURL(
                "file:///android_asset/", currentTextStyle
                        + detailHtml + jsScript,
                "text/html", "utf-8", null
            )
        }
        binding.nsvWebWrapper.scrollTo(0, 0)
//        translate(PreferenceUtil.getTraditionalText())
        binding.btnComment.show()
        binding.llCommentContainer.removeAllViews()
        binding.llCommentContainer.visibility = GONE
        binding.llCommentContainer.addView(
            tvLoad,
            ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        )
    }

    override fun refreshTheme() {
        super.refreshTheme()
        binding.clDetailContainer.setBackgroundColor(ThemeUtil.containerBg)
        refreshStyle()
        refreshReadBtnStatus()
        refreshButtonStyle()
        binding.llCommentContainer.children.forEach {
            if (it is CommentLayout) {
                it.refreshTheme()
            } else {
                (it as TextView).setTextColor(ThemeUtil.darkText)
            }
        }
    }

    /**
     * 不改变网页内容，只刷新样式
     */
    private fun refreshStyle() {
        currentTextStyle = siteStyle + (if (ThemeUtil.currentTheme == NIGHT_THEME) nightTextStyle else dayTextStyle)
        binding.webView.loadDataWithBaseURL(
            "file:///android_asset/", currentTextStyle
                    + detailHtml + jsScript,
            "text/html", "utf-8", null
        )
    }

    private fun setData(s: ScpModel, back: Boolean = false) {
        tvLoad?.text = "评论加载中..."
        viewModel.setScpReadInfo() // scp拿到之后，设置已读数据和拿like数据
        viewModel.getScpLikeInfo()?.observe(this, Observer { scpInfo ->
            if (scpInfo == null) {
                viewModel.setScpLikeInfo() // like数据拿到以后，进行初始化
            }
            invalidateOptionsMenu()
        })
        // 刷新toolbar（收藏状态
        invalidateOptionsMenu()
        // 更新标题
        supportActionBar?.setDisplayShowTitleEnabled(false)
        if (!back) {
            historyList.removeAll {
                historyList.map { it.link }.contains(s.link)
            }
            historyList.add(s)
            historyIndex = historyList.size - 1
        }

//        if (newRandom) {
//            randomList.add(s)
//            randomIndex++
//            newRandom = false
//        }
        binding.tvDetailToolbar.text = s.title
        binding.tvDetailToolbar.isSelected = true
        url = s.link
        refreshReadBtnStatus()
        viewModel.loadDetail(url)
        if (PreferenceUtil.getAppMode() == OFFLINE) {
            viewModel.getOfflineDetail().observe(this) { detail ->
                if (!detail.isNullOrEmpty()) {
                    setDetail(detail)
                }
            }
            viewModel.getOfflineTag().observe(this) { tag ->
                if (!tag.isNullOrEmpty()) {
                    showTag(tag)
                }
            }
        } else {
            viewModel.getDetail().observe(this) { detail ->
                if (!detail.isNullOrEmpty()) {
                    setDetail(detail)
                }
            }
            viewModel.getTag().observe(this) { tag ->
                if (!tag.isNullOrEmpty()) {
                    showTag(tag)
                }
            }
        }
    }

    private fun showTag(tagString: String) {
        val tags = tagString.split(",")
        val spannableString = SpannableString(tagString)
        tags.forEach { t ->
            val foregroundColorSpan = ForegroundColorSpan(Color.parseColor("#FF0000"))
            val tagIndex = tagString.indexOf(t)
            val clickableSpan: ClickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    startActivity(
                        Intent(this@DetailActivity, TagDetailActivity::class.java)
                            .putExtra("tag", t)
                    )
                }
            }
            spannableString.setSpan(
                foregroundColorSpan,
                tagIndex,
                tagIndex + t.length,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )
            spannableString.setSpan(
                clickableSpan,
                tagIndex,
                tagIndex + t.length,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
        binding.tvTagContainer.movementMethod = LinkMovementMethod.getInstance()
        binding.tvTagContainer.text = spannableString
    }

    private fun initToolbar() {
        baseToolbar = binding.detailToolbar
        supportActionBar?.title = null
        populateDetailMenu(binding.detailToolbar.menu)
        binding.detailToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.switch_read_mode -> {
                    PreferenceUtil.addPoints(1)
                    if (onlineMode == 0) {
                        if (Utils.enabledNetwork(this)) {
                            binding.pbLoading.visibility = VISIBLE
                            onlineMode = 1
                            it.setTitle(R.string.offline_mode)
                            binding.webView.loadUrl(fullUrl)
                        } else {
                            showToast("请先开启网络")
                        }
                    } else {
                        onlineMode = 0
                        it.setTitle(R.string.online_mode)
                        binding.webView.loadDataWithBaseURL(
                            "file:///android_asset/",
                            currentTextStyle + detailHtml + jsScript,
                            "text/html", "utf-8", null
                        )
                    }
                }
                R.id.open_in_browser -> {
                    scp?.let { s->
                        EventUtil.onEvent(this, EventUtil.clickOpenInBrowser, s.link)
                    }
                    PreferenceUtil.addPoints(1)
                    startActivity(Utils.getUrlIntent(fullUrl))
                }
                R.id.copy_link -> {
                    scp?.let {s->
                        EventUtil.onEvent(this, EventUtil.clickCopyLink, s.link)
                    }
                    val clipboardManager =
                        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                    val clipData = ClipData.newPlainText("scp_link", fullUrl)
                    clipboardManager?.setPrimaryClip(clipData)
                    showToast("已复制到剪贴板")
                }
                R.id.like -> {
                    likeScp()
                }
                R.id.change_theme -> {
                    it.setTitle(if (ThemeUtil.currentTheme == DAY_THEME) R.string.day_mode else R.string.dark_mode)
                    changeTheme(if (ThemeUtil.currentTheme == DAY_THEME) NIGHT_THEME else DAY_THEME)
                }
                R.id.add_read_later -> {
                    scp?.let { s->
                        ScpDataHelper.getInstance().insertViewListItem(s.link, s.title,
                            SCPConstants.LATER_TYPE)
                        showToast("已加入待读列表")
                    }?:run {
                        showToast("本文只可在线浏览，不可加入待读")
                    }
                }
                R.id.share_picture -> {
                    // 截屏分享
                    scp?.let { s ->
                        EventUtil.onEvent(this, EventUtil.clickShareByPicture, s.link)
                    }
                    showToast("生成图片中...")
                    binding.gpShareContent.visibility = VISIBLE
                    binding.clDetailContainer.viewTreeObserver?.addOnGlobalLayoutListener(object :
                        ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            binding.clDetailContainer.viewTreeObserver?.removeOnGlobalLayoutListener(
                                this
                            )
                            val bitmap = Bitmap.createBitmap(
                                binding.webView.width, binding.clDetailContainer.height,
                                Bitmap.Config.RGB_565
                            )
                            //使用Canvas，调用自定义view控件的onDraw方法，绘制图片
                            val canvas = Canvas(bitmap)
                            binding.clDetailContainer.draw(canvas)
                            lifecycleScope.launch(Dispatchers.IO) {
                                Utils.saveBitmapFile(bitmap, scp?.title?.replace(" ", "") ?: "")
                                withContext(Dispatchers.Main) {
                                    showToast("图片已保存")
                                    binding.gpShareContent.visibility = GONE
                                }
                            }
                        }
                    })
                }

                R.id.set_cookie -> {
                    showCookieDialog()
                }
                else -> {
                }
            }
            true
        }
    }

    private fun populateDetailMenu(menu: Menu?) {
        menu ?: return
        menu.clear()
        menu.add(Menu.NONE, R.id.like, 0, R.string.menu_like).apply {
            setIcon(R.drawable.ic_star_border_white_24dp)
            setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        }
        menu.add(Menu.NONE, R.id.change_theme, 1, R.string.dark_mode)
        menu.add(Menu.NONE, R.id.add_read_later, 2, R.string.menu_add_read_later)
        menu.add(Menu.NONE, R.id.switch_read_mode, 3, R.string.online_mode)
        menu.add(Menu.NONE, R.id.open_in_browser, 4, R.string.menu_open_in_browser)
        menu.add(Menu.NONE, R.id.copy_link, 5, R.string.menu_copy_link)
        menu.add(Menu.NONE, R.id.share_picture, 6, R.string.share_with_picture)
        menu.add(Menu.NONE, R.id.set_cookie, 7, R.string.set_cookie)
    }

    private fun showCookieDialog() {
        val margin = Utils.dp2px(16, this)
        val container = LinearLayout(this).apply {
            orientation = VERTICAL
        }
        val cookieInput = EditText(this).apply {
            hint = "cookie"
            setSingleLine(false)
            setText(PreferenceUtil.getCookie())
        }
        val agentInput = EditText(this).apply {
            hint = "agent"
            setSingleLine(false)
            setText(PreferenceUtil.getAgent())
        }
        val loginButton = Button(this).apply {
            text = "\u4f7f\u7528\u7f51\u9875\u767b\u5f55"
        }
        val inputLp = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            setMargins(margin, margin, margin, 0)
        }
        container.addView(cookieInput, inputLp)
        container.addView(agentInput, inputLp)
        container.addView(
            loginButton,
            LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                setMargins(margin, margin, margin, margin)
            }
        )

        val cookieDialog = AlertDialog.Builder(this)
            .setTitle("\u8bbe\u7f6eCookie")
            .setView(container)
            .setNeutralButton("\u5982\u4f55\u83b7\u53d6cookie\u548cagent") { _, _ ->
                startActivity(Utils.getUrlIntent("https://mbd.pub/o/bread/YZicl55u"))
            }
            .setPositiveButton("OK") { _, _ -> }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()

        cookieDialog.show()

        loginButton.setOnClickListener {
            val loginURL = "https://scp-wiki-cn.wikidot.com/scp-7616"
            showToast("\u6253\u5f00\u4e86\u767b\u5f55\u9875\u9762\uff0c\u4e0d\u5de5\u4f5c\u7684\u8bdd\u53ef\u4ee5\u70b9\u51fb\u201c\u5207\u6362\u7f51\u9875\u6a21\u5f0f\u201d\u624b\u52a8\u767b\u5f55")
            binding.webView.loadUrl(loginURL)
            cookieDialog.dismiss()
        }
        cookieDialog.getButton(BUTTON_POSITIVE).setOnClickListener {
            PreferenceUtil.setCookie(cookieInput.text.toString())
            PreferenceUtil.setAgent(agentInput.text.toString())
            showToast("\u8bbe\u7f6e\u5b8c\u6210")
            cookieDialog.dismiss()
        }
    }

    private fun changeTheme(mode: Int) {
        ThemeUtil.changeTheme(this, mode)
    }


    /**
     * 繁简转换
     */
    private fun translate(translateType: Int) {
        try {
            val converter = JChineseConvertor.getInstance()
            detailHtml = if (translateType == SIMPLE) converter.t2s(detailHtml) else converter.s2t(detailHtml)
            binding.webView.loadDataWithBaseURL(
                "file:///android_asset/", currentTextStyle
                        + detailHtml + jsScript,
                "text/html", "utf-8", null
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 先get or create一个scpInfo信息
     * 未收藏
     * 已收藏
     */
    private fun likeScp() {
        val scpInfo = viewModel.getScpLikeInfo()?.value ?: return
        val likeDao = AppInfoDatabase.getInstance().likeAndReadDao()
        if (!scpInfo.like) {
            // 未收藏
            // 获取数据库中的收藏夹
            val boxList = arrayListOf<ScpLikeBox>()
            boxList.addAll(likeDao.getLikeBox())
            if (boxList.isEmpty()) {
                val defaultBox = ScpLikeBox(0, "默认收藏夹")
                boxList.add(defaultBox)
                likeDao.saveLikeBox(defaultBox)
            }
            val nameList = arrayListOf<String>()
            nameList.addAll(boxList.map { it.name })
            nameList.add("新建收藏夹")
            // 显示收藏夹列表和新建收藏夹选项
            AlertDialog.Builder(this)
                .setTitle("加入收藏夹")
                .setItems(nameList.toTypedArray()) { _, i ->
                    if (i == boxList.size) {
                        createNewBox()
                    } else {
                        PreferenceUtil.addPoints(2)
                        scpInfo.boxId = boxList[i].id
                        scpInfo.like = true
                        viewModel.likeScp(scpInfo)
                    }
                }
                .show()
        } else {
            scpInfo.like = false
            viewModel.likeScp(scpInfo)
        }
    }

    private fun createNewBox() {
        val container = LinearLayout(this).apply {
            orientation = VERTICAL
            setPadding(
                Utils.dp2px(16, this@DetailActivity),
                Utils.dp2px(16, this@DetailActivity),
                Utils.dp2px(16, this@DetailActivity),
                Utils.dp2px(16, this@DetailActivity)
            )
        }
        val titleView = TextView(this).apply {
            text = "输入收藏夹标题"
            setTextColor(ThemeUtil.darkText)
            textSize = 18f
        }
        val input = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            isSingleLine = true
        }
        container.addView(titleView)
        container.addView(input)

        AlertDialog.Builder(this)
            .setView(container)
            .setPositiveButton("确定") { _, _ ->
                input.clearFocus()
                val defaultBox = ScpLikeBox(0, input.text?.toString() ?: "")
                Log.i("scp-detail", defaultBox.toString())
                AppInfoDatabase.getInstance().likeAndReadDao().saveLikeBox(defaultBox)
                likeScp()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun toNextArticle() {
        if (scpDao == null) return
        index = if (scp?.index != -1) scp?.index ?: 0 else index
        scpType = if (scp?.scpType != -1) scp?.scpType ?: 0 else scpType
        PreferenceUtil.addPoints(1)
        when (readType) {
            0 -> {
                scp = scpDao.getNextScp(index, scpType)
                scp?.let {
                    viewModel.setScp(it.link)
                } ?: showToast("已经是最后一篇了")
            }
            1 -> {// 随机模式
                // 下一篇
                if (randomIndex + 1 in randomLinkList.indices) {
                    randomIndex += 1
                    val nextLink = randomLinkList[randomIndex]
                    viewModel.setScp(nextLink) // TODO
                } else {
                    showToast("已经是最后一篇了")
                }
            }
            else -> {
            }
        }
    }


    private fun toPreviewArticle() {
        if (scpDao == null) return
        index = if (scp?.index != -1) scp?.index ?: 0 else index
        scpType = if (scp?.scpType != -1) scp?.scpType ?: 0 else scpType
        PreferenceUtil.addPoints(1)
        when (readType) {
            0 -> {
                when (index) {
                    0 -> showToast("已经是第一篇了")
                    else -> {
                        scp = scpDao.getPreviewScp(index, scpType)
                        scp?.let {
                            setData(it)
                        }
                    }
                }
            }
            1 -> { // random
                if (randomIndex - 1 >= 0) {
                    randomIndex -= 1
                    val prevLink = randomLinkList[randomIndex]
                    viewModel.setScp(prevLink) // TODO
                } else {
                    showToast("已经是第一篇了")
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
                scpInfo = ScpLikeModel(s.link, s.title, like = false, hasRead = false, boxId = 0)
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
        readBtnLp = binding.tvBottomSetHasRead.layoutParams as ConstraintLayout.LayoutParams?
        if (hasRead) {
            binding.tvBottomSetHasRead.setText(R.string.set_has_not_read)
            binding.tvBottomSetHasRead.setTextColor(ThemeUtil.mediumText)
            binding.tvBottomSetHasRead.background = ThemeUtil.customShape(
                ThemeUtil.disabledBg, ThemeUtil.disabledBg, 0, Utils.dp2px(15, this)
            )
            readBtnLp?.endToEnd = -1
            readBtnLp?.startToStart = -1
            readBtnLp?.endToStart = R.id.gl_detail_center
            binding.tvBottomSetHasRead.layoutParams = readBtnLp
            binding.tvBottomLike.visibility = VISIBLE
        } else {
            binding.tvBottomSetHasRead.setText(R.string.set_has_read)
            binding.tvBottomSetHasRead.setTextColor(ThemeUtil.darkText)
            binding.tvBottomSetHasRead.background = ThemeUtil.customShape(
                ThemeUtil.itemBg, ThemeUtil.itemBg, 0, Utils.dp2px(15, this)
            )
            readBtnLp?.endToEnd = 0
            readBtnLp?.startToStart = 0
            readBtnLp?.endToStart = -1
            binding.tvBottomSetHasRead.layoutParams = readBtnLp
            binding.tvBottomLike.visibility = GONE
        }
    }

    private fun initSwitchBtn() {
        refreshButtonStyle()
        binding.tvBottomPreview.setOnClickListener {
            toPreviewArticle()
        }

        binding.tvBottomNext.setOnClickListener {
            toNextArticle()
        }

        binding.tvBottomSetHasRead.setOnClickListener {
            setHasRead()
        }

        binding.tvBottomLike.setOnClickListener { likeScp() }

        viewModel.repo.commentList.observe(this, Observer {
            if (it.isEmpty()) {
                if (HtmlParserService.noComment) {
                    tvLoad?.text = "这篇文档没有评论"
                } else {
                    tvLoad?.text = "因中分官网要求登录才能查看评论，暂时无法获取"
                }
            } else {
                binding.llCommentContainer.removeAllViews()
                val commentLp = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                commentLp.topMargin = Utils.dp2px(4)
                it.forEach { c ->
                    val newComment = CommentLayout(this)
                    newComment.setData(c)
                    binding.llCommentContainer.addView(newComment, commentLp)
                }
            }
        })
        binding.btnComment.setOnClickListener {
            if (PreferenceUtil.getCookie().isEmpty()) {
                showToast("先点击右上角菜单设置 cookie 才能加载评论")
            } else {
                binding.llCommentContainer.visibility = VISIBLE
                binding.nsvWebWrapper.scrollTo(0, (binding.webView.height - screenHeight) + 100)
                binding.btnComment.hide()
                viewModel.loadComment(url)
            }
        }
    }

    private fun refreshButtonStyle() {
        binding.tvBottomPreview.background = ThemeUtil.customShape(
            ThemeUtil.itemBg, ThemeUtil.itemBg, 0, Utils.dp2px(15, this)
        )
        binding.tvBottomNext.background = ThemeUtil.customShape(
            ThemeUtil.itemBg, ThemeUtil.itemBg, 0, Utils.dp2px(15, this)
        )
        binding.tvBottomLike.setTextColor(ThemeUtil.darkText)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KEYCODE_BACK && historyIndex > 0) {
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
        populateDetailMenu(menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val scpInfo = AppInfoDatabase.getInstance().likeAndReadDao().getInfoByLink(url)
        val menuItem = menu?.getItem(0)
        if (scpInfo == null || !scpInfo.like) {
            menuItem?.setIcon(R.drawable.ic_star_border_white_24dp)
            binding.tvBottomLike.text = "收藏"
            binding.tvBottomLike.background = ThemeUtil.customShape(
                ThemeUtil.itemBg, ThemeUtil.itemBg, 0, Utils.dp2px(15, this)
            )
        } else {
            menuItem?.setIcon(R.drawable.ic_star_white_24dp)
            binding.tvBottomLike.text = "取消收藏"
            binding.tvBottomLike.background = ThemeUtil.customShape(
                ThemeUtil.disabledBg, ThemeUtil.disabledBg, 0, Utils.dp2px(15, this)
            )
        }
        return super.onPrepareOptionsMenu(menu)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
