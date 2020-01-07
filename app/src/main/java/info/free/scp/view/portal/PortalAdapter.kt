package info.free.scp.view.portal

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import info.free.scp.bean.PortalModel
import info.free.scp.databinding.ItemPortalBinding

class PortalAdapter : ListAdapter<PortalModel, PortalAdapter.PortalHolder>(PortalDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PortalHolder {
        return PortalHolder(ItemPortalBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: PortalHolder, position: Int) {
        val portal = getItem(position)
        // 绑定 holder
        holder.apply {
            bind(createOnClickListener(portal), portal) // 点击事件
            itemView.tag = portal
        }
    }

    private fun createOnClickListener(portal: PortalModel): View.OnClickListener {
        return View.OnClickListener {
            val updateIntent = Intent()
            updateIntent.action = "android.intent.action.VIEW"
            val updateUrl = Uri.parse(portal.url)
            updateIntent.data = updateUrl
            (it.context as Activity).startActivity(updateIntent)
        }
    }

    class PortalHolder(private val binding: ItemPortalBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(listener: View.OnClickListener, item: PortalModel) {
            // 具体绑定监听事件和数据
            binding.apply {
                clickListener = listener
                portal = item
                Glide.with(itemView.context)
                        .load(item.logoUrl)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(ivPortalLogo)
                executePendingBindings()
            }
        }
    }

    /**
     * 用来对比列表中数据是否变化
     */
    private class PortalDiffCallback : DiffUtil.ItemCallback<PortalModel>() {

        /**
         * 是否是同一个item
         */
        override fun areItemsTheSame(
                oldItem: PortalModel,
                newItem: PortalModel
        ): Boolean {
            return oldItem.objectId == newItem.objectId
        }

        /**
         * item的内容是否一致，仅当[areItemsTheSame]返回true时才调用做进一步判断
         */
        override fun areContentsTheSame(
                oldItem: PortalModel,
                newItem: PortalModel
        ): Boolean {
            return oldItem == newItem
        }
    }
}