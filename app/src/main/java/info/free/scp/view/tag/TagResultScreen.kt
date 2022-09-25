package info.free.scp.view.tag

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.free.scp.bean.ScpItemModel
import info.free.scp.bean.ScpModel
import info.free.scp.components.CommonTopBar
import info.free.scp.db.DetailDatabase

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun TagResultScreen(
    tagViewModel: TagViewModel,
    tagName: String,
    navigateToDetail: (link: String) -> Unit,
    onBackPressed: () -> Unit
) {
    val scpResult by tagViewModel.tagScpList.observeAsState()
    Scaffold(topBar = {
        CommonTopBar("标签：$tagName") {
            onBackPressed()
        }
    }) { p ->
        scpResult?.let { scpList ->
            LazyVerticalGrid(
                GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(scpList) {
                    TagScpCard(it) {
                        navigateToDetail(it)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TagScpCard(scp: ScpModel, navigateToDetail: (link: String) -> Unit) {
    val detailDao = DetailDatabase.getInstance()?.detailDao()
    Card(
        elevation = 1.dp,
        shape = RoundedCornerShape(6.dp),
        backgroundColor = MaterialTheme.colors.onBackground,
        onClick = {
            navigateToDetail(scp.link)
        }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .height(100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                scp.title,
                color = MaterialTheme.colors.onPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (detailDao != null) {
                Text(
                    text = ("标签：" + detailDao.getTag(scp.link)),
                    color = MaterialTheme.colors.onSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}