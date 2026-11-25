package info.free.scp.view.detail

import android.content.Context
import android.content.DialogInterface.BUTTON_POSITIVE
import com.google.android.material.button.MaterialButton
import info.free.scp.data.settings.DetailWebCredentials
import info.free.scp.util.ScpDialogs

/** 设置 Cookie / User-Agent 的 M3 对话框：两个描边输入框 + 网页登录按钮，保存与回调逻辑不变。 */
class DetailCookieDialog(
    private val context: Context
) {
    fun show(
        credentials: DetailWebCredentials,
        onOpenHelp: () -> Unit,
        onOpenLogin: () -> Unit,
        onSave: (cookie: String, userAgent: String) -> Unit
    ) {
        val builder = ScpDialogs.builder(context)
        val dialogContext = builder.context
        val cookieInput = ScpDialogs.inputField(dialogContext, "cookie", credentials.cookie, singleLine = false, maxLines = 6)
        val agentInput = ScpDialogs.inputField(dialogContext, "agent", credentials.userAgent, singleLine = false, maxLines = 6)
        val loginButton = MaterialButton(
            dialogContext, null, com.google.android.material.R.attr.materialButtonOutlinedStyle
        ).apply {
            text = "使用网页登录"
        }

        val dialog = builder
            .setTitle("设置Cookie")
            .setView(ScpDialogs.withDialogPadding(dialogContext, cookieInput, agentInput, loginButton))
            .setNeutralButton("如何获取cookie和agent") { _, _ -> onOpenHelp() }
            .setPositiveButton("OK", null)
            .setNegativeButton("Cancel") { currentDialog, _ -> currentDialog.dismiss() }
            .create()
        dialog.show()

        loginButton.setOnClickListener {
            onOpenLogin()
            dialog.dismiss()
        }
        dialog.getButton(BUTTON_POSITIVE).setOnClickListener {
            onSave(cookieInput.editText?.text.toString(), agentInput.editText?.text.toString())
            dialog.dismiss()
        }
    }
}
