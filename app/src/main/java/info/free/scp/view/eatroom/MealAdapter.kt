package info.free.scp.view.eatroom

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
import info.free.scp.bean.MealModel
import info.free.scp.databinding.ItemMealBinding
import info.free.scp.util.EventUtil

class MealAdapter : ListAdapter<MealModel, MealAdapter.MealHolder>(MealDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealHolder {
        return MealHolder(ItemMealBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: MealHolder, position: Int) {
        val meal = getItem(position)
        // 绑定 holder
        holder.apply {
            bind(createOnClickListener(meal), meal) // 点击事件
            itemView.tag = meal
        }
    }

    private fun createOnClickListener(meal: MealModel): View.OnClickListener {
        return View.OnClickListener {
            EventUtil.onEvent(it.context, EventUtil.clickMeal, meal.link)
            val updateIntent = Intent()
            updateIntent.action = "android.intent.action.VIEW"
            val updateUrl = Uri.parse(meal.link)
            updateIntent.data = updateUrl
            (it.context as Activity).startActivity(updateIntent)
        }
    }

    class MealHolder(private val binding: ItemMealBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(listener: View.OnClickListener, item: MealModel) {
            // 具体绑定监听事件和数据
            binding.apply {
                clickListener = listener
                meal = item
                Glide.with(itemView.context)
                        .load(item.logoUrl)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(ivMealCover)
                executePendingBindings()
            }
        }
    }

    /**
     * 用来对比列表中数据是否变化
     */
    private class MealDiffCallback : DiffUtil.ItemCallback<MealModel>() {

        /**
         * 是否是同一个item
         */
        override fun areItemsTheSame(
                oldItem: MealModel,
                newItem: MealModel
        ): Boolean {
            return oldItem.objectId == newItem.objectId
        }

        /**
         * item的内容是否一致，仅当[areItemsTheSame]返回true时才调用做进一步判断
         */
        override fun areContentsTheSame(
                oldItem: MealModel,
                newItem: MealModel
        ): Boolean {
            return oldItem == newItem
        }
    }
}