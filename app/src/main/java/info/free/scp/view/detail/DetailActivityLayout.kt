package info.free.scp.view.detail

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.webkit.WebView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Space
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.constraintlayout.widget.Guideline
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.core.widget.NestedScrollView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar
import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBarWrapper
import info.free.scp.R
import info.free.scp.util.ThemeUtil
import info.free.scp.util.Utils

class DetailActivityLayout private constructor(
    val root: CoordinatorLayout,
    val detailToolbar: Toolbar,
    val tvDetailToolbar: TextView,
    val nsvWebWrapper: NestedScrollView,
    val clDetailContainer: ConstraintLayout,
    val webView: WebView,
    val tvTagContainer: AppCompatTextView,
    val gpShareContent: Group,
    val llCommentContainer: LinearLayout,
    val tvBottomPreview: AppCompatButton,
    val tvBottomSetHasRead: AppCompatButton,
    val tvBottomLike: AppCompatButton,
    val tvBottomNext: AppCompatButton,
    val btnComment: FloatingActionButton,
    val sbDetail: VerticalSeekBar,
    val pbLoading: ProgressBar
) {
    companion object {
        fun create(context: Context): DetailActivityLayout {
            val root = CoordinatorLayout(context).apply {
                layoutParams = CoordinatorLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                setBackgroundColor(ThemeUtil.containerBg)
            }

            val appBar = AppBarLayout(context).apply {
                id = R.id.ablToolbarWrapper
                fitsSystemWindows = true
            }
            val toolbar = Toolbar(context).apply {
                id = R.id.detail_toolbar
                setBackgroundColor(ThemeUtil.toolbarBg)
                popupTheme = R.style.ToolbarPopupTheme
            }
            val titleView = TextView(context).apply {
                id = R.id.tv_detail_toolbar
                ellipsize = android.text.TextUtils.TruncateAt.MARQUEE
                isFocusable = true
                isFocusableInTouchMode = true
                marqueeRepeatLimit = -1
                setSingleLine(true)
                textSize = 18f
            }
            toolbar.addView(titleView, Toolbar.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
            appBar.addView(
                toolbar,
                AppBarLayout.LayoutParams(MATCH_PARENT, Utils.dp2px(56, context)).apply {
                    scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or
                        AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
                }
            )
            root.addView(appBar, CoordinatorLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))

            val scrollView = NestedScrollView(context).apply {
                id = R.id.nsv_web_wrapper
            }
            val detailContainer = ConstraintLayout(context).apply {
                id = R.id.cl_detail_container
                minHeight = Utils.dp2px(500, context)
                setBackgroundColor(ThemeUtil.containerBg)
            }
            val webView = WebView(context).apply {
                id = R.id.webView
                setPadding(Utils.dp2px(16, context))
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
            }
            detailContainer.addView(
                webView,
                ConstraintLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                    topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                }
            )

            val tagView = AppCompatTextView(context).apply {
                id = R.id.tv_tag_container
                text = "\u8fd9\u91cc\u662f\u6807\u7b7e"
                textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                setPadding(0, 0, 0, Utils.dp2px(8, context))
            }
            detailContainer.addView(
                tagView,
                ConstraintLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                    marginStart = Utils.dp2px(16, context)
                    marginEnd = Utils.dp2px(16, context)
                    topMargin = Utils.dp2px(16, context)
                    topToBottom = R.id.webView
                    startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                    endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                }
            )

            val shareNotice = TextView(context).apply {
                id = R.id.tv_share_notice
                gravity = Gravity.CENTER
                text = "\u626b\u63cf\u4e8c\u7ef4\u7801\uff0c\u63a2\u7d22SCP\u57fa\u91d1\u4f1a\u7684\u79d8\u5bc6"
                textSize = 16f
            }
            detailContainer.addView(
                shareNotice,
                ConstraintLayout.LayoutParams(0, WRAP_CONTENT).apply {
                    topMargin = Utils.dp2px(8, context)
                    topToBottom = R.id.tv_tag_container
                    startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                    endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                }
            )

            val qrView = ImageView(context).apply {
                id = R.id.iv_download_qrcode
                contentDescription = context.getString(R.string.app_name)
                setImageResource(R.drawable.download_qr)
            }
            detailContainer.addView(
                qrView,
                ConstraintLayout.LayoutParams(Utils.dp2px(250, context), Utils.dp2px(250, context)).apply {
                    topMargin = Utils.dp2px(4, context)
                    topToBottom = R.id.tv_share_notice
                    bottomToTop = R.id.s_blank_bottom
                    startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                    endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                }
            )

            val shareGroup = Group(context).apply {
                id = R.id.gp_share_content
                referencedIds = intArrayOf(R.id.tv_share_notice, R.id.iv_download_qrcode)
                visibility = View.GONE
            }
            detailContainer.addView(shareGroup)

            val commentContainer = LinearLayout(context).apply {
                id = R.id.ll_comment_container
                orientation = LinearLayout.VERTICAL
                minimumHeight = Utils.dp2px(100, context)
                visibility = View.GONE
                setPadding(
                    Utils.dp2px(12, context),
                    Utils.dp2px(8, context),
                    Utils.dp2px(12, context),
                    Utils.dp2px(64, context)
                )
                addView(
                    TextView(context).apply {
                        gravity = Gravity.CENTER
                        text = "\u8bc4\u8bba\u52a0\u8f7d\u4e2d..."
                        textSize = 20f
                    },
                    LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                        gravity = Gravity.CENTER_HORIZONTAL
                    }
                )
            }
            detailContainer.addView(
                commentContainer,
                ConstraintLayout.LayoutParams(0, WRAP_CONTENT).apply {
                    topToBottom = R.id.tv_tag_container
                    startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                    endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                }
            )

            val bottomSpace = Space(context).apply { id = R.id.s_blank_bottom }
            detailContainer.addView(
                bottomSpace,
                ConstraintLayout.LayoutParams(MATCH_PARENT, Utils.dp2px(64, context)).apply {
                    topToBottom = R.id.iv_download_qrcode
                    bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                }
            )

            scrollView.addView(
                detailContainer,
                android.view.ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            )
            root.addView(
                scrollView,
                CoordinatorLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
                    behavior = AppBarLayout.ScrollingViewBehavior()
                }
            )

            val bottomBar = ConstraintLayout(context)
            root.addView(
                bottomBar,
                CoordinatorLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                    gravity = Gravity.BOTTOM
                    topMargin = Utils.dp2px(16, context)
                    bottomMargin = Utils.dp2px(16, context)
                }
            )

            val previewButton = bottomButton(context, R.string.preview_article).apply {
                id = R.id.tv_bottom_preview
            }
            bottomBar.addView(
                previewButton,
                ConstraintLayout.LayoutParams(Utils.dp2px(60, context), Utils.dp2px(30, context)).apply {
                    marginStart = Utils.dp2px(16, context)
                    bottomMargin = Utils.dp2px(4, context)
                    bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                    startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                }
            )

            val guideline = Guideline(context).apply { id = R.id.gl_detail_center }
            bottomBar.addView(
                guideline,
                ConstraintLayout.LayoutParams(0, MATCH_PARENT).apply {
                    orientation = ConstraintLayout.LayoutParams.VERTICAL
                    guideBegin = Utils.dp2px(206, context)
                }
            )

            val readButton = bottomButton(context, R.string.set_has_read).apply {
                id = R.id.tv_bottom_set_has_read
            }
            bottomBar.addView(
                readButton,
                ConstraintLayout.LayoutParams(Utils.dp2px(80, context), Utils.dp2px(30, context)).apply {
                    bottomMargin = Utils.dp2px(4, context)
                    bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                    startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                    endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                }
            )

            val likeButton = bottomButton(context, R.string.menu_like).apply {
                id = R.id.tv_bottom_like
                visibility = View.GONE
            }
            bottomBar.addView(
                likeButton,
                ConstraintLayout.LayoutParams(Utils.dp2px(80, context), Utils.dp2px(30, context)).apply {
                    bottomMargin = Utils.dp2px(4, context)
                    bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                    startToStart = R.id.gl_detail_center
                }
            )

            val nextButton = bottomButton(context, R.string.next_article).apply {
                id = R.id.tv_bottom_next
            }
            bottomBar.addView(
                nextButton,
                ConstraintLayout.LayoutParams(Utils.dp2px(60, context), Utils.dp2px(30, context)).apply {
                    marginEnd = Utils.dp2px(16, context)
                    bottomMargin = Utils.dp2px(4, context)
                    bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                    endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                }
            )

            val commentButton = FloatingActionButton(context).apply {
                id = R.id.btn_comment
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary))
                elevation = Utils.dp2px(10, context).toFloat()
                setImageResource(R.drawable.baseline_textsms_white_24dp)
                rippleColor = ContextCompat.getColor(context, R.color.colorPrimaryDark)
            }
            bottomBar.addView(
                commentButton,
                ConstraintLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                    marginEnd = Utils.dp2px(16, context)
                    bottomMargin = Utils.dp2px(16, context)
                    bottomToTop = R.id.tv_bottom_next
                    endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                }
            )

            val seekWrapper = VerticalSeekBarWrapper(context)
            val seekBar = VerticalSeekBar(context).apply {
                id = R.id.sb_detail
                splitTrack = false
                thumb = ContextCompat.getDrawable(context, R.drawable.ic_seekbar_thumb)
                progressDrawable = ContextCompat.getDrawable(context, R.drawable.progress_bar)
            }
            seekWrapper.addView(seekBar, android.view.ViewGroup.LayoutParams(0, 0))
            root.addView(
                seekWrapper,
                CoordinatorLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT).apply {
                    gravity = Gravity.END
                    topMargin = Utils.dp2px(48, context)
                    bottomMargin = Utils.dp2px(16, context)
                }
            )

            val progress = ProgressBar(context, null, android.R.attr.progressBarStyle).apply {
                id = R.id.pbLoading
            }
            root.addView(
                progress,
                CoordinatorLayout.LayoutParams(Utils.dp2px(40, context), Utils.dp2px(40, context)).apply {
                    gravity = Gravity.CENTER
                    topMargin = Utils.dp2px(60, context)
                    bottomMargin = Utils.dp2px(60, context)
                }
            )

            return DetailActivityLayout(
                root = root,
                detailToolbar = toolbar,
                tvDetailToolbar = titleView,
                nsvWebWrapper = scrollView,
                clDetailContainer = detailContainer,
                webView = webView,
                tvTagContainer = tagView,
                gpShareContent = shareGroup,
                llCommentContainer = commentContainer,
                tvBottomPreview = previewButton,
                tvBottomSetHasRead = readButton,
                tvBottomLike = likeButton,
                tvBottomNext = nextButton,
                btnComment = commentButton,
                sbDetail = seekBar,
                pbLoading = progress
            )
        }

        private fun bottomButton(context: Context, textRes: Int): AppCompatButton {
            return AppCompatButton(context).apply {
                gravity = Gravity.CENTER
                setPadding(0)
                setText(textRes)
                setTextColor(ThemeUtil.darkText)
                setTypeface(typeface, Typeface.BOLD)
            }
        }
    }
}
