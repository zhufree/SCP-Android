package info.free.scp.view.home

import android.content.Intent
import android.os.Bundle
import info.free.scp.R
import android.widget.ArrayAdapter
import info.free.scp.SCPConstants.SaveType.SAVE_JOKE
import info.free.scp.SCPConstants.SaveType.SAVE_JOKE_CN
import info.free.scp.SCPConstants.SaveType.SAVE_SERIES
import info.free.scp.SCPConstants.SaveType.SAVE_SERIES_CN
import info.free.scp.db.ScpDao
import info.free.scp.util.Toaster
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.detail.DetailActivity
import kotlinx.android.synthetic.main.activity_direct.*


class DirectActivity : BaseActivity() {
    private var chooseType = 0 // 0 scp 1 cn 2 J 3 cn-j
        set(value) {
            field = value
            typeString = when (value) {
                0 -> "-"
                1 -> "-CN"
                2 -> "-J"
                3 -> "-CN-J"
                else -> "-"
            }
        }
    private var typeString = "" // CN / J
    private var numberString = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_direct)
        val numberList = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "C", "0", "<-") //准备字符串数组or别的什么
        val numberAdapter = ArrayAdapter(this, R.layout.item_direct_number, R.id.tv_direct_number, numberList)
        gv_direct_number?.adapter = numberAdapter
        gv_direct_number?.setOnItemClickListener { _, _, position, _ ->
            if (position < 9 || position == 10) {
                numberString += (position + 1)
            } else if (position == 9) {
                chooseType = 0
                numberString = ""
                typeString = ""
            } else if (position == 11) {
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
                    // TODO 查找并跳转
                    ScpDao.getInstance().getScpByTypeAndNumber(SAVE_SERIES, numberString)
                }
                1 -> ScpDao.getInstance().getScpByTypeAndNumber(SAVE_SERIES_CN, numberString)
                2 -> ScpDao.getInstance().getScpByTypeAndNumber(SAVE_JOKE, numberString)
                3 -> ScpDao.getInstance().getScpByTypeAndNumber(SAVE_JOKE_CN, numberString)
                else -> ScpDao.getInstance().getScpByTypeAndNumber(SAVE_SERIES, numberString)
            }
            scp?.let {s->
                val intent = Intent()
                intent.putExtra("link", s.link)
                intent.putExtra("sId", s.sId)
                intent.setClass(this, DetailActivity::class.java)
                startActivity(intent)
            }?:Toaster.show("没有这篇文章")

        }
    }

    private fun updateExpress() {
        tv_direct_title.text = "SCP$typeString-$numberString"
    }
}
