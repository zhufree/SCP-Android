package info.free.scp.view.home


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.databinding.FragmentHomePageBinding
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.category.GroupListActivity
import info.free.scp.view.feed.FeedActivity
import info.free.scp.view.feed.FeedListViewModel
import info.free.scp.view.feed.TopRatedActivity
import info.free.scp.view.tag.TagCloudActivity
import info.free.scp.view.widget.FeedArticleListItem
import org.jetbrains.anko.support.v4.dip
import org.jetbrains.anko.support.v4.startActivity
import toast


/**
 * Use the [HomePageFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class HomePageFragment : BaseFragment() {
    private val feedVm by lazy {
        ViewModelProvider(this)[FeedListViewModel::class.java]
    }
    private val feedItemList = mutableListOf<FeedArticleListItem>()
    private var _binding: FragmentHomePageBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomePageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 下版本用banner代替
        binding.tvNotice.text = PreferenceUtil.getNotice()
        if (!PreferenceUtil.getShowNotice()) {
            binding.cvNotice.visibility = GONE
        }
        binding.ivRemoveNotice.setOnClickListener {
            PreferenceUtil.setShowNotice(false)
            binding.cvNotice.visibility = GONE
            toast("公告已隐藏，可在app使用说明中查看")
        }

        // TODO banner
        val bannerImgUrlList = listOf("https://wx1.sinaimg.cn/mw690/006S0efgly1gmmcsy0evlj31rc0u0gn4.jpg",
                "https://wx1.sinaimg.cn/mw690/006S0efgly1gmmcsy0evlj31rc0u0gn4.jpg")
//        banner_home_page.addBannerLifecycleObserver(this)
//                .setAdapter(object : BannerImageAdapter<String>(bannerImgUrlList) {
//                    override fun onBindView(holder: BannerImageHolder, url: String?, position: Int, size: Int) {
//                        Glide.with(holder.itemView)
//                                .load(url)
//                                .apply(RequestOptions.bitmapTransform(RoundedCorners(6)))
//                                .into(holder.imageView)
//                    }
//                })
//                .addBannerLifecycleObserver(this)//添加生命周期观察者
//                .setIndicator(CircleIndicator(this.context))
//                .setIndicatorGravity(RIGHT)
//                .setIndicatorWidth(dip(8), dip(8))
//                .setIndicatorSelectedColor(ThemeUtil.toolbarBg)
        binding.eiScp.setOnClickListener {
            goToGroupPage(SCPConstants.Entry.SCP_DOC)
        }
        binding.eiScpCn.setOnClickListener {
            goToGroupPage(SCPConstants.Entry.SCP_CN_DOC)
        }
        binding.eiScpStory.setOnClickListener {
            goToGroupPage(SCPConstants.Entry.STORY_DOC)
        }
        binding.eiScpWander.setOnClickListener {
            goToGroupPage(SCPConstants.Entry.WANDER_DOC)
        }

        binding.btnMoreRecent.background =
            ThemeUtil.customShape(0, ThemeUtil.linkBlue, dip(1), dip(6))
        binding.btnMoreRecentTranslate.background =
            ThemeUtil.customShape(0, ThemeUtil.linkBlue, dip(1), dip(6))

        arrayOf(binding.btnMoreRecent, binding.btnMoreRecentTranslate).forEach {
            it.setOnClickListener {
                startActivity<FeedActivity>()
            }
        }
        binding.btnTopPageEntry.setOnClickListener {
            startActivity<TopRatedActivity>()
        }
        binding.btnAllTags.setOnClickListener {
            startActivity<TagCloudActivity>()
        }

        feedVm.getFeedIndex().observe(this.viewLifecycleOwner, Observer { fi ->
            fi.latestCreate.forEach {
                val newFeedItem = FeedArticleListItem(context!!, it)
                binding.llLatestCn.addView(newFeedItem)
                feedItemList.add(newFeedItem)
            }
            fi.latestTranslate.forEach {
                val newFeedItem = FeedArticleListItem(context!!, it)
                binding.llLatestTranslate.addView(newFeedItem)
                feedItemList.add(newFeedItem)
            }
        })
        feedVm.loadFeedIndex()
    }

    private fun goToGroupPage(entry_type: Int) {
        startActivity<GroupListActivity>("entry_type" to entry_type)
    }

    override fun refreshTheme() {
        super.refreshTheme()
        binding.clHomePageContainer.setBackgroundColor(ThemeUtil.containerBg)
        arrayOf(binding.glEntry, binding.clRecentList, binding.clRecentTranslateList).forEach {
            context?.let { c ->
                it.background = ThemeUtil.getDrawable(c, R.drawable.bg_entry_box)
            }
        }
        arrayOf(binding.tvRecentHeader, binding.tvRecentTranslateHeader).forEach {
            it.setTextColor(ThemeUtil.darkText)
        }
        arrayOf(
            binding.eiScp,
            binding.eiScpCn,
            binding.eiScpStory,
            binding.eiScpWander
        ).forEach { it.refreshTheme() }
        feedItemList.forEach { it.refreshTheme() }
        binding.cvNotice.setBackgroundColor(ThemeUtil.itemBg)
        binding.tvNotice.setTextColor(ThemeUtil.darkText)
        binding.eiScp.refreshTheme()
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        fun newInstance(): HomePageFragment {
            val fragment = HomePageFragment()
            return fragment
        }
    }
}
