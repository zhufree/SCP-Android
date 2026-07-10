package info.free.scp.view.category

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import info.free.scp.SCPConstants.Category.CONTEST
import info.free.scp.SCPConstants.Category.CONTEST_CN
import info.free.scp.SCPConstants.Category.JOKE
import info.free.scp.SCPConstants.Category.SCP_EX
import info.free.scp.SCPConstants.Category.SETTINGS
import info.free.scp.SCPConstants.Category.SETTINGS_CN
import info.free.scp.SCPConstants.Category.STORY_SERIES
import info.free.scp.SCPConstants.Category.STORY_SERIES_CN
import info.free.scp.SCPConstants.Category.TALES
import info.free.scp.SCPConstants.Category.TALES_BY_TIME
import info.free.scp.SCPConstants.Category.TALES_CN
import info.free.scp.SCPConstants.Category.WANDER
import info.free.scp.SCPConstants.Category.WANDER_CN
import info.free.scp.SCPConstants.Entry.SCP_CN_DOC
import info.free.scp.SCPConstants.Entry.SCP_DOC
import info.free.scp.SCPConstants.Entry.STORY_DOC
import info.free.scp.SCPConstants.Entry.WANDER_DOC
import info.free.scp.SCPConstants.ScpType.SAVE_CANON
import info.free.scp.SCPConstants.ScpType.SAVE_CANON_CN
import info.free.scp.SCPConstants.ScpType.SAVE_CONTEST
import info.free.scp.SCPConstants.ScpType.SAVE_CONTEST_CN
import info.free.scp.SCPConstants.ScpType.SAVE_EX
import info.free.scp.SCPConstants.ScpType.SAVE_EX_CN
import info.free.scp.SCPConstants.ScpType.SAVE_JOKE
import info.free.scp.SCPConstants.ScpType.SAVE_JOKE_CN
import info.free.scp.SCPConstants.ScpType.SAVE_SERIES
import info.free.scp.SCPConstants.ScpType.SAVE_SERIES_CN
import info.free.scp.SCPConstants.ScpType.SAVE_STORY_SERIES
import info.free.scp.SCPConstants.ScpType.SAVE_STORY_SERIES_CN
import info.free.scp.SCPConstants.ScpType.SAVE_TALES
import info.free.scp.SCPConstants.ScpType.SAVE_TALES_BY_TIME
import info.free.scp.SCPConstants.ScpType.SAVE_TALES_CN
import info.free.scp.SCPConstants.ScpType.SAVE_WANDER
import info.free.scp.SCPConstants.ScpType.SAVE_WANDER_CN
import info.free.scp.components.CommonTopBar
import info.free.scp.ui.MainTheme
import info.free.scp.ui.category.GroupListScreen
import info.free.scp.util.ThemeUtil
import info.free.scp.view.base.BaseActivity

