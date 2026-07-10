package info.free.scp.view.user

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import info.free.scp.R
import info.free.scp.SCPConstants.HISTORY_TYPE
import info.free.scp.SCPConstants.OrderType.ASC
import info.free.scp.bean.ScpRecordModel
import info.free.scp.components.CommonTopBar
import info.free.scp.ui.MainTheme
import info.free.scp.ui.later.RecordListScreen
import info.free.scp.util.ThemeUtil
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.detail.DetailActivity
import info.free.scp.view.later.LaterViewModel

class LaterAndHistoryActivity : BaseActivity() {
    private val viewModel by viewModels<LaterViewModel>()
    private var orderType by mutableIntStateOf(ASC)
    private var records by mutableStateOf<List<ScpRecordModel>>(emptyList())
    private var scrollToTopSignal by mutableIntStateOf(0)
    private var showClearDialog by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refreshList()
        setContent {
            MainTheme {
                Scaffold(
                    topBar = {
                        CommonTopBar(
                            title = "\u9605\u8bfb\u5386\u53f2",
                            actions = {
                                IconButton(onClick = { reverseOrder() }) {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_swap_vertical_circle_white_24dp),
                                        tint = Color(ThemeUtil.lightText),
                                        contentDescription = "Reverse"
                                    )
                                }
                                IconButton(onClick = { showClearDialog = true }) {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_delete_forever_white_24dp),
                                        tint = Color(ThemeUtil.lightText),
                                        contentDescription = "Clear history"
                                    )
                                }
                            },
                            onBackPressed = { finish() }
                        )
                    },
                    backgroundColor = Color(ThemeUtil.containerBg)
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        RecordListScreen(
                            records = records,
                            scrollToTopSignal = scrollToTopSignal,
                            onOpenRecord = { openRecord(it) },
                            onDeleteRecord = { deleteRecord(it) }
                        )
                    }
                }

                if (showClearDialog) {
                    AlertDialog(
                        onDismissRequest = { showClearDialog = false },
                        title = { Text(text = "Notice") },
                        text = { Text(text = "\u4f60\u786e\u5b9a\u8981\u6e05\u9664\u6240\u6709\u7684\u9605\u8bfb\u8bb0\u5f55\u5417\uff1f") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    clearHistory()
                                    showClearDialog = false
                                }
                            ) {
                                Text(text = "\u786e\u5b9a")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showClearDialog = false }) {
                                Text(text = "\u53d6\u6d88")
                            }
                        }
                    )
                }
            }
        }
    }

    private fun refreshList() {
        records = viewModel.getRecordList(HISTORY_TYPE, orderType)
    }

    private fun reverseOrder() {
        orderType = -orderType
        refreshList()
        scrollToTopSignal += 1
    }

    private fun openRecord(record: ScpRecordModel) {
        startActivity(
            Intent(this, DetailActivity::class.java)
                .putExtra("link", record.link)
                .putExtra("title", record.link)
        )
    }

    private fun deleteRecord(record: ScpRecordModel) {
        viewModel.deleteRecord(record)
        refreshList()
    }

    private fun clearHistory() {
        viewModel.clearHistory()
        refreshList()
    }
}
