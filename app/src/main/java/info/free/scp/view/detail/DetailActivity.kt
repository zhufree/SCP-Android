package info.free.scp.view.detail

import android.content.*
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
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import info.free.scp.BuildConfig
import info.free.scp.R
import info.free.scp.SCPConstants.SCP_SITE_URL
import info.free.scp.SCPConstants.SIMPLE
import info.free.scp.SCPConstants.TRADITIONAL
import info.free.scp.bean.ScpModel
import info.free.scp.data.detail.DetailHtmlComposer
import info.free.scp.data.detail.DetailLinkResolver
import info.free.scp.data.detail.DetailTitleFormatter
import info.free.scp.data.settings.DetailSettingsStore
import info.free.scp.data.settings.PreferenceDetailSettingsStore
import info.free.scp.service.HtmlParserService
import info.free.scp.ui.MainTheme
import info.free.scp.ui.detail.DetailMenuAction
import info.free.scp.ui.detail.DetailToolbarActions
import info.free.scp.util.EventUtil
import info.free.scp.util.ScpDialogs
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

    private val detailSettings: DetailSettingsStore = PreferenceDetailSettingsStore()
    private var onlineMode by mutableIntStateOf(0) // 0 离线 1 网页；菜单文案读取它，所以是 Compose 状态
    private var liked by mutableStateOf(false)
    private var readType = 0 // 0 普通（按顺序） 1 随机 2 TODO 未读列表

    //    private var randomType = 0 // 0 所有，1仅scp，2 故事，3 joke
    private var randomLinkList = emptyList<String>()
    private var randomTitleList = emptyList<String>()
    private var url = ""
        set(value) {
            field = DetailLinkResolver.toLocalLink(value)
            fullUrl = DetailLinkResolver.toFullUrl(value)
        }
    private var title = ""
    private var index = 0
    private var scpType = 0

    private var scp: ScpModel? = null
    private var detailHtml = ""
    private val htmlComposer = DetailHtmlComposer(
        textSize = detailSettings.detailTextSize,
        initialNightTheme = ThemeUtil.currentTheme == NIGHT_THEME
    )
    private var screenHeight = 0
    private val historyList: MutableList<ScpModel> = emptyList<ScpModel>().toMutableList()

    private var randomIndex = 0
    private var historyIndex = 0
    private var fullUrl = ""
    private var forceOnline = false
    private var renderedArticle = DetailUiState()
    private var historyBack = false

    private var tvLoad: TextView? = null

    private val viewModel by lazy {
        ViewModelProvider(this)[DetailViewModel::class.java]
    }
    private val cookieDialog by lazy { DetailCookieDialog(this) }

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

        val initialLink = intent.getStringExtra("link") ?: intent.dataString.orEmpty()
        url = initialLink
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
        configureWebViewClient()
        if (initialLink.isEmpty()) {
            // 入口都确定了有url，没有的话直接finish
            finish()
        } else if (!forceOnline) {
            if (viewModel.uiState.value?.articleKey.isNullOrEmpty()) viewModel.setScp(initialLink)
        } else {
            binding.pbLoading.visibility = VISIBLE
            binding.webView.loadUrl(fullUrl)
        }

        observeUiState()

        binding.webView.requestFocus()

    }

    private fun observeUiState() {
        viewModel.uiState.observe(this) { state ->
            val previous = renderedArticle
            renderedArticle = state
            if (forceOnline) {
                if (state.comments != previous.comments) state.comments?.let(::showComments)
                return@observe
            }
            val changedArticle = state.articleKey != previous.articleKey
            if (changedArticle) {
                url = state.requestedLink
                title = state.document?.title ?: state.articleKey
                binding.tvDetailToolbar.text = title
                scp = null
                detailHtml = ""
                onlineMode = 0
                binding.webView.stopLoading()
                binding.webView.loadUrl("about:blank")
                binding.nsvWebWrapper.scrollTo(0, 0)
                binding.tvTagContainer.text = ""
                binding.btnComment.hide()
                binding.llCommentContainer.visibility = GONE
                binding.llCommentContainer.removeAllViews()
                binding.pbLoading.visibility = VISIBLE
                refreshLikeState()
                refreshReadBtnStatus()
            }
            if (changedArticle || state.document != previous.document) {
                scp = state.document
                state.document?.let {
                    setData(it, historyBack)
                    historyBack = false
                }
            }
            if (changedArticle || state.body != previous.body) {
                when (val body = state.body) {
                    ArticleBody.Loading -> Unit
                    ArticleBody.Website -> {
                        onlineMode = 1
                        binding.webView.loadUrl(fullUrl)
                    }
                    is ArticleBody.Content -> {
                        setDetail(body.html)
                        body.tags?.takeIf(String::isNotBlank)?.let(::showTag)
                    }
                    is ArticleBody.Message -> showArticleMessage(body.text)
                }
            }
            if (state.comments != previous.comments) state.comments?.let(::showComments)
        }
    }

    private var articleMessage: String? = null

    private fun cssColor(color: Int) = String.format("#%06X", color and 0xFFFFFF)

    private fun showArticleMessage(message: String) {
        articleMessage = message
        binding.pbLoading.visibility = GONE
        // 消息页的背景和文字色跟随 ThemeUtil，避免和页面底色出现色差
        val style = "<style>body{background-color:${cssColor(ThemeUtil.containerBg)};}" +
            "* {color:${cssColor(ThemeUtil.darkText)};}</style>"
        binding.webView.loadDataWithBaseURL(null, "$style<p>$message</p>", "text/html", "utf-8", null)
    }

    private fun configureWebViewClient() {
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

                if (onlineMode == 1 || forceOnline) return false
                viewModel.setScp(requestUrl)
                return true
            }

            override fun onPageCommitVisible(view: WebView?, url: String?) {
                if (url != "about:blank") binding.pbLoading.visibility = GONE
            }

            override fun onReceivedError(
                view: WebView, request: WebResourceRequest, error: WebResourceError
            ) {
                if (request.isForMainFrame && request.url.toString() == view.url) {
                    showArticleMessage("在线正文加载失败，请检查网络后重试")
                }
            }

            override fun onReceivedHttpError(
                view: WebView, request: WebResourceRequest, response: WebResourceResponse
            ) {
                if (request.isForMainFrame && request.url.toString() == view.url && response.statusCode == 404) {
                    showArticleMessage("文章不存在（404）")
                }
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
                if (url != "about:blank") binding.pbLoading.visibility = VISIBLE
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
                if (url != "about:blank") binding.pbLoading.visibility = GONE
                if ((view != null && url !== null) && (url.contains("wikidot.com"))) {
                    val cookies = CookieManager.getInstance().getCookie(url)
                    detailSettings.updateWebCredentials(cookies, view.settings.userAgentString)
                }
                Log.i("scp-detail", "webView.height = ${binding.webView.height}")
                Log.i("scp-detail", "nsv_web_wrapper.height = ${binding.nsvWebWrapper.height}")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (detailSettings.shouldShowDetailNotice) {
            ScpDialogs.builder(this)
                    .setTitle("Notice")
                    .setMessage("！！！升级到0.2.6版本后请务必确认数据库也更新到新版本，使用旧版本数据库会导致正文页面闪退\n" +
                            "1.右上角菜单可以切换网络阅读和离线阅读模式（如果本地数据没有加载完成则离线模式可能不可用）\n" +
                            "2.所有显示尚无内容的即表示在网上是404状态，即禁止访问，可能存在数据更新不及时的情况，所以也可以切换阅读模式看原网页\n" +
                            "3.文档数量较多，如果发现有疏漏，如文不对题等，可右上角菜单选择反馈问题\n" +
                            "4.图片依然需要网络才能显示出来，另有一些网页上依赖代码等复杂的文章，请切换跳转网页查看\n")
                    .setPositiveButton("OK") { dialog, _ ->
                        detailSettings.markDetailNoticeShown()
                        dialog.dismiss()
                    }
                    .show()
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
        if (detailHtml.isNotEmpty() && detailSettings.hanzType == TRADITIONAL) {
            detailHtml = toTraditional(detailHtml)
        }

        if (detailHtml.isEmpty()) {
            binding.pbLoading.visibility = VISIBLE
            binding.webView.loadUrl(fullUrl)
        } else {
            binding.pbLoading.visibility = GONE
            binding.webView.loadDataWithBaseURL(
                "file:///android_asset/", composeOfflineHtml(),
                "text/html", "utf-8", null
            )
        }
        binding.nsvWebWrapper.scrollTo(0, 0)
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
        binding.root.setBackgroundColor(ThemeUtil.containerBg)
        binding.clDetailContainer.setBackgroundColor(ThemeUtil.containerBg)
        binding.tvDetailToolbar.setTextColor(ThemeUtil.onToolbar)
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
        articleMessage?.let {
            if (detailHtml.isEmpty()) {
                showArticleMessage(it)
                return
            }
        }
        if (detailHtml.isEmpty() || onlineMode == 1) return
        binding.webView.loadDataWithBaseURL(
            "file:///android_asset/", composeOfflineHtml(),
            "text/html", "utf-8", null
        )
    }

    private fun setData(s: ScpModel, back: Boolean = false) {
        tvLoad?.text = "评论加载中..."
        viewModel.recordOpened(s.link, s.title)
        // 刷新toolbar（收藏状态
        refreshLikeState()
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
        title = s.title
        refreshReadBtnStatus()
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
        val actions = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MainTheme {
                    DetailToolbarActions(
                        liked = liked,
                        webMode = onlineMode == 1,
                        onAction = ::handleMenuAction
                    )
                }
            }
        }
        binding.detailToolbar.addView(
            actions,
            Toolbar.LayoutParams(WRAP_CONTENT, MATCH_PARENT).apply { gravity = Gravity.END }
        )
        refreshLikeState()
    }

    private fun handleMenuAction(action: DetailMenuAction) {
        when (action) {
            DetailMenuAction.SWITCH_READ_MODE -> {
                viewModel.rewardReadModeSwitch()
                if (onlineMode == 0) {
                    if (Utils.enabledNetwork(this)) {
                        binding.pbLoading.visibility = VISIBLE
                        onlineMode = 1
                        binding.webView.loadUrl(fullUrl)
                    } else {
                        showToast("请先开启网络")
                    }
                } else if (detailHtml.isNotEmpty()) {
                    onlineMode = 0
                    refreshStyle()
                } else {
                    showToast("当前文章没有可切换的本地正文")
                }
            }
            DetailMenuAction.OPEN_IN_BROWSER -> {
                scp?.let { s->
                    EventUtil.onEvent(this, EventUtil.clickOpenInBrowser, s.link)
                }
                viewModel.rewardExternalOpen()
                startActivity(Utils.getUrlIntent(fullUrl))
            }
            DetailMenuAction.COPY_LINK -> {
                scp?.let { s ->
                    EventUtil.onEvent(this, EventUtil.clickCopyLink, s.link)
                }
                val clipboardManager =
                    getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                val clipData = ClipData.newPlainText("scp_link", fullUrl)
                clipboardManager?.setPrimaryClip(clipData)
                showToast("已复制到剪贴板")
            }
            DetailMenuAction.TOGGLE_LIKE -> {
                likeScp()
            }
            DetailMenuAction.CHANGE_THEME -> {
                changeTheme(if (ThemeUtil.currentTheme == DAY_THEME) NIGHT_THEME else DAY_THEME)
            }
            DetailMenuAction.ADD_READ_LATER -> {
                scp?.let { s->
                    viewModel.addToLater(s.link, s.title)
                    showToast("已加入待读列表")
                }?:run {
                    showToast("本文只可在线浏览，不可加入待读")
                }
            }
            DetailMenuAction.SHARE_PICTURE -> {
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
                            val fileName = DetailTitleFormatter.toScreenshotFileName(
                                scp?.title.orEmpty()
                            )
                            Utils.saveBitmapFile(bitmap, fileName)
                            withContext(Dispatchers.Main) {
                                showToast("图片已保存")
                                binding.gpShareContent.visibility = GONE
                            }
                        }
                    }
                })
            }

            DetailMenuAction.SET_COOKIE -> {
                showCookieDialog()
            }
        }
    }

    private fun showCookieDialog() {
        cookieDialog.show(
            credentials = detailSettings.webCredentials,
            onOpenHelp = {
                startActivity(Utils.getUrlIntent("https://mbd.pub/o/bread/YZicl55u"))
            },
            onOpenLogin = {
                showToast("\u6253\u5f00\u4e86\u767b\u5f55\u9875\u9762\uff0c\u4e0d\u5de5\u4f5c\u7684\u8bdd\u53ef\u4ee5\u70b9\u51fb\u201c\u5207\u6362\u7f51\u9875\u6a21\u5f0f\u201d\u624b\u52a8\u767b\u5f55")
                binding.webView.loadUrl("https://scp-wiki-cn.wikidot.com/scp-7616")
            },
            onSave = { cookie, userAgent ->
                detailSettings.updateWebCredentials(cookie, userAgent)
                showToast("\u8bbe\u7f6e\u5b8c\u6210")
            }
        )
    }

    private fun changeTheme(mode: Int) {
        ThemeUtil.changeTheme(this, mode)
    }


    /** 设置里选了“繁体”时转换正文；转换库读取失败则保持简体原文，不影响阅读。 */
    private fun toTraditional(html: String): String = try {
        JChineseConvertor.getInstance().s2t(html)
    } catch (e: Exception) {
        Log.w("scp", "s2t failed: ${e.javaClass.simpleName}")
        html
    }

    /**
     * 繁简转换
     */
    private fun translate(translateType: Int) {
        if (detailHtml.isEmpty() || onlineMode == 1) {
            showToast("当前没有可转换的正文")
            return
        }
        try {
            val converter = JChineseConvertor.getInstance()
            detailHtml = if (translateType == SIMPLE) converter.t2s(detailHtml) else converter.s2t(detailHtml)
            binding.webView.loadDataWithBaseURL(
                "file:///android_asset/", composeOfflineHtml(),
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
        val currentScp = scp ?: return
        val scpInfo = viewModel.getLikeInfo(currentScp.link, currentScp.title)
        if (!scpInfo.like) {
            // 未收藏
            // 获取数据库中的收藏夹
            val boxList = viewModel.getLikeBoxList()
            val nameList = arrayListOf<String>()
            nameList.addAll(boxList.map { it.name })
            nameList.add("新建收藏夹")
            // 显示收藏夹列表和新建收藏夹选项：收藏夹数量不定，用底部面板
            ScpDialogs.showActionSheet(this, "加入收藏夹", nameList) { i ->
                if (i == boxList.size) {
                    createNewBox()
                } else {
                    viewModel.setLiked(currentScp.link, currentScp.title, boxList[i].id)
                    refreshLikeState()
                }
            }
        } else {
            viewModel.setUnliked(currentScp.link, currentScp.title)
            refreshLikeState()
        }
    }

    private fun createNewBox() {
        ScpDialogs.showInput(
            context = this,
            title = "输入收藏夹标题",
            label = "收藏夹标题",
            positiveText = "确定",
            negativeText = "取消"
        ) { boxName ->
            Log.i("scp-detail", "create like box: $boxName")
            viewModel.createLikeBox(boxName)
            likeScp()
        }
    }

    private fun toNextArticle() {
        index = if (scp?.index != -1) scp?.index ?: 0 else index
        scpType = if (scp?.scpType != -1) scp?.scpType ?: 0 else scpType
        viewModel.rewardArticleNavigation()
        when (readType) {
            0 -> {
                scp = viewModel.getNextDocument(index, scpType)
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
        index = if (scp?.index != -1) scp?.index ?: 0 else index
        scpType = if (scp?.scpType != -1) scp?.scpType ?: 0 else scpType
        viewModel.rewardArticleNavigation()
        when (readType) {
            0 -> {
                when (index) {
                    0 -> showToast("已经是第一篇了")
                    else -> {
                        scp = viewModel.getPreviousDocument(index, scpType)
                        scp?.let {
                            viewModel.setScp(it.link, it)
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
            viewModel.toggleRead(s.link, s.title)
            refreshReadBtnStatus()
        }
    }

    private fun composeOfflineHtml(): String {
        return htmlComposer.compose(
            content = detailHtml,
            nightTheme = ThemeUtil.currentTheme == NIGHT_THEME
        )
    }

    private fun refreshReadBtnStatus() {
        val hasRead = viewModel.getDocumentStatus(url).hasRead
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

        binding.btnComment.setOnClickListener {
            if (!detailSettings.hasCommentCredentials()) {
                showToast("先点击右上角菜单设置 cookie 才能加载评论")
            } else {
                binding.llCommentContainer.visibility = VISIBLE
                binding.nsvWebWrapper.scrollTo(0, (binding.webView.height - screenHeight) + 100)
                binding.btnComment.hide()
                viewModel.loadComment(url)
            }
        }
    }

    private fun showComments(comments: List<info.free.scp.bean.CommentModel>) {
        if (comments.isEmpty()) {
            tvLoad?.text = if (HtmlParserService.noComment) {
                "这篇文档没有评论"
            } else {
                "因中分官网要求登录才能查看评论，暂时无法获取"
            }
            return
        }
        binding.llCommentContainer.removeAllViews()
        val commentLayoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            topMargin = Utils.dp2px(4)
        }
        comments.forEach { comment ->
            val commentView = CommentLayout(this).apply { setData(comment) }
            binding.llCommentContainer.addView(commentView, commentLayoutParams)
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
                historyBack = true
                viewModel.setScp(it.link, it)
            }
            historyList.removeAt(historyIndex - 1)
            historyIndex--
            return true
        } else {
            return super.onKeyDown(keyCode, event)
        }
    }

    /** 刷新标题栏星标和底部“收藏”按钮的收藏状态（原来由 onPrepareOptionsMenu 负责）。 */
    private fun refreshLikeState() {
        liked = viewModel.getDocumentStatus(url).isLiked
        if (!liked) {
            binding.tvBottomLike.text = "收藏"
            binding.tvBottomLike.background = ThemeUtil.customShape(
                ThemeUtil.itemBg, ThemeUtil.itemBg, 0, Utils.dp2px(15, this)
            )
        } else {
            binding.tvBottomLike.text = "取消收藏"
            binding.tvBottomLike.background = ThemeUtil.customShape(
                ThemeUtil.disabledBg, ThemeUtil.disabledBg, 0, Utils.dp2px(15, this)
            )
        }
    }

    private fun showToast(message: String) {
        ScpDialogs.snackbar(this, message)
    }
}
