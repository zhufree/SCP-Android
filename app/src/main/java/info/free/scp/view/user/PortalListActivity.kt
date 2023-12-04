package info.free.scp.view.user

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import eth.zhufree.baihehub.ui.hehuanHong
import eth.zhufree.baihehub.ui.shizhuHong
import info.free.scp.bean.GameModel
import info.free.scp.components.CommonTopBar
import info.free.scp.ui.MainTheme
import info.free.scp.util.EventUtil
import info.free.scp.util.Utils
import info.free.scp.view.base.BaseActivity
import info.free.scp.viewmodel.FireViewModel
import kotlinx.coroutines.launch

/**
 * compose
 */
class PortalListActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventUtil.onEvent(this, EventUtil.clickGameList)
        val portalViewModel = FireViewModel()
        portalViewModel.getPortals()
        setContent {
            MainTheme {
                PortalPage(portalViewModel) {
                    finish()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun PortalPage(portalViewModel: FireViewModel, finish: () -> Unit) {
    Scaffold(
        topBar = {
            CommonTopBar(title = "传送门") {
                finish()
            }
        },
        backgroundColor = MaterialTheme.colors.background
    ) { _ ->
        val portals by portalViewModel.portalList.observeAsState()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            portals?.forEach {
                Card(elevation = 2.dp, onClick = {
                    Utils.openUrl(it.url)
                }, backgroundColor = MaterialTheme.colors.onBackground) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        AsyncImage(
                            model = it.logoUrl,
                            contentDescription = "",
                            modifier = Modifier.height(150.dp)
                        )
                        Text(
                            text = it.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

