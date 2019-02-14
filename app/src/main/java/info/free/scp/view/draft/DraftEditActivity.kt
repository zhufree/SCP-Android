package info.free.scp.view.draft

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import info.free.scp.R
import info.free.scp.util.Logger
import info.free.scp.util.PreferenceUtil
import info.free.scp.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_draft_edit.*

class DraftEditActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draft_edit)

        setSupportActionBar(draft_toolbar)
        supportActionBar?.setTitle(R.string.draft_list)
        draft_toolbar?.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        draft_toolbar?.setNavigationOnClickListener {
            PreferenceUtil.saveDraftContent(et_content.editableText.toString())
            PreferenceUtil.saveDraftTitle(et_title.editableText.toString())
            finish()
        }

        val oldDraft = PreferenceUtil.getDraftContent()
        if (oldDraft.isNotEmpty()) {
            et_content.editableText.append(oldDraft)
        }
        val oldTitle = PreferenceUtil.getDraftTitle()
        if (oldTitle.isNotEmpty()) {
            et_title.editableText.append(oldTitle)
        }

        et_content.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Logger.i(s.toString())
                PreferenceUtil.saveDraftContent(et_content.editableText.toString())
            }
        })
    }
}
