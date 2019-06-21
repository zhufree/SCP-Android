package info.free.scp.view.draft

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import info.free.scp.R
import info.free.scp.bean.DraftModel
import info.free.scp.db.AppInfoDatabase
import info.free.scp.util.PreferenceUtil
import info.free.scp.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_draft_edit.*
import org.jetbrains.anko.toast

class DraftEditActivity : BaseActivity() {
    lateinit var draft: DraftModel
    var draftDao = AppInfoDatabase.getInstance().draftDao()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draft_edit)

        baseToolbar = draft_toolbar
        supportActionBar?.setTitle(R.string.draft_list)

        draft_toolbar?.inflateMenu(R.menu.menu_draft_edit)
        draft_toolbar?.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.save_draft -> {
                    draft.lastModifyTime = System.currentTimeMillis()
                    draftDao.save(draft)
                    toast("已保存")
                }
            }
            true
        }

        val draftId = intent.getIntExtra("draftId", -1)

        draft = if (draftId > 0) {
            draftDao.getDraft(draftId)
        } else {
            DraftModel(draftDao.getDraftCount() + 1, System.currentTimeMillis(),
                    PreferenceUtil.getDraftTitle(), PreferenceUtil.getDraftContent())
        }
        PreferenceUtil.saveDraftContent("")
        PreferenceUtil.saveDraftTitle("")
        draft_toolbar?.setNavigationOnClickListener {
            draft.lastModifyTime = System.currentTimeMillis()
            draftDao.save(draft)
            finish()
        }

        et_title.editableText.append(draft.title)
        et_content.editableText.append(draft.content)

        et_title.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                draft.title = et_title.editableText.toString()
            }
        })

        et_content.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                draft.content = et_content.editableText.toString()
            }
        })
    }

    override fun onPause() {
        super.onPause()
        draft.lastModifyTime = System.currentTimeMillis()
        draftDao.save(draft)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_draft_edit, menu)
        return true
    }
}
