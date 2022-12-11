package info.free.scp.view.later


import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import info
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.LATER_TYPE
import info.free.scp.db.ScpDataHelper
import info.free.scp.db.ScpDatabase
import info.free.scp.util.EventUtil
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.home.TabFragmentPager
import kotlinx.android.synthetic.main.fragment_later.*
import kotlinx.android.synthetic.main.layout_dialog_input_large.view.*
import org.jetbrains.anko.support.v4.alert
import toast


/**
 * A simple [Fragment] subclass.
 * Use the [LaterFragment.newInstance] factory method to
 * create an instance of this fragment.
 * 待读和收藏tab
 */
class LaterFragment : BaseFragment() {

    var fragmentList = arrayListOf<BaseFragment>()
    private val recordListFragment = RecordListFragment.newInstance(LATER_TYPE)
    private val likeListFragment = LikeBoxListFragment.newInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_later, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentList = arrayListOf(recordListFragment, likeListFragment)
        val titleList = arrayListOf("待读", "收藏夹")
        val laterPagerAdapter =
            this.activity?.let { TabFragmentPager(it, fragmentList, fragmentList.size) }
        vp_later?.adapter = laterPagerAdapter
        TabLayoutMediator(tab_later, vp_later) { tab, position ->
            tab.text = titleList[position]
        }.attach()
        fab_import?.setOnClickListener {
            if (!PreferenceUtil.getShownReadSuggest()) {
                alert("要不要访问大佬们整理的待读列表（可以复制列表后点击此按钮导入）", "不知道读什么？") {
                    positiveButton("带我去！") {
                        val updateIntent = Intent()
                        updateIntent.action = "android.intent.action.VIEW"
                        val updateUrl =
                            Uri.parse("http://scpsandboxcn.wikidot.com/collab:to-read-list")
                        updateIntent.data = updateUrl
                        startActivity(updateIntent)
                    }
                    negativeButton("不用了") {}
                }.show()
                PreferenceUtil.setShownReadSuggest()
            }
            showInputListDialog()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {//可见时
            onVisible()
        }
    }

    private fun onVisible() {
        if (recordListFragment.isAdded) {
            recordListFragment.refreshList()
        }
    }

    override fun refreshTheme() {
        super.refreshTheme()
        tab_later?.background = ColorDrawable(ThemeUtil.itemBg)
        tab_later?.setSelectedTabIndicatorColor(ThemeUtil.accentColor)
        tab_later?.setTabTextColors(ThemeUtil.mediumText, ThemeUtil.accentColor)
        fragmentList.forEach { it.refreshTheme() }
    }

    companion object {
        fun newInstance(): LaterFragment {
            return LaterFragment()
        }
    }

    private fun showInputListDialog() {
        val inputView = LayoutInflater.from(context)
                .inflate(R.layout.layout_dialog_input_large, null)
        val inputDialog = AlertDialog.Builder(context)
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
            val inputString = inputView.et_import.text.toString()
            splitReadList(inputString)
            inputDialog.dismiss()
            EventUtil.onEvent(context, EventUtil.importReadList)
        }
    }

    private fun splitReadList(input: String) {
        var titleList = input.split(",")
        if (titleList.isEmpty() || titleList.size < 2) {
            titleList = input.split("\\s+".toRegex())
        }
        if (titleList.isEmpty() || titleList.size < 2) {
            titleList = input.split("，")
        }
        if (titleList.isEmpty() || titleList.size < 2) {
            titleList = input.split("；")
        }
        if (titleList.isEmpty() || titleList.size < 2) {
            titleList = input.split(";")
        }
        if (titleList.isEmpty()) {
            toast("识别失败，请对照要求修改格式")
            return
        }
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
                    type = if (type == SCPConstants.ScpType.SAVE_SERIES) SCPConstants.ScpType.SAVE_JOKE else SCPConstants.ScpType.SAVE_JOKE_CN
                }
            }
            if (numberString.isNotEmpty()) {
                if (numberString.length == 1) {
                    numberString = "00$numberString"
                } else if (numberString.length == 2) {
                    numberString = "0$numberString"
                }
                val targetScp = ScpDatabase.getInstance()?.scpDao()?.getScpByNumber(type,
                        if (type == SCPConstants.ScpType.SAVE_JOKE || type == SCPConstants.ScpType.SAVE_JOKE_CN)
                            "%-$numberString-%" else "%-$numberString %")
                if (targetScp != null) {
                    ScpDataHelper.getInstance().insertViewListItem(targetScp.link, targetScp.title, LATER_TYPE)
                }

            }
        }
        toast("导入完成")
        recordListFragment.refreshList()
    }
}
