package info.free.scp.view.home


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.config.IndicatorConfig.Direction.RIGHT
import com.youth.banner.holder.BannerImageHolder
import com.youth.banner.indicator.CircleIndicator
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.AppMode.OFFLINE
import info.free.scp.SCPConstants.AppMode.ONLINE
import info.free.scp.db.ScpDatabase
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.category.SeriesDocActivity
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home_page.*
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.dip
import org.jetbrains.anko.support.v4.startActivity


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
        // TODO notice
//        tv_home_notice?.text = PreferenceUtil.getNotice()
//        if (!PreferenceUtil.getShowNotice()) {
//            cd_notice_container.visibility = GONE
//        }
//        iv_remove_notice?.setOnClickListener {
//            PreferenceUtil.setShowNotice(false)
//            cd_notice_container.visibility = GONE
//            toast("公告已隐藏，可在app使用说明中查看")
//        }
        ei_scp?.setOnClickListener {
            goToDocPage(SCPConstants.Entry.SCP_DOC)
        }
        val bannerImgUrlList = listOf("https://wx1.sinaimg.cn/mw690/006S0efgly1gmmcsy0evlj31rc0u0gn4.jpg",
                "https://wx1.sinaimg.cn/mw690/006S0efgly1gmmcsy0evlj31rc0u0gn4.jpg")
        banner_home_page.addBannerLifecycleObserver(this)
                .setAdapter(object : BannerImageAdapter<String>(bannerImgUrlList) {
                    override fun onBindView(holder: BannerImageHolder, url: String?, position: Int, size: Int) {
                        Glide.with(holder.itemView)
                                .load(url)
                                .apply(RequestOptions.bitmapTransform(RoundedCorners(6)))
                                .into(holder.imageView)
                    }
                })
                .addBannerLifecycleObserver(this)//添加生命周期观察者
                .setIndicator(CircleIndicator(this.context))
                .setIndicatorGravity(RIGHT)
                .setIndicatorWidth(dip(8), dip(8))
                .setIndicatorSelectedColor(ThemeUtil.toolbarBg)
    }

    private fun goToDocPage(entry_type: Int) {
        if (!PreferenceUtil.getShownModeNotice()) {
            alert("为了方便用户尽快开始阅读，0.1.5版本添加了全局在线阅读模式，如果需要下载数据库并使用离线模式，" +
                    "请在【我的-数据下载及配置】中设置.\n注意：1.之前已离线的用户仍将使用离线模式，新用户默认使用在线模式" +
                    "\n2.在线阅读模式的数据仍和数据库相同，并非和官网完全同步，具体同步日期可在离线页面查看。\n" +
                    "3.正文页面菜单中的在线模式是强制访问网页，和全局阅读模式不同") {
                positiveButton("我知道了") {
                    ScpDatabase.getInstance()?.let {
                        PreferenceUtil.setAppMode(OFFLINE)
                    } ?: run {
                        PreferenceUtil.setAppMode(ONLINE)
                    }
                    startActivity<SeriesDocActivity>("entry_type" to entry_type)
                }
            }.show()
            PreferenceUtil.setShownModeNotice()
        } else {
            startActivity<SeriesDocActivity>("entry_type" to entry_type)
        }
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
