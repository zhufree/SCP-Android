package info.free.scp.view.later

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import info.free.scp.R
import info.free.scp.SCPConstants.LATER_TYPE
import info.free.scp.data.catalog.ScpCatalogRepository
import info.free.scp.data.reading.ReadListImportParser
import info.free.scp.data.reading.ReadingListRepository
import info.free.scp.ui.MainTheme
import info.free.scp.ui.home.SearchBarScreen
import info.free.scp.util.EventUtil
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.util.Utils
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.home.DirectActivity
import info.free.scp.view.random.RandomActivity
import info.free.scp.view.search.SearchActivity
import toast

class LaterFragment : BaseFragment() {
    private val recordListFragment = RecordListFragment.newInstance(LATER_TYPE)
    private val likeListFragment = LikeBoxListFragment.newInstance()
    private val readingListRepository = ReadingListRepository()
    private val catalogRepository = ScpCatalogRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                var selectedTab by remember { mutableIntStateOf(0) }
                MainTheme {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 0.dp)
                        ) {
                            SearchBarScreen(
                                onDirect = { startActivity(Intent(requireContext(), DirectActivity::class.java)) },
                                onRandom = { showRandomDialog() },
                                onSearch = { keyword -> openSearch(keyword) }
                            )
                            TabRow(
                                selectedTabIndex = selectedTab,
                                backgroundColor = Color(ThemeUtil.itemBg),
                                contentColor = Color(ThemeUtil.accentColor)
                            ) {
                                listOf("\u5f85\u8bfb", "\u6536\u85cf\u5939").forEachIndexed { index, title ->
                                    Tab(
                                        selected = selectedTab == index,
                                        onClick = { selectedTab = index },
                                        text = { Text(text = title) }
                                    )
                                }
                            }
                            AndroidView(
                                modifier = Modifier.weight(1f),
                                factory = { context ->
                                    android.widget.FrameLayout(context).apply {
                                        id = R.id.fl_later_container
                                    }
                                }
                            )
                        }
                        FloatingActionButton(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(24.dp),
                            backgroundColor = MaterialTheme.colors.primary,
                            onClick = { handleImportClick() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Import",
                                tint = Color.White
                            )
                        }
                    }
                }
                LaunchedEffect(selectedTab) {
                    showChildFragment(if (selectedTab == 0) recordListFragment else likeListFragment)
                }
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && recordListFragment.isAdded) {
            recordListFragment.refreshList()
        }
    }

    private fun showChildFragment(fragment: Fragment) {
        if (childFragmentManager.isStateSaved) {
            return
        }
        childFragmentManager.beginTransaction()
            .replace(R.id.fl_later_container, fragment)
            .commit()
        if (fragment === recordListFragment && recordListFragment.isAdded) {
            recordListFragment.refreshList()
        }
    }

    private fun openSearch(keyword: String) {
        if (keyword.isEmpty()) {
            toast("\u8bf7\u8f93\u5165\u6b63\u786e\u7684\u5173\u952e\u8bcd")
            return
        }
        startActivity(
            Intent(requireContext(), SearchActivity::class.java)
                .putExtra("keyword", keyword)
        )
    }

    private fun showRandomDialog() {
        val randomTypes = listOf("\u968f\u673a\u5168\u90e8", "\u968f\u673aSCP", "\u968f\u673a\u6545\u4e8b", "\u968f\u673a\u641e\u7b11")
        AlertDialog.Builder(requireContext())
            .setItems(randomTypes.toTypedArray()) { _, i ->
                when (i) {
                    0 -> EventUtil.onEvent(context, EventUtil.clickRandomAll)
                    1 -> EventUtil.onEvent(context, EventUtil.clickRandomScp)
                    2 -> EventUtil.onEvent(context, EventUtil.clickRandomTale)
                    3 -> EventUtil.onEvent(context, EventUtil.clickRandomJoke)
                }
                startActivity(
                    Intent(requireContext(), RandomActivity::class.java)
                        .putExtra("random_type", i)
                        .putExtra("random_title", randomTypes[i])
                )
            }
            .show()
    }

    private fun handleImportClick() {
        if (!PreferenceUtil.getShownReadSuggest()) {
            AlertDialog.Builder(requireContext())
                .setTitle("\u4e0d\u77e5\u9053\u8bfb\u4ec0\u4e48\uff1f")
                .setMessage("\u8981\u4e0d\u8981\u8bbf\u95ee\u5927\u4f6c\u4eec\u6574\u7406\u7684\u5f85\u8bfb\u5217\u8868\uff1f\u53ef\u4ee5\u590d\u5236\u5217\u8868\u540e\u70b9\u51fb\u6b64\u6309\u94ae\u5bfc\u5165\u3002")
                .setPositiveButton("\u5e26\u6211\u53bb\uff01") { _, _ ->
                    startActivity(
                        Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("http://scpsandboxcn.wikidot.com/collab:to-read-list")
                        }
                    )
                }
                .setNegativeButton("\u4e0d\u7528\u4e86", null)
                .show()
            PreferenceUtil.setShownReadSuggest()
        }
        showInputListDialog()
    }

    private fun showInputListDialog() {
        val input = EditText(requireContext()).apply {
            minLines = 1
            maxLines = 8
            setPadding(
                Utils.dp2px(16, requireContext()),
                Utils.dp2px(16, requireContext()),
                Utils.dp2px(16, requireContext()),
                Utils.dp2px(16, requireContext())
            )
        }
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            addView(input)
        }
        val inputDialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.menu_import_read_list)
            .setMessage(
                "\u5bfc\u5165\u7684\u6587\u7ae0\u6807\u9898\u53ef\u7528\u4e2d\u82f1\u6587\u9017\u53f7\u3001\u5206\u53f7\u6216\u6362\u884c\u5206\u9694\uff0c\u6709\u6570\u5b57\u5373\u53ef\u8bc6\u522b\u3002" +
                    "\u9ed8\u8ba4\u4e3a SCP \u6587\u6863\uff0c\u5176\u4ed6\u7c7b\u578b\u9700\u5728\u6807\u9898\u5185\u5305\u542b cn\u3001j \u7b49\u5173\u952e\u8bcd\u3002"
            )
            .setView(container)
            .setPositiveButton("OK") { _, _ -> }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()
        inputDialog.show()
        inputDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            splitReadList(input.text.toString())
            inputDialog.dismiss()
            EventUtil.onEvent(context, EventUtil.importReadList)
        }
    }

    private fun splitReadList(input: String) {
        val importRequests = ReadListImportParser.parse(input)
        if (importRequests.isEmpty()) {
            toast("\u8bc6\u522b\u5931\u8d25\uff0c\u8bf7\u5bf9\u7167\u8981\u6c42\u4fee\u6539\u683c\u5f0f")
            return
        }
        importRequests.forEach { request ->
            val targetScp = catalogRepository.getDocumentByNumber(
                request.scpType,
                request.numberPattern
            )
            if (targetScp != null) {
                readingListRepository.addToLater(targetScp.link, targetScp.title)
            }
        }
        toast("\u5bfc\u5165\u5b8c\u6210")
        recordListFragment.refreshList()
    }

    companion object {
        fun newInstance(): LaterFragment = LaterFragment()
    }
}
