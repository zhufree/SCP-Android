package info.free.scp.view.user


import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import info.free.scp.BuildConfig
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.HISTORY_TYPE
import info.free.scp.SCPConstants.RequestCode.REQUEST_PICTURE_DIR
import info.free.scp.databinding.FragmentUserBinding
import info.free.scp.db.AppInfoDatabase
import info.free.scp.util.*
import info.free.scp.util.ThemeUtil.DAY_THEME
import info.free.scp.util.ThemeUtil.NIGHT_THEME
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.detail.DetailActivity
import info.free.scp.view.draft.DraftListActivity
import info.free.scp.view.later.LaterViewModel
import info.free.scp.view.widget.HistoryListItem
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.*
import java.util.*


/**
 * Use the [UserFragment.newInstance] factory method to
 * create an instance of this fragment.
 * 其他，包括写作相关，新人资讯，标签云和关于
 */
class UserFragment : BaseFragment() {
    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding.root
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
        if (!PreferenceUtil.hasCheckPrivacy()) {
            NewbieManager.showPrivacyDialog(this.activity!!)
        }
//        childFragmentManager.beginTransaction().replace(R.id.fl_settings, SettingsFragment()).commit()
        binding.tvNickname.text = if (PreferenceUtil.getNickname().isNotEmpty())
            "代号：${PreferenceUtil.getNickname()}" else "代号：点击设置"
        binding.tvJob.text = if (PreferenceUtil.getNickname().isNotEmpty())
            "编号：${Random(System.currentTimeMillis()).nextInt(600)}\n" +
                    "职务：${getRank(PreferenceUtil.getPoint())}"
        else "编号：${Random(System.currentTimeMillis()).nextInt(600)}\n" +
                "职务：点击设置"
        binding.tvDataDesc.text =
            "已研究项目：${AppInfoDatabase.getInstance().likeAndReadDao().getReadCount()}\t" +
                    "已跟踪项目：${AppInfoDatabase.getInstance().likeAndReadDao().getLikeCount()}"

        binding.tvNickname.setOnClickListener { checkUserInfo() }
        binding.tvJob.setOnClickListener {
            checkJob()
        }
        if (PreferenceUtil.getShowMeal()) {
            binding.stOtherWork.visibility = VISIBLE
        }

        refreshHistoryList()
        setSettingEvent()

        Glide.with(this).load(R.drawable.author_head)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(dip(25))))
            .into(binding.ivAuthorHead)

        setHeadImg()

        binding.ivUserHead.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            startActivityForResult(intent, REQUEST_PICTURE_DIR)
        }
    }

    private fun refreshHistoryList() {
        binding.llHistoryList.removeAllViews()
        historyItemList.clear()
        val historyList = viewModel.getRecordList(HISTORY_TYPE, SCPConstants.OrderType.DESC)
        historyList.forEachIndexed { index, scp ->
            if (index < 5) {
                val newItem = HistoryListItem(this.context!!, scp.title, scp.showTime())
                newItem.onItemClick = {
                    startActivity<DetailActivity>("link" to scp.link, "title" to scp.title)
                }
                binding.llHistoryList.addView(newItem)
                historyItemList.add(newItem)
            }
        }
    }

    private fun setSettingEvent() {
        binding.ibTheme.setOnClickListener {
            ThemeUtil.changeTheme(
                activity,
                if (ThemeUtil.currentTheme == 1) DAY_THEME else NIGHT_THEME
            )
        }

        binding.stDraft.onClick = {
            startActivity<DraftListActivity>()
        }
        binding.stGame.onClick = { startActivity<GameListActivity>() }
        binding.stOtherWork.onClick = { startActivity<OtherAppActivity>() }
        binding.stPortal.onClick = { startActivity<PortalListActivity>() }

        binding.ivMoreHistory.setOnClickListener { startActivity<LaterAndHistoryActivity>() }

        binding.stRead.onClick = {
            EventUtil.onEvent(activity, EventUtil.clickReadSetting)
            startActivity<SettingsActivity>()
        }
        binding.stData.onClick = {
            startActivity<DownloadActivity>()
        }
        binding.stUse.onClick = {
            startActivity<AboutAppActivity>()
        }
        if (BuildConfig.FLAVOR == "GooglePlayProduction") {
            binding.btnDonation.visibility = GONE
        }
        binding.btnDonation.setOnClickListener {
            startActivity<DonationQrActivity>()
        } // others
        binding.stQuery.onClick = {
            Utils.openUrl(PreferenceUtil.getQueryLink())
        }
    }


    private fun setHeadImg() {
        val imgFile = privatePictureDir(this.context!!, "user_head.jpg")
        if (imgFile.exists()) {
            binding.ivUserHead.setImageBitmap(BitmapFactory.decodeFile(imgFile.path))
        }
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
                binding.tvNickname?.text = if (PreferenceUtil.getNickname().isNotEmpty())
                    "代号：${PreferenceUtil.getNickname()}" else "代号：点击设置"
                checkJob()
            }
            negativeButton("取消") {}

        }.show()
    }

    private fun checkJob() {
        val jobList = listOf("收容专家", "研究员", "安全人员", "战术反应人员", "外勤特工", "机动特遣队作业员")
        selector("欢迎来到SCP基金会，${PreferenceUtil.getNickname()}，请选择你的职业",
                jobList) { out, i ->
            PreferenceUtil.setJob(jobList[i])
            binding.tvJob?.text = if (PreferenceUtil.getJob().isNotEmpty())
                "编号：${Random(System.currentTimeMillis()).nextInt(600)}\n" +
                        "职务：${getRank(PreferenceUtil.getPoint())}"
            else "编号：${Random(System.currentTimeMillis()).nextInt(600)}\n" +
                    "职务：点击设置"
        }
    }

    override fun onResume() {
        super.onResume()
        refreshHistoryList()
    }

    override fun refreshTheme() {
        super.refreshTheme()
        if (_binding == null) {
            return
        }
        arrayOf(binding.tvHistoryListHead, binding.tvNickname).forEach {
            it.setTextColor(ThemeUtil.darkText)
        }
        binding.tvJob.setTextColor(ThemeUtil.mediumText)
        arrayOf(binding.llHistoryContainer, binding.glSettingItem).forEach {
            it.background = ThemeUtil.getDrawable(context!!, R.drawable.bg_entry_box)
        }
        arrayOf(
            binding.stDraft,
            binding.stData,
            binding.stGame,
            binding.stOtherWork,
            binding.stPortal,
            binding.stQuery,
            binding.stRead,
            binding.stUse
        ).forEach {
            it.refreshTheme()
        }
        historyItemList.forEach { it.refreshTheme() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_PICTURE_DIR && resultCode == Activity.RESULT_OK) {
            data?.let {
                val uri = data.data
                uri?.let {
                    val imgFile = privatePictureDir(this.context!!, "user_head.jpg")
                    copyFileFromUri(this.context!!, uri, imgFile)
                    setHeadImg()
                }
            }
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
