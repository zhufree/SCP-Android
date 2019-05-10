package info.free.scp.view.download

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Progress
import com.lzy.okgo.request.GetRequest
import com.lzy.okserver.OkDownload
import com.lzy.okserver.download.DownloadListener
import com.lzy.okserver.download.DownloadTask
import info.free.scp.PrivateConstants
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.ScpApplication
import info.free.scp.bean.DownloadModel
import info.free.scp.databinding.ItemDownloadBinding
import info.free.scp.util.DownloadUtil.Status.DOWNLOADING
import info.free.scp.util.DownloadUtil.Status.ERROR
import info.free.scp.util.DownloadUtil.Status.FINISH
import info.free.scp.util.DownloadUtil.Status.NEED_UPDATE
import info.free.scp.util.DownloadUtil.Status.NONE
import info.free.scp.util.DownloadUtil.Status.PAUSE
import info.free.scp.util.Logger
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.util.Utils
import org.jetbrains.anko.dip
import java.io.File

class DownloadAdapter : ListAdapter<DownloadModel, DownloadAdapter.DownloadHolder>(DownloadDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadHolder {
        return DownloadHolder(ItemDownloadBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: DownloadHolder, position: Int) {
        val download = getItem(position)
        // 绑定 holder
        holder.apply {
            val link = PreferenceUtil.getDataDownloadLink(position-1)
            Logger.i("download: $position: $link")
            val request: GetRequest<File> = OkGo.get(link)
            val task = OkDownload.request(link, request)
                    .fileName(SCPConstants.SCP_DB_NAME)
                    .register(ListDownloadListener(download.title, this))
                    .save()
            bind(createOnClickListener(task), download) // 点击事件
            itemView.tag = download
        }
    }

    private fun createOnClickListener(task: DownloadTask): View.OnClickListener {
        return View.OnClickListener {
//            DownloadUtil.downloadDb(PrivateConstants.SCP_DB_3_LINK)
            if (task.progress.status == 2) {
                task.pause()
            } else {
                task.start()
            }
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
                val fillColor = when (item.status) {
                    0 -> Color.GREEN
                    1 -> Color.LTGRAY
                    else -> Color.LTGRAY

                }
                vDownloadStatus.post {
                    vDownloadStatus.background = ThemeUtil.customShape(fillColor, fillColor, 0,
                            itemView.context.dip(5))
                }
                executePendingBindings()
            }
        }

        fun refreshStatus(status: Int) {
            val fillColor = when (status) {
                FINISH -> Color.GREEN
                NONE -> Color.LTGRAY
                DOWNLOADING -> Color.BLUE
                PAUSE -> itemView.context.resources.getColor(R.color.colorAccent)
                NEED_UPDATE -> Color.YELLOW
                ERROR -> Color.RED
                else -> Color.LTGRAY

            }
            binding.vDownloadStatus.post {
                binding.vDownloadStatus.background = ThemeUtil.customShape(fillColor, fillColor, 0,
                        itemView.context.dip(5))
            }
            if (status == FINISH) {
                binding.tvDownloadProgress.text = ""
                // TODO 更新时间 离线完成时间
                PreferenceUtil.setDetailLastLoadTime(index, Utils.formatNow())
            }
        }

        fun refreshProgress(progress: Progress?) {
            binding.tvDownloadProgress.text = "${progress?.currentSize?.div(1000)}KB" +
                    "/${progress?.totalSize?.div(1000000)}MB"
        }
    }

    class ListDownloadListener(tag: Any, val holder: DownloadHolder) : DownloadListener(tag) {
        override fun onFinish(t: File?, progress: Progress?) {
            holder.refreshStatus(FINISH)

        }

        override fun onRemove(progress: Progress?) {
        }

        /**
         * int NONE = 0;
         * int WAITING = 1;
         * int LOADING = 2;
         * int PAUSE = 3;
         * int ERROR = 4;
         * int FINISH = 5;
         * @param progress Progress?
         */
        override fun onProgress(progress: Progress?) {
            holder.refreshProgress(progress)
            if (progress?.status == Progress.PAUSE) {
                holder.refreshStatus(PAUSE)
            }
        }

        override fun onError(progress: Progress?) {
            holder.refreshStatus(ERROR)
        }

        override fun onStart(progress: Progress?) {
            holder.refreshStatus(DOWNLOADING)
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