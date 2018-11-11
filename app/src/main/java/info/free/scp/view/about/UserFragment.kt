package info.free.scp.view.about


import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import info.free.scp.R
import info.free.scp.ScpApplication
import info.free.scp.db.ScpDao
import info.free.scp.util.*
import info.free.scp.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_about.*
import kotlin.random.Random


/**
 * A simple [Fragment] subclass.
 * Use the [UserFragment.newInstance] factory method to
 * create an instance of this fragment.
 * 其他，包括写作相关，新人资讯，标签云和关于
 */
class UserFragment : BaseFragment() {
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

    private fun getRank(point: Int): String {
        Log.i("point", "point = $point")
        return when {
            point < 100 -> {
                PreferenceUtil.getJobNameByLevel(0)
            }
            point < 500 -> {
                PreferenceUtil.getJobNameByLevel(1)
            }
            point < 1200 -> {
                PreferenceUtil.getJobNameByLevel(2)
            }
            point < 2000 -> {
                "C级" + PreferenceUtil.getJobNameByLevel(3)
            }
            point < 3000 -> {
                "B级" + PreferenceUtil.getJobNameByLevel(4)
            }
            point < 6000 -> {
                "A级" + PreferenceUtil.getJobNameByLevel(5)
            }
            else -> {
                PreferenceUtil.getJobNameByLevel(0)
            }
        } + Random(System.currentTimeMillis()).nextInt(600)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        about_toolbar?.setTitle(R.string.app_name)
        tv_nickname?.text = "${getRank(PreferenceUtil.getPoint())}：${PreferenceUtil.getNickname()}"
        tv_data_desc?.text = "已阅读${ScpDao.getInstance().getReadCount()}篇文档    " +
                "已收藏${ScpDao.getInstance().getLikeCount()}篇文档"

        iv_user_head?.setImageBitmap(BitmapFactory.decodeFile(Utils.getAlbumStorageDir("SCP").path
                + "/scp_user_head.jpg"))
        iv_user_head.setOnClickListener {
            val intent = Intent()
            /* 开启Pictures画面Type设定为image */
            intent.type = "image/*"
            /* 使用Intent.ACTION_GET_CONTENT这个Action */
            intent.action = Intent.ACTION_GET_CONTENT
            /* 取得相片后返回本画面 */
            startActivityForResult(intent, 1)
        }
        tv_like_list?.setOnClickListener {
            EventUtil.onEvent(activity, EventUtil.clickLikeList)
            activity?.startActivity(Intent(activity, LikeActivity::class.java))
        }

        tv_about_app?.setOnClickListener {
            EventUtil.onEvent(activity, EventUtil.clickAboutApp)
            activity?.startActivity(Intent(activity, AboutAppActivity::class.java))
        }
        tv_about_developer?.setOnClickListener {
            EventUtil.onEvent(activity, EventUtil.clickAboutMe)
            activity?.startActivity(Intent(activity, AboutMeActivity::class.java))
        }
        tv_change_theme?.text = if (ThemeUtil.currentTheme == 1) "日间模式" else "夜间模式"

        tv_change_theme?.setOnClickListener {
            EventUtil.onEvent(activity, EventUtil.clickChangeTheme)
            ThemeUtil.changeTheme(activity, if (ThemeUtil.currentTheme == 1) 0 else 1)
            tv_change_theme?.text = if (ThemeUtil.currentTheme == 1) "日间模式" else "夜间模式"
        }
        tv_init_data?.setOnClickListener {
            EventUtil.onEvent(activity, EventUtil.clickInitData)
            listener?.onInitDataClick()
        }
        tv_reset_data?.setOnClickListener {
            AlertDialog.Builder(activity)
                    .setTitle("注意")
                    .setMessage("该选项将删除所有目录及正文数据并重新加载，是否确定？")
                    .setPositiveButton("确定") { dialog, _ ->
                        EventUtil.onEvent(activity, EventUtil.clickResetData)
                        listener?.onResetDataClick()
                        dialog.dismiss()
                    }
                    .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
                    .create().show()
        }
        tv_query_enter?.setOnClickListener {
            if (PreferenceUtil.getQueryLink().isNotEmpty()) {
                EventUtil.onEvent(context, EventUtil.clickQuery)
                val queryIntent = Intent()
                queryIntent.action = "android.intent.action.VIEW"
                val updateUrl = Uri.parse(PreferenceUtil.getQueryLink())
                queryIntent.data = updateUrl
                startActivity(queryIntent)
            }
        }
    }

    fun refreshTheme() {
        view?.setBackgroundColor(ThemeUtil.containerBg)
        about_toolbar?.setBackgroundColor(ThemeUtil.toolbarBg)
        tv_nickname?.setTextColor(ThemeUtil.darkText)
        tv_data_desc?.setTextColor(ThemeUtil.lightText)
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
        tv_query_enter?.setTextColor(ThemeUtil.darkText)
        tv_query_enter?.setBackgroundColor(ThemeUtil.itemBg)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && data != null) {
            val uri = data.data

            uri?.let {
                try {
                    val file = Utils.getFileByUri(uri, context!!)
                    file?.let{f ->
                        EventUtil.onEvent(context, EventUtil.changeHeadImg, f.path)
                        Utils.save(f, "scp_user_head")
                        iv_user_head?.setImageBitmap(BitmapFactory.decodeFile(f.path))
                    }
                } catch (e: Exception) {
                    Log.e("Exception", e.message, e)
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data)
        Log.i("user", "requestCode = $requestCode")
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
        fun newInstance(): UserFragment {
            val fragment = UserFragment()
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
