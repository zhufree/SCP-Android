package info.free.scp.ui.later

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.free.scp.R
import info.free.scp.bean.ScpRecordModel
import info.free.scp.ui.MainTheme
import info.free.scp.util.ThemeUtil
import java.util.Date

@Composable
fun RecordListScreen(
    records: List<ScpRecordModel>,
    scrollToTopSignal: Int,
    onOpenRecord: (ScpRecordModel) -> Unit,
    onDeleteRecord: (ScpRecordModel) -> Unit
) {
    val listState = rememberLazyListState()
    var deletingRecord by remember { mutableStateOf<ScpRecordModel?>(null) }

    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > 0) {
            listState.animateScrollToItem(0)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        items(records, key = { "${it.link}-${it.viewTime.time}" }) { record ->
            RecordListItem(
                record = record,
                onClick = { onOpenRecord(record) },
                onLongClick = { deletingRecord = record }
            )
        }
    }

    deletingRecord?.let { record ->
        AlertDialog(
            onDismissRequest = { deletingRecord = null },
            title = { Text(text = "Notice") },
            text = { Text(text = "\u662f\u5426\u786e\u8ba4\u5220\u9664\uff1f") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteRecord(record)
                        deletingRecord = null
                    }
                ) {
                    Text(text = "\u786e\u5b9a")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingRecord = null }) {
                    Text(text = "\u53d6\u6d88")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecordListItem(
    record: ScpRecordModel,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(6.dp),
        backgroundColor = Color(ThemeUtil.itemBg),
        elevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(start = 20.dp, top = 15.dp, end = 16.dp, bottom = 20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                modifier = Modifier.size(30.dp),
                painter = painterResource(R.drawable.outline_note_black_24dp),
                tint = Color(ThemeUtil.darkText),
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.title,
                    color = Color(ThemeUtil.darkText),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = record.showTime(),
                    color = Color(ThemeUtil.mediumText),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RecordListScreenPreview() {
    MainTheme {
        RecordListScreen(
            records = listOf(
                ScpRecordModel("/scp-173", "SCP-173", viewTime = Date()),
                ScpRecordModel("/scp-682", "SCP-682", viewTime = Date())
            ),
            scrollToTopSignal = 0,
            onOpenRecord = {},
            onDeleteRecord = {}
        )
    }
}
