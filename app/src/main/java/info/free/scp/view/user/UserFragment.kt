package info.free.scp.view.user


import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.HISTORY_TYPE
import info.free.scp.SCPConstants.RequestCode.REQUEST_PICTURE_DIR
import info.free.scp.ScpApplication
import info.free.scp.db.AppInfoDatabase
import info.free.scp.util.*
import info.free.scp.util.ThemeUtil.DAY_THEME
import info.free.scp.util.ThemeUtil.NIGHT_THEME
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.detail.DetailActivity
import info.free.scp.view.draft.DraftListActivity
import info.free.scp.view.eatroom.MealListActivity
import info.free.scp.view.game.GameListActivity
import info.free.scp.view.later.LaterViewModel
import info.free.scp.view.portal.PortalActivity
import info.free.scp.view.widget.HistoryListItem
import kotlinx.android.synthetic.main.fragment_user.*
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.dip
import org.jetbrains.anko.support.v4.selector
import org.jetbrains.anko.support.v4.startActivity
import java.util.*


/**
 * Use the [UserFragment.newInstance] factory method to
 * create an instance of this fragment.
 * 其他，包括写作相关，新人资讯，标签云和关于
 */
class UserFragment : BaseFragment() {

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

    private val viewModel by lazy {
        ViewModelProvider(this)
                .get(LaterViewModel::class.java)
    }

    private val historyItemList = mutableListOf<HistoryListItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        childFragmentManager.beginTransaction().replace(R.id.fl_settings, SettingsFragment()).commit()
        tv_nickname?.text = "代号" // TODO
        tv_job?.text = if (PreferenceUtil.getNickname().isNotEmpty())
            "编号：${Random(System.currentTimeMillis()).nextInt(600)}\n" +
                    "职务：${getRank(PreferenceUtil.getPoint())}   代号：${PreferenceUtil.getNickname()}"
        else "编号：${Random(System.currentTimeMillis()).nextInt(600)}\n" +
                "职务：点击设置   代号：点击设置"
        tv_data_desc?.text = "已研究项目：${AppInfoDatabase.getInstance().likeAndReadDao().getReadCount()}\t" +
                "已跟踪项目：${AppInfoDatabase.getInstance().likeAndReadDao().getLikeCount()}"


        tv_nickname.setOnClickListener { checkUserInfo() }
        if (PreferenceUtil.getShowMeal()) {
            st_meal.visibility = VISIBLE
        }
        if (PreferenceUtil.getNewMealCount() > PreferenceUtil.getOldMealCount()) {
//            st_meal.setRight("NEW") TODO
        }

        val historyList = viewModel.getRecordList(HISTORY_TYPE, SCPConstants.OrderType.DESC)
        historyList.forEachIndexed { index, scp ->
            if (index < 5) {
                val newItem = HistoryListItem(this.context!!, scp.title, scp.showTime())
                newItem.onItemClick = {
                    startActivity<DetailActivity>("link" to scp.link, "title" to scp.title)
                }
                ll_history_container.addView(newItem)
                historyItemList.add(newItem)
            }
        }
        setSettingEvent()

