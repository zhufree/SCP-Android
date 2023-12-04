package info.free.scp.view.draft

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import info.free.scp.R
import info.free.scp.bean.DraftModel
import info.free.scp.databinding.ActivityDraftListBinding
import info.free.scp.view.base.BaseActivity
import info.free.scp.viewmodel.DraftListViewModel
import org.jetbrains.anko.startActivity

class DraftListActivity : BaseActivity() {
    private lateinit var binding: ActivityDraftListBinding

    private val model by lazy {
        ViewModelProvider(this)
                .get(DraftListViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDraftListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        baseToolbar = binding.draftListToolbar
        binding.draftListToolbar.inflateMenu(R.menu.menu_draft_list)
        binding.draftListToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.add_draft -> {
                    startActivity<DraftEditActivity>()
                }

                R.id.draft_help -> {
                    val jumpIntent = Intent()
                    jumpIntent.action = "android.intent.action.VIEW"
                    val updateUrl = Uri.parse("http://scp-wiki-cn.wikidot.com/how-to-write-an-scp")
                    jumpIntent.data = updateUrl
                    startActivity(jumpIntent)
                }
            }
            true
        }

        val adapter = DraftAdapter()
        binding.rlDraftList.adapter = adapter

        model.getDraft()?.observe(this, Observer<List<DraftModel>> { drafts ->
                    // update UI
                    if (drafts.isNotEmpty()) {
                        binding.tvEmptyDraft.visibility = View.GONE
                        adapter.submitList(drafts)
                        adapter.notifyDataSetChanged()
                    }
                })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_draft_list, menu)
        return true
    }
}