class GroupListActivity : BaseActivity() {
    private var entryType = SCP_DOC
    private var selectedSeries by mutableIntStateOf(1)
    private val categoryCount = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        entryType = intent.getIntExtra("entry_type", -1)
        selectedSeries = getInitialSeries()
        setContent {
            MainTheme {
                Scaffold(
                    topBar = {
                        CommonTopBar(
                            title = getTitlePrefix(),
                            onBackPressed = { finish() }
                        )
                    },
                    backgroundColor = Color(ThemeUtil.containerBg)
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        GroupListScreen(
                            seriesItems = getSeriesItems(),
                            selectedSeries = selectedSeries,
                            groupTitle = getSeriesTitle(selectedSeries),
                            groups = getGroupListTitle(selectedSeries),
                            onSelectSeries = { switchSeries(it) },
                            onOpenGroup = { title, index -> openGroup(title, index) }
                        )
                    }
                }
            }
        }
    }

    private fun getInitialSeries(): Int {
        return when (entryType) {
            STORY_DOC -> TALES
            WANDER_DOC -> WANDER
            else -> 1
        }
    }

    private fun getTitlePrefix(): String {
        return when (entryType) {
            SCP_DOC -> "SCP\u7cfb\u5217"
            SCP_CN_DOC -> "SCP-CN\u7cfb\u5217"
            STORY_DOC -> "\u6545\u4e8b"
            WANDER_DOC -> "\u653e\u9010\u8005\u56fe\u4e66\u9986"
            else -> "SCP\u7cfb\u5217"
        }
    }

    private fun getSeriesTitle(index: Int): String {
        return when (index) {
            JOKE -> "\u641e\u7b11SCP"
            SCP_EX -> "\u5df2\u89e3\u660eSCP"
            TALES -> "\u57fa\u91d1\u4f1a\u6545\u4e8b"
            TALES_CN -> "CN\u539f\u521b\u6545\u4e8b"
            TALES_BY_TIME -> "CN\u539f\u521b\u6545\u4e8b(\u6309\u53d1\u5e03\u65f6\u95f4\u6392\u5217)"
            SETTINGS -> "\u8bbe\u5b9a\u4e2d\u5fc3"
            SETTINGS_CN -> "CN\u8bbe\u5b9a\u4e2d\u5fc3"
            STORY_SERIES -> "\u6545\u4e8b\u7cfb\u5217"
            STORY_SERIES_CN -> "CN\u6545\u4e8b\u7cfb\u5217"
            CONTEST -> "\u5f81\u6587\u7ade\u8d5b"
            CONTEST_CN -> "CN\u5f81\u6587\u7ade\u8d5b"
            WANDER -> "\u653e\u9010\u8005\u56fe\u4e66\u9986"
            WANDER_CN -> "CN\u653e\u9010\u8005\u56fe\u4e66\u9986"
            else -> "\u7cfb\u5217$index"
        }
    }

    private fun getSeriesItems(): List<Pair<Int, String>> {
        val series = when (entryType) {
            SCP_DOC -> (1..8).toList() + listOf(JOKE, SCP_EX)
            SCP_CN_DOC -> (1..3).toList() + listOf(JOKE, SCP_EX)
            STORY_DOC -> listOf(
                TALES,
                TALES_CN,
                TALES_BY_TIME,
                SETTINGS,
                SETTINGS_CN,
                STORY_SERIES,
                STORY_SERIES_CN,
                CONTEST,
                CONTEST_CN
            )
            WANDER_DOC -> listOf(WANDER, WANDER_CN)
            else -> emptyList()
        }
        return series.map { it to getSeriesTitle(it) }
    }

    private fun getSaveType(type: Int): Int {
        return when (entryType) {
            SCP_DOC -> when (type) {
                JOKE -> SAVE_JOKE
                SCP_EX -> SAVE_EX
                else -> SAVE_SERIES
            }
            SCP_CN_DOC -> when (type) {
                JOKE -> SAVE_JOKE_CN
                SCP_EX -> SAVE_EX_CN
                else -> SAVE_SERIES_CN
            }
            STORY_DOC -> when (type) {
                TALES -> SAVE_TALES
                TALES_CN -> SAVE_TALES_CN
                TALES_BY_TIME -> SAVE_TALES_BY_TIME
                SETTINGS -> SAVE_CANON
                SETTINGS_CN -> SAVE_CANON_CN
                STORY_SERIES -> SAVE_STORY_SERIES
                STORY_SERIES_CN -> SAVE_STORY_SERIES_CN
                CONTEST -> SAVE_CONTEST
                CONTEST_CN -> SAVE_CONTEST_CN
                else -> SAVE_TALES
            }
            WANDER_DOC -> when (type) {
                WANDER -> SAVE_WANDER
                WANDER_CN -> SAVE_WANDER_CN
                else -> SAVE_WANDER
            }
            else -> SAVE_SERIES
        }
    }

    private fun getGroupListTitle(index: Int): List<String> {
        return when (index) {
            TALES, TALES_CN, WANDER, WANDER_CN -> listOf(
                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
                "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "0-9"
            )
            TALES_BY_TIME -> listOf("2014", "2015", "2016", "2017", "2018")
            else -> (0 until (1000 / categoryCount)).map {
                "${getTitlePrefix()}${(it + (index - 1) * 10) * categoryCount}+"
            }
        }
    }

    private fun switchSeries(index: Int) {
        if (index < 100) {
            selectedSeries = index
        } else {
            startActivity(
                Intent(this, DocListActivity::class.java)
                    .putExtra("saveType", getSaveType(index))
            )
        }
    }

    private fun openGroup(title: String, index: Int) {
        startActivity(
            Intent(this, DocListActivity::class.java)
                .putExtra("saveType", getSaveType(selectedSeries))
                .putExtra("groupIndex", (selectedSeries - 1) * 10 + index)
                .putExtra("extraType", title)
        )
    }
}
