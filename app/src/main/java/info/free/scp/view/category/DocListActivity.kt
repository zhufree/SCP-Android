package info.free.scp.view.category

import android.os.Bundle
import android.view.Menu
import androidx.lifecycle.ViewModelProvider
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.OrderType.ASC
import info.free.scp.SCPConstants.ScpType.SAVE_ARCHIVED
import info.free.scp.SCPConstants.ScpType.SAVE_CONTEST
import info.free.scp.SCPConstants.ScpType.SAVE_CONTEST_CN
import info.free.scp.SCPConstants.ScpType.SAVE_EX
import info.free.scp.SCPConstants.ScpType.SAVE_EX_CN
import info.free.scp.SCPConstants.ScpType.SAVE_JOKE
import info.free.scp.SCPConstants.ScpType.SAVE_JOKE_CN
import info.free.scp.SCPConstants.ScpType.SAVE_SERIES
import info.free.scp.SCPConstants.ScpType.SAVE_SERIES_CN
import info.free.scp.SCPConstants.ScpType.SAVE_SETTINGS
import info.free.scp.SCPConstants.ScpType.SAVE_SETTINGS_CN
import info.free.scp.SCPConstants.ScpType.SAVE_STORY_SERIES
import info.free.scp.SCPConstants.ScpType.SAVE_STORY_SERIES_CN
import info.free.scp.SCPConstants.ScpType.SAVE_TALES
import info.free.scp.SCPConstants.ScpType.SAVE_TALES_BY_TIME
import info.free.scp.SCPConstants.ScpType.SAVE_TALES_CN
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
    private var saveType = 1
    private var groupIndex = -1
    private var extraType = ""
    private var orderType = ASC
        set(value) {
            field = value
            val ascList = vm.getDocList(saveType, groupIndex)
            docAdapter.submitList(if (value == ASC) ascList else ascList.reversed()) {
                rv_doc_list.scrollToPosition(0)
            }
        }

    private fun getListTitle(saveType: Int): String {
        return when (saveType) {
            SAVE_SERIES -> "SCP系列"
            SAVE_SERIES_CN -> "SCP-CN系列"
            SAVE_JOKE -> "搞笑SCP"
            SAVE_JOKE_CN -> "CN搞笑SCP"
            SAVE_EX -> "已解明SCP"
            SAVE_EX_CN -> "CN已解明SCP"
            SAVE_TALES -> "基金会故事"
            SAVE_TALES_CN -> "CN原创故事"
            SAVE_TALES_BY_TIME -> "CN原创故事(按发布时间排列)"
            SCPConstants.ScpType.SAVE_SETTINGS -> "设定中心"
            SCPConstants.ScpType.SAVE_SETTINGS_CN -> "CN设定中心"
            SCPConstants.ScpType.SAVE_STORY_SERIES -> "故事系列"
            SCPConstants.ScpType.SAVE_STORY_SERIES_CN -> "CN故事系列"
            SCPConstants.ScpType.SAVE_CONTEST -> "征文竞赛"
            SCPConstants.ScpType.SAVE_CONTEST_CN -> "CN征文竞赛"
            else -> "SCP系列"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doc_list)

        baseToolbar = doc_list_toolbar
        doc_list_toolbar?.inflateMenu(R.menu.category_menu)
        doc_list_toolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.reverse -> {
                    orderType = -orderType
                }
            }
            true
        }
        saveType = intent.getIntExtra("saveType", 1)
        groupIndex = intent.getIntExtra("groupIndex", -1)
        extraType = intent.getStringExtra("extraType") ?: ""
        supportActionBar?.title = getListTitle(saveType)
        rv_doc_list.adapter = docAdapter
        val docList = when (saveType) {
            SAVE_SERIES, SAVE_SERIES_CN, SAVE_JOKE, SAVE_JOKE_CN, SAVE_EX, SAVE_EX_CN -> {
                vm.getDocList(saveType, groupIndex)
            }
            SAVE_TALES, SAVE_TALES_CN, SAVE_TALES_BY_TIME -> {
                vm.getDocList(saveType, extraType = extraType)
            }
            SAVE_SETTINGS, SAVE_SETTINGS_CN, SAVE_STORY_SERIES, SAVE_STORY_SERIES_CN, SAVE_CONTEST,
            SAVE_CONTEST_CN -> {
                vm.getDocList(saveType)
            }
            else -> {
                vm.getDocList(saveType, groupIndex)
            }
        }
        docAdapter.submitList(docList)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.category_menu, menu)
        return true
    }
}