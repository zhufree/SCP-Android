package info.free.scp.view.home

import android.os.Bundle
import android.view.Menu
import android.widget.ArrayAdapter
import androidx.core.view.children
import apiCall
import executeResponse
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.AppMode.OFFLINE
import info.free.scp.SCPConstants.ScpType.SAVE_JOKE
import info.free.scp.SCPConstants.ScpType.SAVE_JOKE_CN
import info.free.scp.SCPConstants.ScpType.SAVE_SERIES
import info.free.scp.SCPConstants.ScpType.SAVE_SERIES_CN
import info.free.scp.bean.ScpItemModel
import info.free.scp.db.ScpDataHelper
import info.free.scp.db.ScpDatabase
import info.free.scp.service.HttpManager
import info.free.scp.util.EventUtil
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.detail.DetailActivity
import kotlinx.android.synthetic.main.activity_direct.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.doAsync
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

        tv_direct_cn.setOnClickListener {
            chooseType = when (chooseType) {
                0 -> 1
                1 -> 0
                2 -> 3
                3 -> 2
                else -> 1
            }
            updateExpress()
        }
        tv_direct_j.setOnClickListener {
            chooseType = when (chooseType) {
                0 -> 2
                1 -> 3
                2 -> 0
                3 -> 1
                else -> 2
            }
            updateExpress()
        }
        tv_direct_c.setOnClickListener {
            if (numberString.isNotEmpty()) {
                numberString = numberString.substring(0, numberString.length - 1)
                updateExpress()
            }
        }
        gl_direct_btns.children.forEachIndexed { index, view ->
            if (index > 2) {
                view.setOnClickListener {
                    when (index - 3) {
                        in 0..8 -> {
                            numberString += (index - 2)
                            updateExpress()
                        }
                        9 -> {
                            numberString += "0"
                            updateExpress()
                        }
                        10 -> {
                            GlobalScope.launch { goDirect() }
                        }
                        else -> {
                        }
                    }
                }
            }
        }
    }

    private suspend fun goDirect() {
        val scpType = when (chooseType) {
            0 -> SAVE_SERIES
            1 -> SAVE_SERIES_CN
            2 -> SAVE_JOKE
            3 -> SAVE_JOKE_CN
            else -> SAVE_SERIES
        }
        val number = when (chooseType) {
            0 -> "%-$numberString %"
            1 -> "%-$numberString %"
            2 -> "%-$numberString-%"
            3 -> "%-$numberString-%"
            else -> "%-$numberString"
        }
        if (PreferenceUtil.getAppMode() == OFFLINE) {
            val scp = ScpDatabase.getInstance()?.scpDao()?.getScpByNumber(scpType, number)
            scp?.let { s ->
                EventUtil.onEvent(this, EventUtil.clickDirect)
                startActivity<DetailActivity>("link" to s.link)
            } ?: runOnUiThread { toast("没有这篇文章") }

        } else {
            loadDirect(scpType, number)
        }
    }

    private suspend fun loadDirect(scpType: Int, number: String) {
        val response = apiCall { HttpManager.instance.getDirect(scpType, number) }
        response?.let {
            executeResponse(response, {

            }, {
                if (!response.results.isNullOrEmpty()) {
                    val scp = response.results[0]
                    EventUtil.onEvent(this, EventUtil.clickDirect)
                    startActivity<DetailActivity>("link" to scp.link, "title" to scp.title)
                } else {
                    toast("没有这篇文章")
                }
            })
        }
    }

    private fun initToolbar() {
        baseToolbar = direct_toolbar
        supportActionBar?.setTitle(R.string.title_direct)
        direct_toolbar?.inflateMenu(R.menu.direct_menu) //设置右上角的填充菜单
        direct_toolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.random_all -> {
                    EventUtil.onEvent(this, EventUtil.clickRandomAll)
                    startActivity<DetailActivity>(
                            "read_type" to 1,
                            "random_type" to 0)
                }
                R.id.random_scp -> {
                    EventUtil.onEvent(this, EventUtil.clickRandomScp)
                    startActivity<DetailActivity>(
                            "read_type" to 1,
                            "random_type" to 1)
                }
                R.id.random_tales -> {
                    EventUtil.onEvent(this, EventUtil.clickRandomTale)
                    startActivity<DetailActivity>(
                            "read_type" to 1,
                            "random_type" to 2)
                }
                R.id.random_joke -> {
                    EventUtil.onEvent(this, EventUtil.clickRandomJoke)
                    startActivity<DetailActivity>(
                            "read_type" to 1,
                            "random_type" to 3)
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
