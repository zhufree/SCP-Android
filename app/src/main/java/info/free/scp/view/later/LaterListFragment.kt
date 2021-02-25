package info.free.scp.view.later

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import info.free.scp.SCPConstants.LATEST_CREATED
import info.free.scp.databinding.FragmentLaterListBinding

import info.free.scp.view.base.BaseFragment

class LaterListFragment : BaseFragment() {

    var feedType = LATEST_CREATED

    companion object {
        fun newInstance(): LaterListFragment {
            val fragment = LaterListFragment()
            val args = Bundle()
//            args.putInt("feedType", feedType)
            fragment.arguments = args
            return fragment
        }
    }

    private val viewModel by lazy {
        ViewModelProvider(this)
                .get(LaterViewModel::class.java)
    }

    private val laterAdapter by lazy {
        LaterListAdapter()
    }

    private lateinit var binding: FragmentLaterListBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLaterListBinding.inflate(inflater, container, false)
        binding.rvLater.adapter = laterAdapter
        subscribeUi(laterAdapter)
        return binding.root
    }


    private fun subscribeUi(adapter: LaterListAdapter) {
//        feedType = arguments?.getInt("feedType") ?: LATEST_CREATED
//        Log.i("feed", "type = $feedType")
        adapter.submitList(viewModel.getLaterList())
    }

    override fun refreshTheme() {
        super.refreshTheme()
        laterAdapter.refreshTheme()
    }
}
