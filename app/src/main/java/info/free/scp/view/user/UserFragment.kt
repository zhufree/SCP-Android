package info.free.scp.view.user


import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import info.free.scp.R
import info.free.scp.db.ScpDao
import info.free.scp.util.*
import info.free.scp.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_about.*
import java.util.*
//import kotlin.random.Random


/**
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
            point < 200 -> {
                PreferenceUtil.getJobNameByLevel(0)
            }
            point < 700 -> {
                PreferenceUtil.getJobNameByLevel(1)
            }
            point < 1500 -> {
                PreferenceUtil.getJobNameByLevel(2)
            }
            point < 2500 -> {
                "C级" + PreferenceUtil.getJobNameByLevel(3)
            }
            point < 4000 -> {
                "B级" + PreferenceUtil.getJobNameByLevel(4)
            }
            point < 8000 -> {
                "A级" + PreferenceUtil.getJobNameByLevel(5)
            }
            point > 8000 -> {
                "A级" + PreferenceUtil.getJobNameByLevel(5)
            }
            else -> {
                PreferenceUtil.getJobNameByLevel(0)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        about_toolbar?.setTitle(R.string.app_name)
        childFragmentManager.beginTransaction().replace(R.id.fl_settings, SettingsFragment()).commit()
        tv_nickname?.text = "编号：${Random(System.currentTimeMillis()).nextInt(600)}\n" +
                "职务：${getRank(PreferenceUtil.getPoint())}\n代号：${PreferenceUtil.getNickname()}"
        tv_data_desc?.text = "已研究项目：${ScpDao.getInstance().getReadCount()}\n" +
                "已跟踪项目：${ScpDao.getInstance().getLikeCount()}"

        iv_user_head?.setImageBitmap(BitmapFactory.decodeFile(Utils.getAlbumStorageDir("SCP").path
                + "/scp_user_head.jpg"))
        iv_user_head.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            /* 开启Pictures画面Type设定为image */
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*")
            /* 使用Intent.ACTION_GET_CONTENT这个Action */
            /* 取得相片后返回本画面 */
            startActivityForResult(intent, 1)
        }
    }

    fun refreshTheme() {
        view?.setBackgroundColor(ThemeUtil.containerBg)
        about_toolbar?.setBackgroundColor(ThemeUtil.toolbarBg)
        tv_nickname?.setTextColor(ThemeUtil.darkText)
        tv_data_desc?.setTextColor(ThemeUtil.lightText)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null) {
            val uri = data.data

            uri?.let {
                try {
                    val file = Utils.getFileByUri(uri, context!!)
                    file?.let{f ->
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
