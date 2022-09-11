package info.free.scp.view.tag

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import info.free.scp.ui.MainTheme
import info.free.scp.view.detail.DetailActivity
import org.jetbrains.anko.startActivity

class TagDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tag = intent.getStringExtra("tag") ?: ""
        setContent {
            MainApp(tag, {
                startActivity<DetailActivity>("link" to it)
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
                tagViewModel.getScpByTag(tag)
                TagResultScreen(tagViewModel, tag, {
                    navigateToDetail(it)
                }) {
                    finish()
                }
            }
//            composable("addNewCode/{location}") { backStackEntry ->
//                AddCodeScreen(intent, imageUri, qrUrl, backStackEntry.arguments?.getString("location")?:"null"){
//                    navController.popBackStack()
//                }
//            }
//            composable("about"){
//                AboutScreen( {
//                    navController.popBackStack()
//                }, {
//                    goUrl(it)
//                })
//            }
        }
    }
}