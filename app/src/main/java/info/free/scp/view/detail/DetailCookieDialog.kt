package info.free.scp.view.detail

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface.BUTTON_POSITIVE
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import info.free.scp.data.settings.DetailWebCredentials
import info.free.scp.util.Utils

class DetailCookieDialog(
    private val context: Context
) {
    fun show(
        credentials: DetailWebCredentials,
        onOpenHelp: () -> Unit,
        onOpenLogin: () -> Unit,
        onSave: (cookie: String, userAgent: String) -> Unit
    ) {
        val margin = Utils.dp2px(16, context)
        val container = LinearLayout(context).apply { orientation = VERTICAL }
        val cookieInput = EditText(context).apply {
            hint = "cookie"
            setSingleLine(false)
            setText(credentials.cookie)
        }
        val agentInput = EditText(context).apply {
            hint = "agent"
            setSingleLine(false)
            setText(credentials.userAgent)
        }
        val loginButton = Button(context).apply {
            text = "\u4f7f\u7528\u7f51\u9875\u767b\u5f55"
        }
        val inputLayoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            setMargins(margin, margin, margin, 0)
        }
        container.addView(cookieInput, inputLayoutParams)
        container.addView(agentInput, inputLayoutParams)
        container.addView(
            loginButton,
            LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                setMargins(margin, margin, margin, margin)
            }
        )

        val dialog = AlertDialog.Builder(context)
            .setTitle("\u8bbe\u7f6eCookie")
            .setView(container)
            .setNeutralButton("\u5982\u4f55\u83b7\u53d6cookie\u548cagent") { _, _ -> onOpenHelp() }
            .setPositiveButton("OK") { _, _ -> }
            .setNegativeButton("Cancel") { currentDialog, _ -> currentDialog.dismiss() }
            .create()
        dialog.show()

        loginButton.setOnClickListener {
            onOpenLogin()
            dialog.dismiss()
        }
        dialog.getButton(BUTTON_POSITIVE).setOnClickListener {
            onSave(cookieInput.text.toString(), agentInput.text.toString())
            dialog.dismiss()
        }
    }
}
