package info.free.scp.view.category

import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import info.free.scp.R
import info.free.scp.bean.ScpModel
import info.free.scp.util.PreferenceUtil
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.home.ScpListFragment
import kotlinx.android.synthetic.main.activity_category.*

/**
 * 二级目录，直接scp列表，套scpListFragment
 */
class ScpListActivity : BaseActivity() {
    private val categoryList: MutableList<Any> = emptyList<Any>().toMutableList()
    private val scpList: MutableList<ScpModel> = emptyList<ScpModel>().toMutableList()
    private var categoryAdapter: CategoryAdapter? = null
    private var scpAdapter: ScpAdapter? = null
    private var categoryType = -1
    private var clickPosition = -1
    private var isCnPage = false // 是否是cn页面 归档内容部分用到
    private var onlyOneLayer = false
    private val tag = "category"
    private var currentScpPosition = -1
    private var currentCategoryPosition = -1
    private val categoryCount = PreferenceUtil.getCategoryCount()


    private val eventScpList: MutableList<ScpModel> = emptyList<ScpModel>().toMutableList()
    private val taleTimeList: MutableList<ScpModel> = emptyList<ScpModel>().toMutableList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        setSupportActionBar(category_toolbar)
        category_toolbar?.title = "文档目录"
        category_toolbar?.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        category_toolbar?.setNavigationOnClickListener {
            finish()
        }

        initData()

        category_toolbar?.inflateMenu(R.menu.category_menu) //设置右上角的填充菜单
        category_toolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.reverse -> {
                    // TODO reverse
                }
                R.id.cn_page -> {
                    isCnPage = !isCnPage
                    initData()
                }
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
//        if (pageType == 1 && currentCategoryPosition > -1 && currentCategoryPosition < categoryList.size) {
//            getScpList(currentCategoryPosition)
//            rv_scp_list?.scrollToPosition(currentScpPosition)
//        }
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
        supportFragmentManager.beginTransaction().replace(R.id.fl_scp_list_container,
                ScpListFragment.newInstance(categoryType, clickPosition)).commit()
        // 一级目录
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.category_menu, menu)
        return true
    }

}
