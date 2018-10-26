package info.free.scp.view.about


import android.app.AlertDialog
import android.app.Fragment
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import info.free.scp.BuildConfig
import info.free.scp.R
import info.free.scp.util.EventUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.util.Toaster
import info.free.scp.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_about.*


/**
 * A simple [Fragment] subclass.
 * Use the [AboutFragment.newInstance] factory method to
 * create an instance of this fragment.
 * 其他，包括写作相关，新人资讯，标签云和关于
 */
class AboutFragment : BaseFragment() {
    private var listener: AboutListener? = null

//    private var mParam1: String? = null
//    private var mParam2: String? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is AboutListener) {
            listener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        about_toolbar?.setTitle(R.string.app_name)

        tv_like_list?.setOnClickListener {
            activity.startActivity(Intent(activity, LikeActivity::class.java))
        }

        tv_about_app?.setOnClickListener {
            EventUtil.onEvent(activity, EventUtil.clickAboutApp)
            activity.startActivity(Intent(activity, AboutAppActivity::class.java))
        }
        tv_about_developer?.setOnClickListener {
            EventUtil.onEvent(activity, EventUtil.clickAboutMe)
            activity.startActivity(Intent(activity, AboutMeActivity::class.java))
        }
        tv_change_theme?.text = if (ThemeUtil.currentTheme == 1) "白天模式" else "夜间模式"

        tv_change_theme?.setOnClickListener {
            EventUtil.onEvent(activity, EventUtil.clickChangeTheme)
            ThemeUtil.changeTheme(activity, if (ThemeUtil.currentTheme == 1) 0 else 1)
            tv_change_theme?.text = if (ThemeUtil.currentTheme == 1) "白天模式" else "夜间模式"
        }
        tv_init_data?.setOnClickListener {
            EventUtil.onEvent(activity, EventUtil.clickInitData)
            listener?.onInitDataClick()
        }
        tv_reset_data?.setOnClickListener {
            AlertDialog.Builder(activity)
                    .setTitle("注意")
                    .setMessage("改选项将删除所有目录及正文数据并重新加载，是否确定？")
                    .setPositiveButton("确定") { dialog, _ ->
                        EventUtil.onEvent(activity, EventUtil.clickResetData)
                        listener?.onResetDataClick()
                        dialog.dismiss()
                    }
                    .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
                    .create().show()
        }
    }

    fun refreshTheme() {
        view?.setBackgroundColor(ThemeUtil.containerBg)
        about_toolbar?.setBackgroundColor(ThemeUtil.toolbarBg)
        tv_like_list?.setTextColor(ThemeUtil.darkText)
        tv_like_list?.setBackgroundColor(ThemeUtil.itemBg)
        tv_about_app?.setTextColor(ThemeUtil.darkText)
        tv_about_app?.setBackgroundColor(ThemeUtil.itemBg)
        tv_about_developer?.setTextColor(ThemeUtil.darkText)
        tv_about_developer?.setBackgroundColor(ThemeUtil.itemBg)
        tv_change_theme?.setTextColor(ThemeUtil.darkText)
        tv_change_theme?.setBackgroundColor(ThemeUtil.itemBg)
        tv_init_data?.setTextColor(ThemeUtil.darkText)
        tv_init_data?.setBackgroundColor(ThemeUtil.itemBg)
        tv_reset_data?.setTextColor(ThemeUtil.darkText)
        tv_reset_data?.setBackgroundColor(ThemeUtil.itemBg)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    companion object {
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//        private val LISTENER = "listener"
//        private val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
//        fun newInstance(param1: String, param2: String): HomeFragment {
        fun newInstance(): AboutFragment {
            val fragment = AboutFragment()
//            val args = Bundle()
//            args.putString(ARG_PARAM1, param1)
//            args.putString(ARG_PARAM2, param2)
//            fragment.arguments = args
            return fragment
        }
    }

    interface AboutListener {
        fun onInitDataClick()
        fun onResetDataClick()
    }

} // Required empty public constructor
