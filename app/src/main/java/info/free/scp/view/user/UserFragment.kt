package info.free.scp.view.user

import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import info.free.scp.BuildConfig
import info.free.scp.R
import info.free.scp.bean.ScpRecordModel
import info.free.scp.databinding.FragmentUserBinding
import info.free.scp.ui.user.UserDashboardViewModel
import info.free.scp.util.EventUtil
import info.free.scp.util.NewbieManager
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.util.ThemeUtil.DAY_THEME
import info.free.scp.util.ThemeUtil.NIGHT_THEME
import info.free.scp.util.Utils
import info.free.scp.util.copyFileFromUri
import info.free.scp.util.privatePictureDir
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.detail.DetailActivity
import info.free.scp.view.draft.DraftListActivity
import info.free.scp.view.widget.HistoryListItem
import kotlinx.coroutines.launch
import java.util.Random

/**
 * 用户页，包含写作相关入口、新人资讯、站点入口和关于信息。
 */
class UserFragment : BaseFragment() {
    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!
    private val pickUserHeadImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val imgFile = privatePictureDir(requireContext(), "user_head.jpg")
                copyFileFromUri(requireContext(), it, imgFile)
                setHeadImg()
            }
        }

    private val dashboardViewModel by lazy {
        ViewModelProvider(this)[UserDashboardViewModel::class.java]
    }

    private val historyItemList = mutableListOf<HistoryListItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!PreferenceUtil.hasCheckPrivacy()) {
            NewbieManager.showPrivacyDialog(requireActivity())
        }

        refreshProfileTexts()
        observeUserDashboardState()
        dashboardViewModel.refreshUserDashboard()

        binding.tvNickname.setOnClickListener { checkUserInfo() }
        binding.tvJob.setOnClickListener { checkJob() }

        if (PreferenceUtil.getShowMeal()) {
            binding.stOtherWork.visibility = VISIBLE
        }

        setSettingEvent()

        Glide.with(this)
            .load(R.drawable.author_head)
            .apply(
                RequestOptions.bitmapTransform(
                    RoundedCorners(Utils.dp2px(25, requireContext()))
                )
            )
            .into(binding.ivAuthorHead)

        setHeadImg()

        binding.ivUserHead.setOnClickListener {
            pickUserHeadImage.launch("image/*")
        }
    }

    private fun observeUserDashboardState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                dashboardViewModel.uiState.collect { uiState ->
                    binding.tvDataDesc.text = "已阅读项目：${uiState.readCount}\t已收藏项目：${uiState.likeCount}"
                    refreshHistoryList(uiState.recentHistory)
                }
            }
        }
    }

    private fun getRank(point: Int): String {
        Log.i("point", "point = $point")
        return when {
            point < 200 -> PreferenceUtil.getJobNameByLevel(0)
            point < 700 -> PreferenceUtil.getJobNameByLevel(1)
            point < 1500 -> PreferenceUtil.getJobNameByLevel(2)
            point < 2500 -> "C级 ${PreferenceUtil.getJobNameByLevel(3)}"
            point < 4000 -> "B级 ${PreferenceUtil.getJobNameByLevel(4)}"
            else -> "A级 ${PreferenceUtil.getJobNameByLevel(5)}"
        }
    }

    private fun refreshProfileTexts() {
        val nickname = PreferenceUtil.getNickname()
        val code = Random(System.currentTimeMillis()).nextInt(600)
        binding.tvNickname.text = if (nickname.isNotEmpty()) {
            "代号：$nickname"
        } else {
            "代号：点击设置"
        }
        binding.tvJob.text = if (nickname.isNotEmpty()) {
            "编号：$code\n职务：${getRank(PreferenceUtil.getPoint())}"
        } else {
            "编号：$code\n职务：点击设置"
        }
    }

    private fun refreshHistoryList(historyList: List<ScpRecordModel>) {
        binding.llHistoryList.removeAllViews()
        historyItemList.clear()
        historyList.forEach { scp ->
            val newItem = HistoryListItem(requireContext(), scp.title, scp.showTime())
            newItem.onItemClick = {
                startActivity(
                    Intent(requireContext(), DetailActivity::class.java)
                        .putExtra("link", scp.link)
                        .putExtra("title", scp.title)
                )
            }
            binding.llHistoryList.addView(newItem)
            historyItemList.add(newItem)
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
            startActivity(Intent(requireContext(), DraftListActivity::class.java))
        }
        binding.stGame.onClick = {
            startActivity(Intent(requireContext(), GameListActivity::class.java))
        }
        binding.stOtherWork.onClick = {
            startActivity(Intent(requireContext(), OtherAppActivity::class.java))
        }
        binding.stPortal.onClick = {
            startActivity(Intent(requireContext(), PortalListActivity::class.java))
        }

        binding.ivMoreHistory.setOnClickListener {
            startActivity(Intent(requireContext(), LaterAndHistoryActivity::class.java))
        }

        binding.stRead.onClick = {
            EventUtil.onEvent(activity, EventUtil.clickReadSetting)
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }
        binding.stData.onClick = {
            startActivity(Intent(requireContext(), DownloadActivity::class.java))
        }
        binding.stUse.onClick = {
            startActivity(Intent(requireContext(), AboutAppActivity::class.java))
        }
        if (BuildConfig.FLAVOR == "GooglePlayProduction") {
            binding.btnDonation.visibility = GONE
        }
        binding.btnDonation.setOnClickListener {
            startActivity(Intent(requireContext(), DonationQrActivity::class.java))
        }
        binding.stQuery.onClick = {
            Utils.openUrl(PreferenceUtil.getQueryLink())
        }
    }

    private fun setHeadImg() {
        val imgFile = privatePictureDir(requireContext(), "user_head.jpg")
        if (imgFile.exists()) {
            binding.ivUserHead.setImageBitmap(BitmapFactory.decodeFile(imgFile.path))
        }
    }

    private fun checkUserInfo() {
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                Utils.dp2px(16, requireContext()),
                Utils.dp2px(16, requireContext()),
                Utils.dp2px(16, requireContext()),
                Utils.dp2px(16, requireContext())
            )
        }
        val titleView = TextView(requireContext()).apply {
            text = "欢迎来到 SCP 基金会，调查员，请输入你的名字"
            setTextColor(ThemeUtil.darkText)
            textSize = 18f
        }
        val input = EditText(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            isSingleLine = true
            setText(PreferenceUtil.getNickname())
        }
        container.addView(titleView)
        container.addView(input)

        AlertDialog.Builder(requireContext())
            .setView(container)
            .setPositiveButton("确定") { _, _ ->
                PreferenceUtil.saveNickname(input.text?.toString().orEmpty())
                refreshProfileTexts()
                checkJob()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun checkJob() {
        val jobList = arrayOf(
            "收容专家",
            "研究员",
            "安保人员",
            "战术反应人员",
            "外勤特工",
            "机动特遣队成员"
        )
        AlertDialog.Builder(requireContext())
            .setTitle("欢迎来到 SCP 基金会，${PreferenceUtil.getNickname()}，请选择你的职业")
            .setItems(jobList) { _, i ->
                PreferenceUtil.setJob(jobList[i])
                refreshProfileTexts()
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        dashboardViewModel.refreshUserDashboard()
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
            it.background = ThemeUtil.getDrawable(requireContext(), R.drawable.bg_entry_box)
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

    companion object {
        fun newInstance(): UserFragment {
            return UserFragment()
        }
    }
}
