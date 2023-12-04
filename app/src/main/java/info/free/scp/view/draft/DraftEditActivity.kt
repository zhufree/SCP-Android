package info.free.scp.view.draft

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import info.free.scp.R
import info.free.scp.bean.DraftModel
import info.free.scp.databinding.ActivityDraftEditBinding
import info.free.scp.db.AppInfoDatabase
import info.free.scp.util.PreferenceUtil
import info.free.scp.view.base.BaseActivity
import org.jetbrains.anko.toast

class DraftEditActivity : BaseActivity() {
    lateinit var draft: DraftModel
    var draftDao = AppInfoDatabase.getInstance().draftDao()
    private lateinit var binding: ActivityDraftEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDraftEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        baseToolbar = binding.draftToolbar
        supportActionBar?.setTitle(R.string.draft_list)

        binding.draftToolbar?.inflateMenu(R.menu.menu_draft_edit)
        binding.draftToolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
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
                PreferenceUtil.getDraftTitle(), PreferenceUtil.getDraftContent()
            )
        }
        PreferenceUtil.saveDraftContent("")
        PreferenceUtil.saveDraftTitle("")
        binding.draftToolbar?.setNavigationOnClickListener {
            draft.lastModifyTime = System.currentTimeMillis()
            draftDao.save(draft)
            finish()
        }

        binding.etTitle.editableText.append(draft.title)
        binding.etContent.editableText.append(draft.content)

        binding.etTitle.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                draft.title = binding.etTitle.editableText.toString()
            }
        })

        binding.etContent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                draft.content = binding.etContent.editableText.toString()
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
