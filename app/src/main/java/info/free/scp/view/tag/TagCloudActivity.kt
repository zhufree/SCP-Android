package info.free.scp.view.tag

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.free.scp.components.CommonTopBar
import info.free.scp.ui.MainTheme
import org.jetbrains.anko.startActivity

class TagCloudActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tagViewModel = TagViewModel()
        tagViewModel.getTags()

        setContent {
            MainTheme {
                MainApp(tagViewModel, {
                    startActivity<TagDetailActivity>("tag" to it)
                }) {
                    finish()
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MainApp(
    tagViewModel: TagViewModel,
    navigateToTagDetail: (tag: String) -> Unit,
    finish: () -> Unit
) {
    Scaffold(
        topBar = {
            CommonTopBar(title = "全部标签") {
                finish()
            }
        },
        backgroundColor = MaterialTheme.colors.background
    ) { _ ->
        val tagMap by tagViewModel.tagMap.observeAsState()
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            tagMap?.let { map ->
                val tagList = map.keys.toList().sortedByDescending { map[it] }
                if (map.size == 0) {
                    Text(
                        text = "加载中请稍候",
                        fontSize = 20.sp,
                        color = MaterialTheme.colors.onPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyVerticalGrid(columns = GridCells.Fixed(3)) {
                        items(tagList) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp), // Random height to simulate staggered grid
                                elevation = 2.dp
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp)
                                        .clickable {
                                            navigateToTagDetail(it)
                                        },
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = it,
                                        color = MaterialTheme.colors.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                    )
                                    Text(
                                        text = map[it].toString(),
                                        color = MaterialTheme.colors.onSecondary,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}