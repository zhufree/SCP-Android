package info.free.scp.view.user

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.HISTORY_TYPE
import info.free.scp.SCPConstants.LATER_TYPE
import info.free.scp.SCPConstants.ScpType.SAVE_JOKE
import info.free.scp.SCPConstants.ScpType.SAVE_JOKE_CN
import info.free.scp.bean.ScpRecordModel
import info.free.scp.db.AppInfoDatabase
import info.free.scp.db.ScpDatabase
import info.free.scp.db.ScpDataHelper
import info.free.scp.util.EventUtil
import info.free.scp.util.EventUtil.clickHistoryList
import info.free.scp.util.EventUtil.clickLaterList
import info.free.scp.util.EventUtil.importReadList
import info.free.scp.view.detail.DetailActivity
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.base.BaseAdapter
import kotlinx.android.synthetic.main.activity_like.*
import kotlinx.android.synthetic.main.layout_dialog_report.view.*
import org.jetbrains.anko.*

/**
 * 待读列表
 */
class LaterAndHistoryActivity : BaseActivity() {
    private var viewType = -1
        set(value) {
            field = value
            viewItemList.clear()
            viewItemList.addAll(ScpDataHelper.getInstance().getViewListByTypeAndOrder(value, orderType))
            supportActionBar?.title = if (value == HISTORY_TYPE) "历史阅读记录" else "待读列表"
            if (value == HISTORY_TYPE) {
                EventUtil.onEvent(this, clickHistoryList)
            } else {
                EventUtil.onEvent(this, clickLaterList)
            }
            adapter?.notifyDataSetChanged()
        }
    val viewItemList = emptyList<ScpRecordModel?>().toMutableList()
    var adapter : TimeScpAdapter? = null
    private var orderType = 1 // 0 時間正序，倒序
        set(value) {
            field = value
            viewItemList.clear()
            viewItemList.addAll(ScpDataHelper.getInstance().getViewListByTypeAndOrder(viewType, value))
            adapter?.notifyDataSetChanged()
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_like)
        initToolbar()

        val lm = LinearLayoutManager(this, VERTICAL, false)
        rv_like?.layoutManager = lm
        adapter = TimeScpAdapter(this, viewItemList)
        rv_like?.adapter = adapter
        adapter?.mOnItemClickListener = object : BaseAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val intent = Intent()
                intent.putExtra("link", viewItemList[position]?.link)
                intent.setClass(this@LaterAndHistoryActivity, DetailActivity::class.java)
                startActivity(intent)
            }
        }
        viewType = intent?.getIntExtra("view_type", 0)?:0
    }

    override fun onResume() {
        super.onResume()
        viewItemList.clear()
        viewItemList.addAll(ScpDataHelper.getInstance().getViewListByTypeAndOrder(viewType, orderType))
        adapter?.notifyDataSetChanged()
    }

    private fun initToolbar() {
        setSupportActionBar(like_toolbar)
        like_toolbar?.inflateMenu(R.menu.menu_read_list)
        like_toolbar?.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        like_toolbar?.setNavigationOnClickListener { finish() }
        like_toolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.reverse -> {
                    orderType = if (orderType == 0) 1 else 0
                }
                R.id.import_read_list -> {
                    showInputListDialog()
                }
                R.id.clear_read_history -> {
                    // 清除历史记录
                    alert("你确定要清除所有的阅读记录吗", "Notice") {
                        yesButton {
                            AppInfoDatabase.getInstance().readRecordDao().clearHistory()
                            onResume()
                        }
                        noButton {  }
                    }.show()
                }
            }
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_read_list, menu)
        if (viewType == HISTORY_TYPE) {
            menu?.getItem(1)?.isVisible = false
            menu?.getItem(2)?.isVisible = true
        } else {
            menu?.getItem(1)?.isVisible = true
            menu?.getItem(2)?.isVisible = false
        }
        return true
    }

    private fun showInputListDialog() {
        val inputView = LayoutInflater.from(this)
                .inflate(R.layout.layout_dialog_input_large, null)
        val inputDialog = AlertDialog.Builder(this)
                .setTitle(R.string.menu_import_read_list)
                .setMessage("导入的文章标题用中英文逗号/中英文分号/换行分隔均可（但需保持统一），有数字即可识别，" +
                        "默认为SCP文档，其他类型文档标题内需要包含cn，j等关键词作为区分（大小写均可），" +
                        "标题格式不规则的文档建议使用搜索功能手动添加。如果导入出错请加群向开发者反馈，谢谢。")
                .setView(inputView)
                .setPositiveButton("OK") { _, _ -> }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .create()
        inputDialog.show()
        inputDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            val inputString = inputView.et_report.text.toString()
            splitReadList(inputString)
            inputDialog.dismiss()
            EventUtil.onEvent(this, importReadList)
        }
    }

    private fun splitReadList(input: String) {
        var titleList = input.split(",")
        if (titleList.isEmpty() || titleList.size < 2){
            titleList = input.split("\\s+".toRegex())
        }
        if (titleList.isEmpty() || titleList.size < 2){
            titleList = input.split("，")
        }
        if (titleList.isEmpty() || titleList.size < 2){
            titleList = input.split("；")
        }
        if (titleList.isEmpty() || titleList.size < 2){
            titleList = input.split(";")
        }
        if (titleList.isEmpty()){
            toast("识别失败，请对照要求修改格式")
            return
        }
        info(titleList)
        titleList.forEach { str ->
            info(str)
            var type = SCPConstants.ScpType.SAVE_SERIES
            var numberString = ""
            // 判断是否中分文档
            if (str.contains("cn") || str.contains("CN")) {
                type = SCPConstants.ScpType.SAVE_SERIES_CN
            }
            str.forEach {
                if (it.isDigit()) {
                    numberString += it
                } else if (it == 'j' || it == 'J') {
                    // 判断是否J文档以及中分J或者全站J
                    type = if (type == SCPConstants.ScpType.SAVE_SERIES) SAVE_JOKE else SAVE_JOKE_CN
                }
            }
            if (numberString.isNotEmpty()) {
                if (numberString.length == 1) {
                    numberString = "00$numberString"
                } else if (numberString.length == 2) {
                    numberString = "0$numberString"
                }
                val targetScp = ScpDatabase.getInstance()?.scpDao()?.getScpByNumber(type,
                        if (type ==SAVE_JOKE || type == SAVE_JOKE_CN)
                    "%-$numberString-%" else "%-$numberString %")
                if (targetScp != null) {
                    info(targetScp)
                    ScpDataHelper.getInstance().insertViewListItem(targetScp.link, targetScp.title, LATER_TYPE)
                }

            }
        }
        toast("导入完成")
        onResume()
    }
}
