package info.free.scp.view.category

import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import info.free.scp.R
import info.free.scp.util.ThemeUtil
import info.free.scp.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_category.*

/**
 * 二级目录，直接scp列表，套scpListFragment
 */
class ScpListActivity : BaseActivity() {
    private var categoryType = -1
    private var clickPosition = -1
    private var mFragment: ScpListFragment? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        baseToolbar = category_toolbar
        category_toolbar?.title = "文档目录"

        initData()

        category_toolbar?.inflateMenu(R.menu.category_menu) //设置右上角的填充菜单
        category_toolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.reverse -> {
//                    mFragment?.reverseScpList()
                }
            }
            true
        }
    }

    override fun refreshTheme() {
        super.refreshTheme()
        mFragment?.refreshTheme()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
        }
        return false
    }

    private fun initData() {
        categoryType = intent.getIntExtra("category_type", -1)
        clickPosition = intent.getIntExtra("click_position", -1)
//        mFragment = ScpListFragment.newInstance(categoryType, clickPosition)
        mFragment?.let {
            supportFragmentManager.beginTransaction().replace(R.id.fl_scp_list_container, it).commit()
        }
        // 一级目录
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.category_menu, menu)
        return true
    }

}
