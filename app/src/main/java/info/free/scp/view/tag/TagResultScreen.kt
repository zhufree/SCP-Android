package info.free.scp.view.tag

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.free.scp.bean.ScpModel
import info.free.scp.components.CommonTopBar

@Composable
fun TagResultScreen(
    tagViewModel: TagViewModel,
    tagName: String,
    navigateToDetail: (link: String) -> Unit,
    tagTextProvider: (link: String) -> String? = { link -> tagViewModel.getTagText(link) },
    onBackPressed: () -> Unit
) {
    val scpResult by tagViewModel.tagScpList.observeAsState()
    Scaffold(topBar = {
        CommonTopBar("标签：$tagName") {
            onBackPressed()
        }
    }) { p ->
        Column(
            modifier = Modifier
                .padding(p)
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            scpResult?.let { scpList ->
                if (scpList.isEmpty()) {
                    Text(
                        text = "加载中请稍候",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                LazyVerticalGrid(
                    GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(scpList) {
                        TagScpCard(it, tagTextProvider(it.link)) { scp ->
                            navigateToDetail(scp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TagScpCard(
    scp: ScpModel,
    tagText: String?,
    navigateToDetail: (link: String) -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        onClick = {
            navigateToDetail(scp.link)
        },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Text(
                scp.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (!tagText.isNullOrEmpty()) {
                Text(
                    text = ("标签：" + tagText),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
