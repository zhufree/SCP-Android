package info.free.scp.view.home

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import info.free.scp.R
import android.widget.ArrayAdapter
import info.free.scp.SCPConstants.LATER_TYPE
import info.free.scp.SCPConstants.ScpType.SAVE_JOKE
import info.free.scp.SCPConstants.ScpType.SAVE_JOKE_CN
import info.free.scp.SCPConstants.ScpType.SAVE_SERIES
import info.free.scp.SCPConstants.ScpType.SAVE_SERIES_CN
import info.free.scp.db.ScpDao
import info.free.scp.util.Toaster
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.detail.DetailActivity
import kotlinx.android.synthetic.main.activity_direct.*
import kotlinx.android.synthetic.main.layout_dialog_report.view.*


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
            chooseType = if (chooseType == 2) 3 else 1
            updateExpress()
        }
        btn_direct_j.setOnClickListener {
            chooseType = if (chooseType == 1) 3 else 2
            updateExpress()
        }
        tv_go_direst_btn?.setOnClickListener {
            val scp = when (chooseType) {
                0 -> {
                    ScpDao.getInstance().getScpByTypeAndNumber(SAVE_SERIES, numberString)
                }
                1 -> ScpDao.getInstance().getScpByTypeAndNumber(SAVE_SERIES_CN, numberString)
                2 -> ScpDao.getInstance().getScpByTypeAndNumber(SAVE_JOKE, numberString)
                3 -> ScpDao.getInstance().getScpByTypeAndNumber(SAVE_JOKE_CN, numberString)
                else -> ScpDao.getInstance().getScpByTypeAndNumber(SAVE_SERIES, numberString)
            }
            scp?.let { s ->
                val intent = Intent()
                intent.putExtra("link", s.link)
                intent.putExtra("sId", s.sId)
                intent.setClass(this, DetailActivity::class.java)
                startActivity(intent)
            } ?: Toaster.show("没有这篇文章")

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
                    startActivity(Intent(this, DetailActivity::class.java))
                }
                // FIXME 这部分逻辑还是要放到正文里，因为上下章切换要保持在范围内
                R.id.random_scp -> {
                    val targetScp = ScpDao.getInstance().getRandomScp("1,2")
                    targetScp?.let {
                        val intent = Intent()
                        intent.putExtra("link", targetScp.link)
                        intent.setClass(this, DetailActivity::class.java)
                        startActivity(intent)
                    }?: Toaster.show("没有离线的该部分内容，无法随机")
                }
                R.id.random_tales -> {
                    val targetScp = ScpDao.getInstance().getRandomScp("3,4")
                    targetScp?.let {
                        val intent = Intent()
                        intent.putExtra("link", targetScp.link)
                        intent.setClass(this, DetailActivity::class.java)
                        startActivity(intent)
                    }?: Toaster.show("没有离线的该部分内容，无法随机")
                }
                R.id.random_joke -> {
                    val targetScp = ScpDao.getInstance().getRandomScp("5,6")
                    targetScp?.let {
                        val intent = Intent()
                        intent.putExtra("link", targetScp.link)
                        intent.setClass(this, DetailActivity::class.java)
                        startActivity(intent)
                    }?: Toaster.show("没有离线的该部分内容，无法随机")
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
