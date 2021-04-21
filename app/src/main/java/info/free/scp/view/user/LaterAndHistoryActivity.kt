package info.free.scp.view.user

import android.os.Bundle
import android.view.Menu
import info.free.scp.R
import info.free.scp.SCPConstants.HISTORY_TYPE
import info.free.scp.SCPConstants.OrderType.ASC
import info.free.scp.db.AppInfoDatabase
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.later.RecordListFragment
import kotlinx.android.synthetic.main.activity_history.*
import org.jetbrains.anko.*

/**
 * 待读列表
 */
class LaterAndHistoryActivity : BaseActivity() {
    private var orderType = ASC // 0 時間正序，倒序
        set(value) {
            field = value
            fragment.reverseList(value)
        }

    val fragment = RecordListFragment.newInstance(HISTORY_TYPE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        initToolbar()
        supportFragmentManager.beginTransaction()
                .add(R.id.fl_history, fragment, "f_record")
                .commit()
    }


    private fun initToolbar() {
        baseToolbar = history_toolbar
        history_toolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.reverse -> {
                    orderType = -orderType
                }
                R.id.clear_read_history -> {
                    // 清除历史记录
                    alert("你确定要清除所有的阅读记录吗", "Notice") {
                        yesButton {
                            AppInfoDatabase.getInstance().readRecordDao().clearHistory()
                            fragment.refreshList()
                        }
                        noButton { }
                    }.show()
                }
            }
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_read_list, menu)
        return true
    }
}
