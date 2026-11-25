package info.free.scp.util

import android.content.Context
import android.text.InputType
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import info.free.scp.R

/**
 * View 侧弹窗 / 底部面板 / 输入框 / 提示的统一入口（Compose 侧见 ui/common/Dialogs.kt）。
 *
 * 所有弹窗都用 Material 3 组件，样式由主题里的 materialAlertDialogTheme / bottomSheetDialogTheme / snackbarStyle 决定。
 * 日夜只由 [ThemeUtil.currentTheme] 决定：这里每次都按它给 context 套一层 DayTheme/NightTheme，
 * 这样即使传入的 Activity 在后台栈里没来得及 setTheme、或是 Application context 派生的 context，也不会拿错主题。
 */
object ScpDialogs {

    /** 套上当前日夜主题的 context。必须以 Activity 为底（弹窗需要窗口 token），不要传 Application context。 */
    fun themed(context: Context): Context = ContextThemeWrapper(
        context,
        if (ThemeUtil.currentTheme == ThemeUtil.NIGHT_THEME) R.style.NightTheme else R.style.DayTheme
    )

    fun builder(context: Context): MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(themed(context))

    /** 单按钮提示。 */
    fun showMessage(
        context: Context,
        message: CharSequence,
        title: CharSequence? = null,
        positiveText: CharSequence = context.getString(android.R.string.ok),
        onPositive: (() -> Unit)? = null
    ): AlertDialog = builder(context)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positiveText) { _, _ -> onPositive?.invoke() }
        .show()

    /** 必须选一项的短列表（≤ 6 项）：M3 简单对话框，点选即确认。 */
    fun showChoice(
        context: Context,
        title: CharSequence,
        items: List<String>,
        onSelect: (Int) -> Unit
    ): AlertDialog = builder(context)
        .setTitle(title)
        .setItems(items.toTypedArray()) { _, index -> onSelect(index) }
        .show()

    /**
     * 底部操作面板：选项较多或属于“从列表里选一个去执行”的操作（选收藏夹、选下载线路）。
     * 用 M3 BottomSheetDialog，拇指够得着；一次点选即关闭。
     */
    fun showActionSheet(
        context: Context,
        title: CharSequence?,
        items: List<String>,
        onSelect: (Int) -> Unit
    ): BottomSheetDialog {
        val themedContext = themed(context)
        val dialog = BottomSheetDialog(themedContext)
        val density = themedContext.resources.displayMetrics.density
        fun dp(value: Int) = (value * density + 0.5f).toInt()

        val column = LinearLayout(themedContext).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, dp(16))
        }
        if (!title.isNullOrEmpty()) {
            column.addView(
                TextView(themedContext).apply {
                    setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleMedium)
                    text = title
                    setPadding(dp(24), dp(8), dp(24), dp(12))
                    setTextColor(resolveColor(themedContext, com.google.android.material.R.attr.colorOnSurface))
                },
                LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            )
        }
        val rowBackground = android.util.TypedValue().also {
            themedContext.theme.resolveAttribute(android.R.attr.selectableItemBackground, it, true)
        }.resourceId
        items.forEachIndexed { index, label ->
            column.addView(
                TextView(themedContext).apply {
                    setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge)
                    text = label
                    gravity = android.view.Gravity.CENTER_VERTICAL
                    minHeight = dp(56)
                    setPadding(dp(24), 0, dp(24), 0)
                    setTextColor(resolveColor(themedContext, com.google.android.material.R.attr.colorOnSurface))
                    setBackgroundResource(rowBackground)
                    setOnClickListener {
                        dialog.dismiss()
                        onSelect(index)
                    }
                },
                LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            )
        }
        val scroll = androidx.core.widget.NestedScrollView(themedContext).apply { addView(column) }
        dialog.setContentView(scroll)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.behavior.skipCollapsed = true
        dialog.show()
        return dialog
    }

    /**
     * 单个输入框的对话框（M3 描边输入框 + 浮动标签）。
     * [allowBlank] 为 false 时输入为空会禁用确认键并在输入框下提示；其余校验由调用方的 [onConfirm] 决定。
     */
    fun showInput(
        context: Context,
        title: CharSequence?,
        label: CharSequence,
        initialText: String = "",
        message: CharSequence? = null,
        singleLine: Boolean = true,
        maxLines: Int = 8,
        allowBlank: Boolean = true,
        positiveText: CharSequence = context.getString(android.R.string.ok),
        negativeText: CharSequence = context.getString(android.R.string.cancel),
        onConfirm: (String) -> Unit
    ): AlertDialog {
        val builder = builder(context)
        val field = inputField(builder.context, label, initialText, singleLine, maxLines)
        val dialog = builder
            .setTitle(title)
            .setMessage(message)
            .setView(withDialogPadding(builder.context, field))
            .setPositiveButton(positiveText, null)
            .setNegativeButton(negativeText, null)
            .create()
        dialog.setOnShowListener {
            field.editText?.requestFocus()
            dialog.window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            val positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positive.setOnClickListener {
                onConfirm(field.editText?.text?.toString().orEmpty())
                dialog.dismiss()
            }
            if (!allowBlank) {
                fun refresh() {
                    val blank = field.editText?.text.isNullOrBlank()
                    positive.isEnabled = !blank
                    field.error = if (blank) context.getString(R.string.dialog_input_required) else null
                }
                field.editText?.addTextChangedListener(object : android.text.TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = refresh()
                    override fun afterTextChanged(s: android.text.Editable?) = Unit
                })
                refresh()
            }
        }
        dialog.show()
        return dialog
    }

    /** 创建一个 M3 描边输入框（TextInputLayout + TextInputEditText），用于自定义内容的对话框。 */
    fun inputField(
        context: Context,
        label: CharSequence,
        initialText: String = "",
        singleLine: Boolean = true,
        maxLines: Int = 8
    ): TextInputLayout {
        val layout = TextInputLayout(
            context, null, com.google.android.material.R.attr.textInputOutlinedStyle
        ).apply { hint = label }
        val edit = TextInputEditText(layout.context).apply {
            setText(initialText)
            if (singleLine) {
                setSingleLine(true)
            } else {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                minLines = 1
                this.maxLines = maxLines
                isSingleLine = false
            }
        }
        layout.addView(edit, FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        return layout
    }

    /** 给自定义内容加上与 M3 对话框正文一致的 24dp 水平边距。 */
    fun withDialogPadding(context: Context, vararg children: View): View {
        val density = context.resources.displayMetrics.density
        fun dp(value: Int) = (value * density + 0.5f).toInt()
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(8), dp(24), 0)
            children.forEachIndexed { index, child ->
                val params = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                if (index > 0) params.topMargin = dp(12)
                addView(child, params)
            }
        }
    }

    /** 在当前页面底部弹出 M3 Snackbar，用来替代 Toast。页面即将关闭时的提示请继续用 Toast。 */
    fun snackbar(activity: AppCompatActivity, message: CharSequence) {
        val root = activity.findViewById<View>(android.R.id.content) ?: return
        val duration = if (message.length > 16) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT
        Snackbar.make(root, message, duration).show()
    }

    private fun resolveColor(context: Context, @AttrRes attr: Int): Int {
        val value = android.util.TypedValue()
        context.theme.resolveAttribute(attr, value, true)
        return if (value.resourceId != 0) context.getColor(value.resourceId) else value.data
    }
}
