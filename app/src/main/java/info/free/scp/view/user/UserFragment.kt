package info.free.scp.view.user


import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Environment.DIRECTORY_PICTURES
import android.os.storage.StorageManager
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.documentfile.provider.DocumentFile
import info.free.scp.R
import info.free.scp.SCPConstants.RequestCode.REQUEST_PICTURE_DIR
import info.free.scp.db.AppInfoDatabase
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.util.Utils
import info.free.scp.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_user.*
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.selector
import java.io.FileInputStream
import java.util.*


/**
 * Use the [UserFragment.newInstance] factory method to
 * create an instance of this fragment.
 * 其他，包括写作相关，新人资讯，标签云和关于
 */
class UserFragment : BaseFragment() {

//    private var mParam1: String? = null
//    private var mParam2: String? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user, container, false)
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
        childFragmentManager.beginTransaction().replace(R.id.fl_settings, SettingsFragment()).commit()
        tv_nickname?.text = if (PreferenceUtil.getNickname().isNotEmpty())
            "编号：${Random(System.currentTimeMillis()).nextInt(600)}\n" +
                    "职务：${getRank(PreferenceUtil.getPoint())}\n代号：${PreferenceUtil.getNickname()}"
        else "编号：${Random(System.currentTimeMillis()).nextInt(600)}\n" +
                "职务：点击设置\n代号：点击设置"
        tv_data_desc?.text = "已研究项目：${AppInfoDatabase.getInstance().likeAndReadDao().getReadCount()}\n" +
                "已跟踪项目：${AppInfoDatabase.getInstance().likeAndReadDao().getLikeCount()}"

        iv_user_head.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            /* 开启Pictures画面Type设定为image */
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            /* 使用Intent.ACTION_GET_CONTENT这个Action */
            /* 取得相片后返回本画面 */
            startActivityForResult(intent, 1)
        }
        tv_nickname.setOnClickListener { checkUserInfo() }
        getHeadImg()
    }

    private fun getHeadImg() {
        if (Build.VERSION.SDK_INT > 23) {
            val sm = context?.getSystemService(StorageManager::class.java)
            val volume = sm?.primaryStorageVolume
            volume?.createAccessIntent(DIRECTORY_PICTURES)?.also {
                startActivityForResult(it, REQUEST_PICTURE_DIR)
            }
        } else {
            iv_user_head?.setImageBitmap(BitmapFactory.decodeFile(Utils.getAlbumStorageDir("SCP").path
                    + "/scp_user_head.jpg"))

        }
    }

    private fun checkUserInfo() {
        var input: EditText? = null
        alert {
            customView {
                linearLayout {
                    padding = dip(16)
                    orientation = LinearLayout.VERTICAL
                    textView("欢迎来到SCP基金会，调查员，请输入你的名字（重启app后生效显示）") {
                        textColor = ThemeUtil.darkText
                        textSize = 18f
                    }
                    input = editText {
                        height = ViewGroup.LayoutParams.WRAP_CONTENT
                        width = ViewGroup.LayoutParams.MATCH_PARENT
                        singleLine = true
                    }
                }
            }
            positiveButton("确定") {
                PreferenceUtil.saveNickname(input?.text.toString())
                tv_nickname?.text = "编号：${Random(System.currentTimeMillis()).nextInt(600)}\n" +
                        "职务：${getRank(PreferenceUtil.getPoint())}\n代号：${PreferenceUtil.getNickname()}"
                checkJob()
            }
            negativeButton("取消") {}

        }.show()
    }

    private fun checkJob() {
        val jobList = listOf("收容专家", "研究员", "安全人员", "战术反应人员", "外勤特工", "机动特遣队作业员")
        selector("欢迎来到SCP基金会，${PreferenceUtil.getNickname()}，请选择你的职业",
                jobList) { out, i ->
            val field = out.javaClass.superclass?.getDeclaredField(
                    "mShowing")
            field?.isAccessible = true
            //   将mShowing变量设为false，表示对话框已关闭
            field?.set(out, false)
            alert(getString(PreferenceUtil.getDescForJob(jobList[i])), jobList[i]) {
                positiveButton("确定选择") {
                    field?.set(out, true)
                    out.dismiss()
                    PreferenceUtil.setJob(jobList[i])
                    tv_nickname?.text = "编号：${Random(System.currentTimeMillis()).nextInt(600)}\n" +
                            "职务：${getRank(PreferenceUtil.getPoint())}\n代号：${PreferenceUtil.getNickname()}"
                }
                negativeButton("我手滑了") { }
            }.show()
        }
    }

    override fun refreshTheme() {
        super.refreshTheme()
        tv_nickname?.setTextColor(ThemeUtil.darkText)
        tv_data_desc?.setTextColor(ThemeUtil.lightText)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        data?.let {
            val uri = data.data
            uri?.let {
                if (requestCode == REQUEST_PICTURE_DIR) {
                    context?.contentResolver?.takePersistableUriPermission(uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    val df = DocumentFile.fromTreeUri(context!!, uri)
                    val scpDir = df?.findFile("SCP")
                    val picPath = scpDir?.findFile("scp_user_head.jpg")
                    picPath?.let {
                        val pfd = context?.contentResolver?.openFileDescriptor(picPath.uri, "r")
                        pfd?.let {
                            val fileInputStream = FileInputStream(pfd.fileDescriptor)
                            iv_user_head?.setImageBitmap(BitmapFactory.decodeStream(fileInputStream))
                            fileInputStream.close()
                        }
                    }

                } else {
                    try {
                        val file = Utils.getFileByUri(uri, context!!)
                        file?.let { f ->
                            Utils.save(f, "scp_user_head")
                            iv_user_head?.setImageBitmap(BitmapFactory.decodeFile(f.path))
                        }
                    } catch (e: Exception) {
                        Log.e("Exception", e.message, e)
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
        Log.i("user", "requestCode = $requestCode")
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

} // Required empty public constructor
