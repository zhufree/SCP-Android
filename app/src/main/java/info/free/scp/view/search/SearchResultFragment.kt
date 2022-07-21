package info.free.scp.view.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.SearchType.CONTENT
import info.free.scp.SCPConstants.SearchType.TITLE
import info.free.scp.databinding.FragmentSearchTabBinding
import info.free.scp.util.FileUtil
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.user.DownloadActivity
import kotlinx.android.synthetic.main.fragment_search_tab.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.startActivity

/**
 * 待读和历史列表共用一个fragment
 * recordType区分
 */
class SearchResultFragment : BaseFragment() {

    var searchType = TITLE
    var keyword = ""
    var searched = false

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

    override fun onResume() {
        super.onResume()
        if (searchType == CONTENT && !searched) {
            if (FileUtil.checkDataReady(SCPConstants.DETAIL_DB_NAME)) {
                pb_loading.visibility = VISIBLE
                doAsync {
                    viewModel.searchScp(searchType, "%$keyword%")
                    searched = true
                }
            } else {
                tv_search_notice.visibility = VISIBLE
                btn_go_download.visibility = VISIBLE
                btn_go_download.setOnClickListener {
                    startActivity<DownloadActivity>()
                }
            }
        }
    }

    private fun subscribeUi(adapter: SearchResultAdapter) {
        if (searchType == TITLE) {
            viewModel.searchScp(searchType, "%$keyword%")
            doAsync {
                adapter.submitList(viewModel.titleResult)
            }
        } else {
            viewModel.contentResult.observe(viewLifecycleOwner, Observer {
                pb_loading.visibility = GONE
                doAsync {
                    adapter.submitList(it)
                }
            })
        }
    }
}
