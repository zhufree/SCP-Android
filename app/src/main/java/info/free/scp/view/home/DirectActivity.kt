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
                R.id.random -> {
                    startActivity(Intent(this, DetailActivity::class.java))
                }
                R.id.import_read_list -> {
                    showInputListDialog()
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

    private fun showInputListDialog() {
        val inputView = LayoutInflater.from(this)
                .inflate(R.layout.layout_dialog_input_large, null)
        val inputDialog = AlertDialog.Builder(this)
                .setTitle(R.string.menu_import_read_list)
                .setMessage("导入的文章标题用逗号分隔，标题内需要包含cn，j等关键词作为区分")
                .setView(inputView)
                .setPositiveButton("OK") { _, _ -> }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .create()
        inputDialog.show()
        inputDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            val inputString = inputView.et_report.text.toString()
            Log.i("readlist", inputString)
            splitReadList(inputString)
            inputDialog.dismiss()
            Toaster.show("导入完成")
        }
    }

    private fun splitReadList(input: String) {
        val titleList = input.split(",")
        if (titleList.isEmpty()) return
        titleList.forEach { str ->
            var type = SAVE_SERIES
            var numberString = ""
            if (str.contains("cn") || str.contains("CN")) {
                type = SAVE_SERIES_CN
            }
            str.forEach {
                if (it.isDigit()) {
                    numberString += it
                } else {
                    if (it == 'j' || it == 'J') {
                        type = if (type == SAVE_SERIES) SAVE_JOKE else SAVE_JOKE_CN
                    }
                }
            }
            if (numberString.isNotEmpty()) {
                val targetScp = ScpDao.getInstance().getScpByTypeAndNumber(type, numberString)
                if (targetScp != null) {
                    print(targetScp)
                    ScpDao.getInstance().insertViewListItem(targetScp.link, targetScp.title, LATER_TYPE)
                }
            }
        }
    }
}
