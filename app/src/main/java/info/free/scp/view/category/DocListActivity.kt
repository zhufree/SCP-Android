package info.free.scp.view.category

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_doc_list.*

class DocListActivity : BaseActivity() {
    private val vm by lazy {
        ViewModelProvider(this)
                .get(GroupViewModel::class.java)
    }
    private val docAdapter by lazy {
        ScpAdapter()
    }

    private fun getListTitle(saveType: Int): String {
        return when (saveType) {
            SCPConstants.ScpType.SAVE_SERIES -> "SCP系列"
            SCPConstants.ScpType.SAVE_SERIES_CN -> "SCP-CN系列"
            SCPConstants.ScpType.SAVE_JOKE -> "搞笑SCP"
            SCPConstants.ScpType.SAVE_JOKE_CN -> "CN搞笑SCP"
            SCPConstants.ScpType.SAVE_EX -> "已解明SCP"
            SCPConstants.ScpType.SAVE_EX_CN -> "CN已解明SCP"
            else -> "SCP系列"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doc_list)

        baseToolbar = doc_list_toolbar

        val saveType = intent.getIntExtra("saveType", 1)
        val groupIndex = intent.getIntExtra("groupIndex", -1)
        supportActionBar?.title = getListTitle(saveType)
        rv_doc_list.adapter = docAdapter
        docAdapter.submitList(vm.getDocList(saveType, groupIndex))
    }
}