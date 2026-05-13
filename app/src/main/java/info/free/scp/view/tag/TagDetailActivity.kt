package info.free.scp.view.tag

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import info.free.scp.ui.MainTheme
import info.free.scp.view.detail.DetailActivity

class TagDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tag = intent.getStringExtra("tag") ?: ""
        setContent {
            MainApp(tag, {
                startActivity(Intent(this, DetailActivity::class.java).putExtra("link", it))
            }) {
                finish()
            }
        }
    }
}

@Composable
fun MainApp(tag: String = "", navigateToDetail: (url: String) -> Unit, finish: () -> Unit) {
    MainTheme {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "tag") {
            val tagViewModel = TagViewModel()
            composable("tag") {
                LaunchedEffect(Unit) {
                    tagViewModel.getScpByTag(tag)
                }
                TagResultScreen(tagViewModel, tag, {
                    navigateToDetail(it)
                }) {
                    finish()
                }
            }
        }
    }
}
