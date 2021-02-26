package info.free.scp.view.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import info.free.scp.SCPConstants.SearchType.CONTENT
import info.free.scp.SCPConstants.SearchType.TITLE
import info.free.scp.databinding.FragmentSearchTabBinding
import info.free.scp.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_search_tab.*
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.support.v4.toast

/**
 * 待读和历史列表共用一个fragment
 * recordType区分
 */
class SearchResultFragment : BaseFragment() {

    var searchType = TITLE
    var keyword = ""

    companion object {
        fun newInstance(searchType: Int, keyword: String): SearchResultFragment {
            val fragment = SearchResultFragment()
            val args = Bundle()
            args.putInt("search_type", searchType)
            args.putString("keyword", keyword)
            fragment.arguments = args
            return fragment
        }
    }

    private val viewModel by lazy {
        ViewModelProvider(this)
                .get(SearchViewModel::class.java)
    }

    private val searchAdapter by lazy {
        SearchResultAdapter()
    }

    private lateinit var binding: FragmentSearchTabBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchTabBinding.inflate(inflater, container, false)
        binding.rvSearchResult.adapter = searchAdapter
        searchType = arguments?.getInt("search_type") ?: TITLE
        keyword = arguments?.getString("keyword") ?: ""
        subscribeUi(searchAdapter)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        if (searchType == CONTENT) {
            pb_loading.visibility = VISIBLE
            viewModel.searchScp(searchType, "%$keyword%")
        }
    }

    private fun subscribeUi(adapter: SearchResultAdapter) {
        if (searchType == TITLE) {
            viewModel.searchScp(searchType, "%$keyword%")
            adapter.submitList(viewModel.titleResult)
        } else {
            viewModel.contentResult.observe(viewLifecycleOwner, Observer {
                pb_loading.visibility = GONE
                adapter.submitList(it)
            })
        }
    }

    override fun refreshTheme() {
        super.refreshTheme()
        searchAdapter.refreshTheme()
    }
}
