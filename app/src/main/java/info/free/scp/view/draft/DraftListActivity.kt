package info.free.scp.view.draft

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import info.free.scp.R
import info.free.scp.bean.DraftModel
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.detail.DetailViewModel
import info.free.scp.viewmodel.DraftListViewModel
import kotlinx.android.synthetic.main.activity_draft_list.*
import org.jetbrains.anko.startActivity

class DraftListActivity : BaseActivity() {

    private val model by lazy {
        ViewModelProvider(this)
                .get(DraftListViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draft_list)
        baseToolbar = draft_list_toolbar
        draft_list_toolbar.inflateMenu(R.menu.menu_draft_list)
        draft_list_toolbar?.setOnMenuItemClickListener {
            when(it.itemId) {
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
        rl_draft_list.adapter = adapter

        model.getDraft()?.observe(this, Observer<List<DraftModel>> { drafts ->
                    // update UI
                    if (drafts.isNotEmpty()) {
                        tv_empty_draft.visibility = View.GONE
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
