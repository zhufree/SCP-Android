package info.free.scp.view.later

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import info.free.scp.SCPConstants.LATEST_CREATED
import info.free.scp.databinding.FragmentLikeBoxListBinding

import info.free.scp.databinding.SubFeedFragmentBinding
import info.free.scp.view.base.BaseFragment

class LikeBoxListFragment : BaseFragment() {

    companion object {
        fun newInstance(): LikeBoxListFragment {
            val fragment = LikeBoxListFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    private val viewModel by lazy {
        ViewModelProvider(this)
                .get(LaterViewModel::class.java)
    }

    private val boxAdapter by lazy {
        LikeBoxAdapter()
    }

    private lateinit var binding: FragmentLikeBoxListBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentLikeBoxListBinding.inflate(inflater, container, false)
        binding.rvLikeBox.adapter = boxAdapter
        subscribeUi(boxAdapter)
        return binding.root
    }


    private fun subscribeUi(adapter: LikeBoxAdapter) {
        adapter.submitList(viewModel.getLikeBoxList())
    }

    override fun refreshTheme() {
        super.refreshTheme()
//        boxAdapter.refreshTheme() TODO
    }
}
