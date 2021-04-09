package info.free.scp.view.home


import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.AppMode.OFFLINE
import info.free.scp.SCPConstants.AppMode.ONLINE
import info.free.scp.db.ScpDatabase
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.category.GroupListActivity
import info.free.scp.view.feed.TopRatedActivity
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home_page.*
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.dip
import org.jetbrains.anko.support.v4.startActivity
import toast


/**
 * Use the [HomePageFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class HomePageFragment : BaseFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 下版本用banner代替
        tv_notice?.text = PreferenceUtil.getNotice()
        if (!PreferenceUtil.getShowNotice()) {
            cv_notice.visibility = GONE
        }
        iv_remove_notice?.setOnClickListener {
            PreferenceUtil.setShowNotice(false)
            cv_notice.visibility = GONE
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
        ei_scp?.setOnClickListener {
            goToGroupPage(SCPConstants.Entry.SCP_DOC)
        }
        ei_scp_cn?.setOnClickListener {
            goToGroupPage(SCPConstants.Entry.SCP_CN_DOC)
        }
        ei_scp_story?.setOnClickListener {
            goToGroupPage(SCPConstants.Entry.STORY_DOC)
        }
        ei_scp_wander?.setOnClickListener {
            goToGroupPage(SCPConstants.Entry.WANDER_DOC)
        }

        btn_more_recent?.background = ThemeUtil.customShape(0, ThemeUtil.linkBlue, dip(1), dip(6))
        btn_more_recent_translate?.background = ThemeUtil.customShape(0, ThemeUtil.linkBlue, dip(1), dip(6))

        btn_top_page_entry?.setOnClickListener {
            startActivity<TopRatedActivity>()
        }
    }

    private fun goToGroupPage(entry_type: Int) {
        // TODO 埋点
        startActivity<GroupListActivity>("entry_type" to entry_type)
    }

    override fun refreshTheme() {
        super.refreshTheme()
//        cd_notice_container?.setBackgroundColor(ThemeUtil.containerBg)
//        tv_home_notice?.setTextColor(ThemeUtil.darkText)
        ei_scp?.refreshTheme()
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
