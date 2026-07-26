package info.free.scp.view.category

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import info.free.scp.SCPConstants.Entry.ART_DOC
import info.free.scp.SCPConstants.Entry.GOI_DOC
import info.free.scp.SCPConstants.Entry.INTERNATIONAL_DOC
import info.free.scp.SCPConstants.ScpType.SAVE_ART
import info.free.scp.SCPConstants.ScpType.SAVE_GOI
import info.free.scp.SCPConstants.ScpType.SAVE_INTERNATIONAL
import info.free.scp.ui.MainTheme
import info.free.scp.ui.category.CategoryEntryScreen
import info.free.scp.data.settings.InteractionPointsStore
import info.free.scp.data.settings.PreferenceSettingsStore
import info.free.scp.view.base.BaseFragment

class CategoryFragment : BaseFragment() {
    private val interactionPoints: InteractionPointsStore = PreferenceSettingsStore()
    private var entryType = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): ComposeView {
        entryType = arguments?.getInt("entry_type") ?: -1
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MainTheme {
                    CategoryEntryScreen(
                        categories = getCategoryTitles(),
                        onOpenCategory = { title, index -> openCategory(title, index) }
                    )
                }
            }
        }
    }

    private fun getSaveType(): Int {
        return when (entryType) {
            INTERNATIONAL_DOC -> SAVE_INTERNATIONAL
            GOI_DOC -> SAVE_GOI
            ART_DOC -> SAVE_ART
            else -> SAVE_INTERNATIONAL
        }
    }

    private fun getCategoryTitles(): List<String> {
        return when (entryType) {
            INTERNATIONAL_DOC -> listOf(
                "\u4fc4\u7f57\u65af\u5206\u90e8",
                "\u97e9\u56fd\u5206\u90e8",
                "\u6cd5\u56fd\u5206\u90e8",
                "\u6ce2\u5170\u5206\u90e8",
                "\u897f\u73ed\u7259\u5206\u90e8",
                "\u6cf0\u56fd\u5206\u90e8",
                "\u65e5\u672c\u5206\u90e8",
                "\u5fb7\u56fd\u5206\u90e8",
                "\u610f\u5927\u5229\u5206\u90e8",
                "\u4e4c\u514b\u5170\u5206\u90e8",
                "\u8461\u8404\u7259\u8bed\u5206\u90e8",
                "\u6377\u514b\u5206\u90e8",
                "\u975e\u5b98\u65b9\u5206\u90e8"
            )
            GOI_DOC -> listOf("1-100", "100-200", "200-300", "300-400", "400+")
            ART_DOC -> listOf("1-100", "100-200", "200-300", "300+")
            else -> listOf("")
        }
    }

    private fun openCategory(title: String, position: Int) {
        interactionPoints.increasePoints(2)
        startActivity(
            Intent(requireContext(), DocListActivity::class.java)
                .putExtra("saveType", getSaveType())
                .putExtra("groupIndex", position)
                .putExtra("extraType", title)
        )
    }

    companion object {
        fun newInstance(entryType: Int, subPosition: Int): CategoryFragment {
            val fragment = CategoryFragment()
            val args = Bundle()
            args.putInt("entry_type", entryType)
            args.putInt("sub_position", subPosition)
            fragment.arguments = args
            return fragment
        }
    }
}
