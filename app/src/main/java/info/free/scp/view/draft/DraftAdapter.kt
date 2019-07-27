package info.free.scp.view.draft

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import info.free.scp.bean.DraftModel
import info.free.scp.databinding.ItemDraftBinding
import info.free.scp.db.AppInfoDatabase
import info.free.scp.util.TimeUtil
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.yesButton

class DraftAdapter : ListAdapter<DraftModel, DraftAdapter.DraftHolder>(DraftDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DraftHolder {
        return DraftHolder(ItemDraftBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: DraftHolder, position: Int) {
        val draft = getItem(position)
        // 绑定 holder
        holder.apply {
            bind(createOnClickListener(draft), createOnLongClickListener(draft), draft) // 点击事件
            itemView.tag = draft
        }
    }

    private fun createOnClickListener(draft: DraftModel): View.OnClickListener {
        return View.OnClickListener {
            (it.context as Activity).startActivity<DraftEditActivity>("draftId" to draft.draftId)
        }
    }

    private fun createOnLongClickListener(draft: DraftModel): View.OnLongClickListener {
        return View.OnLongClickListener {
            (it.context as Activity).alert("确定删除草稿吗？") {
                yesButton {
                    AppInfoDatabase.getInstance().draftDao().delete(draft.draftId)
                }
                noButton {  }
            }.show()
            true
        }
    }

    class DraftHolder(private val binding: ItemDraftBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(listener: View.OnClickListener, longListener: View.OnLongClickListener,
                 item: DraftModel) {
            // 具体绑定监听事件和数据
            binding.apply {
                clickListener = listener
                longClickListener = longListener
                draft = item
                tvDraftTime.text = TimeUtil.timeStampToStr(item.lastModifyTime)
                executePendingBindings()
            }
        }
    }

    /**
     * 用来对比列表中数据是否变化
     */
    private class DraftDiffCallback : DiffUtil.ItemCallback<DraftModel>() {

        /**
         * 是否是同一个item
         */
        override fun areItemsTheSame(
                oldItem: DraftModel,
                newItem: DraftModel
        ): Boolean {
            return oldItem.draftId == newItem.draftId
        }

        /**
         * item的内容是否一致，仅当[areItemsTheSame]返回true时才调用做进一步判断
         */
        override fun areContentsTheSame(
                oldItem: DraftModel,
                newItem: DraftModel
        ): Boolean {
            return oldItem == newItem
        }
    }
}