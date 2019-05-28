package info.free.scp.view.home

import android.os.Bundle
import android.view.Menu
import info.free.scp.R
import android.widget.ArrayAdapter
import info.free.scp.SCPConstants.ScpType.SAVE_JOKE
import info.free.scp.SCPConstants.ScpType.SAVE_JOKE_CN
import info.free.scp.SCPConstants.ScpType.SAVE_SERIES
import info.free.scp.SCPConstants.ScpType.SAVE_SERIES_CN
import info.free.scp.db.ScpDatabase
import info.free.scp.db.ScpDataHelper
import info.free.scp.util.EventUtil
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.detail.DetailActivity
import kotlinx.android.synthetic.main.activity_direct.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast


/**
 * 编号直达页
 */
class DirectActivity : BaseActivity() {
    private var chooseType = 0 // 0 scp 1 cn 2 J 3 cn-j
        set(value) {
            field = value
            cnString = when (value) {
                0 -> ""
                1 -> "CN-"
                3 -> "CN-"
                else -> ""
            }
            jString = when (value) {
                0 -> ""
                2 -> "-J"
                3 -> "-J"
                else -> ""
            }
        }
    private var cnString = "" // CN / J
    private var jString = "" // CN / J
    private var numberString = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_direct)
        initToolbar()
        val numberList = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "C", "0", "<-") //准备字符串数组or别的什么
        val numberAdapter = ArrayAdapter(this, R.layout.item_direct_number, R.id.tv_direct_number, numberList)
        gv_direct_number?.adapter = numberAdapter
        gv_direct_number?.setOnItemClickListener { _, _, position, _ ->
            if (position < 9) {
                numberString += (position + 1)
            } else if (position == 10) {
                numberString += "0"
            } else if (position == 9) {
                chooseType = 0
                numberString = ""
                cnString = ""
            } else if (position == 11 && numberString.isNotEmpty()) {
                numberString = numberString.substring(0, numberString.length - 1)
            }
            updateExpress()
        }

        btn_direct_cn.setOnClickListener {
            chooseType = when (chooseType) {
                0 -> 1
                1 -> 0
                2 -> 3
                3 -> 2
                else -> 1
            }
            updateExpress()
        }
        btn_direct_j.setOnClickListener {
            chooseType = when (chooseType) {
                0 -> 2
                1 -> 3
                2 -> 0
                3 -> 1
                else -> 2
            }
            updateExpress()
        }
        tv_go_direst_btn?.setOnClickListener {
            val scp = when (chooseType) {
                0 -> ScpDatabase.getInstance()?.scpDao()?.getScpByNumber(SAVE_SERIES, "%-$numberString %")
                1 -> ScpDatabase.getInstance()?.scpDao()?.getScpByNumber(SAVE_SERIES_CN, "%-$numberString %")
                2 -> ScpDatabase.getInstance()?.scpDao()?.getScpByNumber(SAVE_JOKE, "%-$numberString-%")
                3 -> ScpDatabase.getInstance()?.scpDao()?.getScpByNumber(SAVE_JOKE_CN, "%-$numberString-%")
                else -> ScpDatabase.getInstance()?.scpDao()?.getScpByNumber(SAVE_SERIES, "%-$numberString")
            }
            scp?.let { s ->
                EventUtil.onEvent(this, EventUtil.clickDirect)
                startActivity<DetailActivity>("link" to s.link)
            } ?: toast("没有这篇文章")

        }
    }

    private fun initToolbar() {
        setSupportActionBar(direct_toolbar)
        supportActionBar?.setTitle(R.string.title_direct)
        direct_toolbar?.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        direct_toolbar?.setNavigationOnClickListener {
            finish()
        }
        direct_toolbar?.inflateMenu(R.menu.direct_menu) //设置右上角的填充菜单
        direct_toolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.random_all -> {
                    EventUtil.onEvent(this, EventUtil.clickRandomAll)
                    val targetScp = ScpDataHelper.getInstance().getRandomScp("")
                    targetScp?.let {
                        startActivity<DetailActivity>(
                                "link" to targetScp.link,
                                "read_type" to 1,
                                "random_type" to 0)
                    } ?: toast("没有离线的文档，无法随机")
                }
                R.id.random_scp -> {
                    EventUtil.onEvent(this, EventUtil.clickRandomScp)
                    val targetScp = ScpDataHelper.getInstance().getRandomScp("1,2")
                    targetScp?.let {
                        startActivity<DetailActivity>(
                                "link" to targetScp.link,
                                "read_type" to 1,
                                "random_type" to 1)
                    } ?: toast("没有离线的该部分内容，无法随机")
                }
                R.id.random_tales -> {
                    EventUtil.onEvent(this, EventUtil.clickRandomTale)
                    val targetScp = ScpDataHelper.getInstance().getRandomScp("3,4")
                    targetScp?.let {
                        startActivity<DetailActivity>(
                                "link" to targetScp.link,
                                "read_type" to 1,
                                "random_type" to 2)
                    } ?: toast("没有离线的该部分内容，无法随机")
                }
                R.id.random_joke -> {
                    EventUtil.onEvent(this, EventUtil.clickRandomJoke)
                    val targetScp = ScpDataHelper.getInstance().getRandomScp("5,6")
                    targetScp?.let {
                        startActivity<DetailActivity>(
                                "link" to targetScp.link,
                                "read_type" to 1,
                                "random_type" to 3)
                    } ?: toast("没有离线的该部分内容，无法随机")
                }
            }
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.direct_menu, menu)
        return true
    }

    private fun updateExpress() {
        tv_direct_title.text = "SCP-$cnString$numberString$jString"
    }
}
