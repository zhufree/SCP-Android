package info.free.scp

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView

/**
 * Created by zhufree on 2018/8/22.
 *
 */

class CategoryAdapter(val dataList: MutableList<SimpleSCPModel>, val mContext: Context) : RecyclerView.Adapter<CategoryHolder>() {
    private var mOnItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_category, parent, false)
//        val newHolder = CategoryHolder(view)
        return CategoryHolder(view)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener { mOnItemClickListener?.onItemClick(holder, position) }
        }
        holder.setData(dataList[position])
    }

    fun setOnItemClickListener(l: OnItemClickListener) {
        this.mOnItemClickListener = l
    }


    interface OnItemClickListener {
        fun onItemClick(holder: CategoryHolder, position: Int)
    }
}
