package info.free.scp.view.download

import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import info.free.scp.ScpApplication
import info.free.scp.bean.DownloadModel
import info.free.scp.databinding.ItemDownloadBinding
import info.free.scp.db.ScpDatabase
import info.free.scp.util.DownloadUtil
import info.free.scp.util.FileUtil
import info.free.scp.util.PreferenceUtil
import org.jetbrains.anko.*


class DownloadAdapter : ListAdapter<DownloadModel, DownloadAdapter.DownloadHolder>(DownloadDiffCallback()) {

    val downloadList = arrayListOf(-1L, -1L, -1L, -1L, -1L, -1L)
    val holderList = arrayListOf<DownloadHolder>()
    var mStartVideoHandler: Handler = Handler()
    private var runnable: Runnable? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadHolder {

        val holder = DownloadHolder(ItemDownloadBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
        holderList.add(holder)
        if (holderList.size == 6 && runnable == null) {
            runnable = Runnable {
                for ((index, id) in downloadList.withIndex()) {
                    if (id > 0) {
                        holderList[index].refreshProgress(DownloadUtil.getDownloadInfo(id))
                    }
                }
                runnable?.let {
                    mStartVideoHandler.postDelayed(runnable, 1000)
                }
            }
            runnable?.run()
        }
        return holder
    }

    override fun onBindViewHolder(holder: DownloadHolder, position: Int) {
        val download = getItem(position)
        // 绑定 holder
        holder.apply {
            val link = PreferenceUtil.getDataDownloadLink(position - 1)
            if (link.isEmpty()) {
                ScpApplication.currentActivity?.toast("下载链接未加载成功，请稍后再进入该页面")
            }
            ScpApplication.currentActivity?.info("download: $position: $link")
            bind(createOnClickListener(position), download) // 点击事件
            itemView.tag = download
        }
    }

//    private fun getFileNameByIndex(index: Int): String {
//        return when (index) {
//            -1 -> "full_scp_data.db"
//            else -> "only_scp_$index.db"
//        }
//    }

    private fun createOnClickListener(position: Int): View.OnClickListener {
        return View.OnClickListener {
            val fileHelper = FileUtil.getInstance(ScpApplication.context)
            if (position == 0) {
                // 检查本地是否有已经下载过的
                if (fileHelper.checkBackupDataExist() && downloadList[position] == -1L) { // 是总数据库
                    ScpApplication.currentActivity?.alert("检测到该数据库之前已下载完成，是否恢复？", "恢复") {
                        positiveButton("恢复") {
                            ScpApplication.context.toast("开始恢复")
                            doAsync {
                                if (fileHelper.restoreData()) {
                                    ScpDatabase.getNewInstance()
                                    uiThread {
                                        ScpApplication.currentActivity?.toast("恢复完成")
                                    }
                                }
                            }
                        }
                        negativeButton("仍要下载") {
                            toggleDownloadStatus(position, PreferenceUtil.getDataDownloadLink(position - 1))
                        }
                        neutralPressed("取消") {}
                    }?.show()
                } else {
                    toggleDownloadStatus(position, PreferenceUtil.getDataDownloadLink(position - 1))
                }
            } else {
                toggleDownloadStatus(position, PreferenceUtil.getDataDownloadLink(position - 1))
            }
        }
    }

    private fun toggleDownloadStatus(position: Int, url: String) {
        val downloadId = downloadList[position]
        if (downloadId < 0) {
            val newDownloadId = DownloadUtil.createDownload(url)
            downloadList[position] = newDownloadId
        } else {
            ScpApplication.downloadManager.remove(downloadId)
            downloadList[position] = -1
            holderList[position].refreshProgress(intArrayOf(0, 0, -1))
        }

    }

    class DownloadHolder(private val binding: ItemDownloadBinding) : RecyclerView.ViewHolder(binding.root) {

        var index = 0
        fun bind(listener: View.OnClickListener, item: DownloadModel) {
            // 具体绑定监听事件和数据
            binding.apply {
                clickListener = listener
                download = item
                index = item.dbIndex
                tvDownloadTime.text = "本地同步时间：" + item.lastDownloadTime
                tvUpdateTime.text = "服务器更新时间：" + item.lastUpdateTime

                executePendingBindings()
            }
        }

        fun refreshProgress(info: IntArray) {
            binding.tvDownloadProgress.text = "${info[0] / 1000000}M/${info[1] / 1000000}M ${getStatusByCode(info[2])}"
        }

        private fun getStatusByCode(code: Int): String {
            return when (code) {
                1 shl 0 -> {
                    "PENDING"
                }
                1 shl 1 -> {
                    "下载中"
                }
                1 shl 2 -> {
                    "暂停"
                }
                1 shl 3 -> {
                    PreferenceUtil.setDetailLastLoadTime(index, System.currentTimeMillis())
                    "下载成功"
                }
                1 shl 4 -> {
                    "下载失败"
                }
                -1 -> {
                    "取消下载"
                }
                else -> {
                    "PENDING"
                }
            }
        }
    }


    /**
     * 用来对比列表中数据是否变化
     */
    private class DownloadDiffCallback : DiffUtil.ItemCallback<DownloadModel>() {

        /**
         * 是否是同一个item
         */
        override fun areItemsTheSame(
                oldItem: DownloadModel,
                newItem: DownloadModel
        ): Boolean {
            return oldItem.title == newItem.title
        }

        /**
         * item的内容是否一致，仅当[areItemsTheSame]返回true时才调用做进一步判断
         */
        override fun areContentsTheSame(
                oldItem: DownloadModel,
                newItem: DownloadModel
        ): Boolean {
            return oldItem == newItem
        }
    }
}