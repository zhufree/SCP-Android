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
import info.free.scp.bean.SimpleScp
import info.free.scp.db.ScpDatabase
import info.free.scp.db.ScpDataHelper
import info.free.scp.util.EventUtil
import info.free.scp.util.EventUtil.clickHistoryList
import info.free.scp.util.EventUtil.clickLaterList
import info.free.scp.util.EventUtil.importReadList
import info.free.scp.util.Toaster
import info.free.scp.view.detail.DetailActivity
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.base.BaseAdapter
import kotlinx.android.synthetic.main.activity_like.*
import kotlinx.android.synthetic.main.layout_dialog_report.view.*

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
    val viewItemList = emptyList<SimpleScp?>().toMutableList()
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
                    if (viewType == LATER_TYPE) {
                        showInputListDialog()
                    } else {
                        Toaster.show("请在待读列表页点击此按钮")
                    }
                }
            }
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_read_list, menu)
        return true
    }

    private fun showInputListDialog() {
        val inputView = LayoutInflater.from(this)
                .inflate(R.layout.layout_dialog_input_large, null)
        val inputDialog = AlertDialog.Builder(this)
                .setTitle(R.string.menu_import_read_list)
                .setMessage("导入的文章标题用逗号分隔，标题内需要包含cn，j等关键词作为区分，可能会出错，请及时向开发者反馈，谢谢。")
                .setView(inputView)
                .setPositiveButton("OK") { _, _ -> }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .create()
        inputDialog.show()
        inputDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            val inputString = inputView.et_report.text.toString()
            splitReadList(inputString)
            inputDialog.dismiss()
            Toaster.show("导入完成")
            EventUtil.onEvent(this, importReadList)
        }
    }

    private fun splitReadList(input: String) {
        val titleList = input.split(",")
        if (titleList.isEmpty()) return
        titleList.forEach { str ->
            var type = SCPConstants.ScpType.SAVE_SERIES
            var numberString = ""
            if (str.contains("cn") || str.contains("CN")) {
                type = SCPConstants.ScpType.SAVE_SERIES_CN
            }
            str.forEach {
                if (it.isDigit()) {
                    numberString += it
                } else {
                    if (it == 'j' || it == 'J') {
                        type = if (type == SCPConstants.ScpType.SAVE_SERIES) SAVE_JOKE else SAVE_JOKE_CN
                    }
                }
            }
            if (numberString.isNotEmpty()) {
                val targetScp = ScpDatabase.getInstance().scpDao().getScpByNumber(type, if (type ==SAVE_JOKE || type == SAVE_JOKE_CN)
                    "-$numberString-" else "-$numberString")
                if (targetScp != null) {
                    print(targetScp)
                    ScpDataHelper.getInstance().insertViewListItem(targetScp.link, targetScp.title, LATER_TYPE)
                }
            }
        }
    }
}
