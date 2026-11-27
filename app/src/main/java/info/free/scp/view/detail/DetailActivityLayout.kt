package info.free.scp.view.detail

import android.content.Context
import android.graphics.Typeface
import android.text.TextUtils
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.setPadding
import androidx.core.widget.NestedScrollView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.color.MaterialColors
import info.free.scp.R
import info.free.scp.util.ThemeUtil
import info.free.scp.util.Utils

/**
 * 正文阅读页的 View 树：正文滚动区铺满整个页面，顶栏和底栏盖在上下两端；滚动区的上下留白等于两栏高度，
 * 所以文章开头和结尾不会被遮住，而两栏收起/展开时正文不会跟着重排。
 */
class DetailActivityLayout private constructor(
    val root: FrameLayout,
    val detailToolbar: Toolbar,
    val tvDetailToolbar: TextView,
    val tvDetailSubtitle: TextView,
    val toolbarActions: FrameLayout,
    val nsvWebWrapper: NestedScrollView,
    val detailContainer: LinearLayout,
    val webView: WebView,
    val tagSection: LinearLayout,
    val tagsHeader: SectionHeader,
    val tagGroup: ChipGroup,
    val commentSection: LinearLayout,
    val commentsHeader: SectionHeader,
    val llCommentContainer: LinearLayout,
    val llShareContent: LinearLayout,
    val bottomBar: DetailBottomBar,
    val fastScroll: FastScrollView,
    val pbLoading: ProgressBar
) {
    /** 顶栏高度，也是正文滚动区的上留白。 */
    val topBarHeight: Int get() = detailToolbar.layoutParams.height

    /** 带进度线的底栏总高度，也是正文滚动区的下留白。 */
    val bottomBarHeight: Int get() = bottomBar.barHeight + Utils.dp2px(2, root.context)

    fun refreshTheme() {
        tvDetailToolbar.setTextColor(ThemeUtil.onToolbar)
        tvDetailSubtitle.setTextColor(ThemeUtil.onToolbar)
        tagsHeader.refreshTheme()
        commentsHeader.refreshTheme()
        bottomBar.refreshTheme()
        fastScroll.refreshTheme()
    }

    companion object {
        fun create(context: Context): DetailActivityLayout {
            val root = FrameLayout(context).apply {
                layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                setBackgroundColor(ThemeUtil.containerBg)
            }
            val barHeight = Utils.dp2px(56, context)
            val bottomBar = DetailBottomBar(context)
            val bottomBarHeight = bottomBar.barHeight + Utils.dp2px(2, context)

            val scrollView = NestedScrollView(context).apply {
                id = R.id.nsv_web_wrapper
                isFillViewport = true
                clipToPadding = false
                setPadding(0, barHeight, 0, bottomBarHeight)
            }
            val detailContainer = LinearLayout(context).apply {
                id = R.id.cl_detail_container
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(ThemeUtil.containerBg)
            }
            val webView = WebView(context).apply {
                id = R.id.webView
                setPadding(Utils.dp2px(16, context))
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
            }
            detailContainer.addView(webView, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))

            val sidePadding = Utils.dp2px(16, context)
            val tagsHeader = SectionHeader(context, R.string.reading_tags, R.string.reading_tags_en)
            val tagGroup = ChipGroup(context).apply {
                id = R.id.tv_tag_container
                chipSpacingHorizontal = Utils.dp2px(8, context)
                chipSpacingVertical = 0
            }
            val tagSection = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                visibility = View.GONE
                setPadding(sidePadding, Utils.dp2px(8, context), sidePadding, 0)
                addView(tagsHeader, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
                addView(tagGroup, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
            }
            detailContainer.addView(tagSection, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))

            val commentsHeader = SectionHeader(context, R.string.reading_comments, R.string.reading_comments_en)
            val commentContainer = LinearLayout(context).apply {
                id = R.id.ll_comment_container
                orientation = LinearLayout.VERTICAL
            }
            val commentSection = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                visibility = View.GONE
                setPadding(sidePadding, Utils.dp2px(8, context), sidePadding, 0)
                addView(commentsHeader, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
                addView(commentContainer, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
            }
            detailContainer.addView(commentSection, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))

            // 图片分享才会显示的二维码区域
            val shareNotice = TextView(context).apply {
                id = R.id.tv_share_notice
                gravity = Gravity.CENTER
                text = "扫描二维码，探索SCP基金会的秘密"
                textSize = 16f
            }
            val qrView = ImageView(context).apply {
                id = R.id.iv_download_qrcode
                contentDescription = context.getString(R.string.app_name)
                setImageResource(R.drawable.download_qr)
            }
            val shareContent = LinearLayout(context).apply {
                id = R.id.gp_share_content
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                visibility = View.GONE
                setPadding(0, Utils.dp2px(16, context), 0, Utils.dp2px(16, context))
                addView(shareNotice, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
                addView(
                    qrView,
                    LinearLayout.LayoutParams(Utils.dp2px(250, context), Utils.dp2px(250, context)).apply {
                        topMargin = Utils.dp2px(4, context)
                    }
                )
            }
            detailContainer.addView(shareContent, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))

            scrollView.addView(detailContainer, FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
            root.addView(scrollView, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))

            val progress = ProgressBar(context, null, android.R.attr.progressBarStyle).apply {
                id = R.id.pbLoading
            }
            root.addView(
                progress,
                FrameLayout.LayoutParams(Utils.dp2px(40, context), Utils.dp2px(40, context)).apply {
                    gravity = Gravity.CENTER
                }
            )

            val fastScroll = FastScrollView(context).apply { id = R.id.fast_scroll }
            fastScroll.bind(scrollView)
            root.addView(
                fastScroll,
                FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
                    topMargin = barHeight
                    bottomMargin = bottomBarHeight
                }
            )

            // 与旧版布局里 AppBarLayout 的 android:theme 一致：标题和返回箭头用浅色
            val toolbar = Toolbar(ContextThemeWrapper(context, R.style.AppTheme_AppBarOverlay)).apply {
                id = R.id.detail_toolbar
                setBackgroundColor(ThemeUtil.toolbarBg)
                setContentInsetsRelative(Utils.dp2px(16, context), Utils.dp2px(4, context))
            }
            val titleView = TextView(toolbar.context).apply {
                id = R.id.tv_detail_toolbar
                ellipsize = TextUtils.TruncateAt.MARQUEE
                isFocusable = true
                isFocusableInTouchMode = true
                marqueeRepeatLimit = -1
                setSingleLine(true)
                textSize = 18f
                setTextColor(ThemeUtil.onToolbar)
            }
            val subtitleView = TextView(toolbar.context).apply {
                id = R.id.tv_detail_subtitle
                setSingleLine(true)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                setTextColor(ThemeUtil.onToolbar)
                alpha = 0.78f
                setText(R.string.reading_web_mode)
                visibility = View.GONE
            }
            val titleColumn = LinearLayout(toolbar.context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_VERTICAL
                addView(titleView, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
                addView(subtitleView, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
            }
            // 收藏星标和菜单（DetailToolbarActions 的 ComposeView）放在 actions 里
            val actions = FrameLayout(toolbar.context)
            // Toolbar 按顺序测量自定义子 View，先加的满宽 View 会把剩余宽度用光。所以只加这一个横向容器，
            // 标题 weight=1、动作区 WRAP_CONTENT，由它自己分配宽度。
            val toolbarContent = LinearLayout(toolbar.context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                addView(titleColumn, LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f))
                addView(actions, LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT))
            }
            toolbar.addView(toolbarContent, Toolbar.LayoutParams(MATCH_PARENT, MATCH_PARENT))
            root.addView(toolbar, FrameLayout.LayoutParams(MATCH_PARENT, barHeight, Gravity.TOP))
            root.addView(
                bottomBar,
                FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, Gravity.BOTTOM)
            )

            return DetailActivityLayout(
                root = root,
                detailToolbar = toolbar,
                tvDetailToolbar = titleView,
                tvDetailSubtitle = subtitleView,
                toolbarActions = actions,
                nsvWebWrapper = scrollView,
                detailContainer = detailContainer,
                webView = webView,
                tagSection = tagSection,
                tagsHeader = tagsHeader,
                tagGroup = tagGroup,
                commentSection = commentSection,
                commentsHeader = commentsHeader,
                llCommentContainer = commentContainer,
                llShareContent = shareContent,
                bottomBar = bottomBar,
                fastScroll = fastScroll,
                pbLoading = progress
            )
        }
    }

    /** 文末小节标题：“标签 TAGS”“评论 COMMENTS”。 */
    class SectionHeader(context: Context, zhRes: Int, enRes: Int) : LinearLayout(context) {
        private val zh = TextView(context)
        private val en = TextView(context)

        init {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            minimumHeight = Utils.dp2px(32, context)
            zh.apply {
                setText(zhRes)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            }
            en.apply {
                setText(enRes)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                typeface = Typeface.MONOSPACE
                letterSpacing = 0.12f
            }
            addView(zh, LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
            addView(en, LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply { marginStart = Utils.dp2px(8, context) })
            refreshTheme()
        }

        fun refreshTheme() {
            zh.setTextColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface))
            en.setTextColor(ThemeUtil.mediumText)
        }
    }
}
