package info.free.scp.view.feed


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import info.free.scp.SCPConstants.TOP_RATED_ALL
import info.free.scp.SCPConstants.TOP_RATED_GOI
import info.free.scp.SCPConstants.TOP_RATED_SCP
import info.free.scp.SCPConstants.TOP_RATED_TALES
import info.free.scp.SCPConstants.TOP_RATED_WANDERS
import info.free.scp.databinding.FragmentTopRatedBinding
import info.free.scp.util.ThemeUtil
import info.free.scp.view.base.BaseFragment
import org.jetbrains.anko.support.v4.startActivity

/**
 * A simple [Fragment] subclass.
 * Use the [TopRatedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TopRatedFragment : BaseFragment() {
    private var _binding: FragmentTopRatedBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTopRatedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cvTopRatedAll.setOnClickListener {
            startActivity<TopRatedListActivity>("feedType" to TOP_RATED_ALL)
        }
        binding.cvTopRatedScp.setOnClickListener {
            startActivity<TopRatedListActivity>("feedType" to TOP_RATED_SCP)
        }
        binding.cvTopRatedTales.setOnClickListener {
            startActivity<TopRatedListActivity>("feedType" to TOP_RATED_TALES)
        }
        binding.cvTopRatedGoi.setOnClickListener {
            startActivity<TopRatedListActivity>("feedType" to TOP_RATED_GOI)
        }
        binding.cvTopRatedWanderer.setOnClickListener {
            startActivity<TopRatedListActivity>("feedType" to TOP_RATED_WANDERS)
        }
    }

    override fun refreshTheme() {
        super.refreshTheme()
        binding.cvTopRatedGoi.setBackgroundColor(ThemeUtil.containerBg)
        binding.cvTopRatedTales.setBackgroundColor(ThemeUtil.containerBg)
        binding.cvTopRatedScp.setBackgroundColor(ThemeUtil.containerBg)
        binding.cvTopRatedAll.setBackgroundColor(ThemeUtil.containerBg)
        binding.cvTopRatedWanderer.setBackgroundColor(ThemeUtil.containerBg)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            TopRatedFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
