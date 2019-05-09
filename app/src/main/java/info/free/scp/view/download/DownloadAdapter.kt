package info.free.scp.view.download

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lzy.okgo.model.Progress
import com.lzy.okserver.download.DownloadListener
import info.free.scp.PrivateConstants
import info.free.scp.bean.DownloadModel
import info.free.scp.databinding.ItemDownloadBinding
import info.free.scp.util.DownloadManager
import info.free.scp.util.ThemeUtil
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
            bind(createOnClickListener(download), download) // 点击事件
            itemView.tag = download
        }
    }

    private fun createOnClickListener(download: DownloadModel): View.OnClickListener {
        return View.OnClickListener {
            DownloadManager.downloadDb(PrivateConstants.SCP_DB_3_LINK)
        }
    }

    class DownloadHolder(private val binding: ItemDownloadBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(listener: View.OnClickListener, item: DownloadModel) {
            // 具体绑定监听事件和数据
            binding.apply {
                clickListener = listener
                download = item
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
                0 -> Color.GREEN
                1 -> Color.LTGRAY
                else -> Color.LTGRAY

            }
            binding.vDownloadStatus.post {
                binding.vDownloadStatus.background = ThemeUtil.customShape(fillColor, fillColor, 0,
                        itemView.context.dip(5))
            }
        }
    }

    class ListDownloadListener(tag: Any, val holder: DownloadHolder) : DownloadListener(tag) {
        override fun onFinish(t: File?, progress: Progress?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onRemove(progress: Progress?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
            if (progress?.status == Progress.LOADING) {

            }
        }

        override fun onError(progress: Progress?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onStart(progress: Progress?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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