        Glide.with(this).load(R.drawable.author_head)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(dip(25))))
                .into(iv_author_head)

        setHeadImg()

        iv_user_head.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            startActivityForResult(intent, REQUEST_PICTURE_DIR)
        }

    }

    private fun setSettingEvent() {
        ib_theme.setOnClickListener {
            ThemeUtil.changeTheme(activity, if (ThemeUtil.currentTheme == 1) DAY_THEME else NIGHT_THEME)
        }

        st_draft.onClick = {
            startActivity<DraftListActivity>()
        }
        st_game.onClick = { startActivity<GameListActivity>() }
        st_meal.onClick = { startActivity<MealListActivity>() }
        st_portal.onClick = { startActivity<PortalActivity>() }

        iv_more_history.setOnClickListener { startActivity<LaterAndHistoryActivity>() }

        st_read.onClick = {
            EventUtil.onEvent(activity, EventUtil.clickReadSetting)
            startActivity<SettingsActivity>()
        }
        st_data.onClick = {
            startActivity<DownloadActivity>()
        }
        st_use.onClick = {
            startActivity<AboutAppActivity>()
        }
        btn_donation.setOnClickListener {
            if (ScpApplication.channelName == "GooglePlay") {
                startActivity<DonationActivity>()
            } else {
                startActivity<DonationQrActivity>()
            }
        }
        st_query.onClick = {
            val updateIntent = Intent()
            updateIntent.action = "android.intent.action.VIEW"
            val updateUrl = Uri.parse("http://freeescp.mikecrm.com/zelnB9R")
            updateIntent.data = updateUrl
            startActivity(updateIntent)
        }
    }


    private fun setHeadImg() {
        val imgFile = privatePictureDir(this.context!!, "user_head.jpg")
        if (imgFile.exists()) {
            iv_user_head?.setImageBitmap(BitmapFactory.decodeFile(imgFile.path))
        }
//        if (Build.VERSION.SDK_INT > 23) {
//            val sm = context?.getSystemService(StorageManager::class.java)
//            val volume = sm?.primaryStorageVolume
//            volume?.createAccessIntent(DIRECTORY_PICTURES)?.also {
//                startActivityForResult(it, REQUEST_PICTURE_DIR)
//            }
//        } else {
//            iv_user_head?.setImageBitmap(BitmapFactory.decodeFile(Utils.getAlbumStorageDir("SCP").path
//                    + "/scp_user_head.jpg"))
//        }
    }

    private fun checkUserInfo() {
        var input: EditText? = null
        alert {
            customView {
                linearLayout {
                    padding = dip(16)
                    orientation = LinearLayout.VERTICAL
                    textView("欢迎来到SCP基金会，调查员，请输入你的名字") {
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
        arrayOf(tv_history_list_head, tv_nickname).forEach {
            it?.setTextColor(ThemeUtil.darkText)
        }
        tv_job?.setTextColor(ThemeUtil.mediumText)
        arrayOf(ll_history_container, gl_setting_item).forEach {
            it?.background = ThemeUtil.getDrawable(context!!, R.drawable.bg_entry_box)
        }
        arrayOf(st_draft, st_data, st_game, st_meal, st_portal, st_query, st_read, st_use).forEach {
            it?.refreshTheme()
        }
        historyItemList.forEach { it.refreshTheme() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_PICTURE_DIR) {
            data?.let {
                val uri = data.data
                uri?.let {
                    val imgFile = privatePictureDir(this.context!!, "user_head.jpg")
                    copyFileFromUri(this.context!!, uri, imgFile)
                    setHeadImg()

//                    val df = DocumentFile.fromTreeUri(context!!, uri)
//                    val scpDir = df?.findFile("SCP")
//                    val picPath = scpDir?.findFile("scp_user_head.jpg")
//                    picPath?.let {
//                        val pfd = context?.contentResolver?.openFileDescriptor(picPath.uri, "r")
//                        pfd?.let {
//                            val fileInputStream = FileInputStream(pfd.fileDescriptor)
//                            iv_user_head?.setImageBitmap(BitmapFactory.decodeStream(fileInputStream))
//                            fileInputStream.close()
//                        }
//                    }
                }
            }
        } else {
//            data?.let {
//                val uri = data.data
//                uri?.let {
//                    try {
//                        val file = Utils.getFileByUri(uri, context!!)
//                        file?.let { f ->
//                            Utils.save(f, "scp_user_head")
//                            iv_user_head?.setImageBitmap(BitmapFactory.decodeFile(f.path))
//                        }
//                    } catch (e: Exception) {
//                        Log.e("Exception", e.message, e)
//                    }
//                }
//            }
        }
        super.onActivityResult(requestCode, resultCode, data)
        Log.i("user", "requestCode = $requestCode")
    }


    companion object {
        fun newInstance(): UserFragment {
            return UserFragment()
        }
    }
}
