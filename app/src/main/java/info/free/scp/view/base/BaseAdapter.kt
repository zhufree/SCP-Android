package info.free.scp.view.base

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View

/**
 * Created by zhufree on 2018/8/23.
 * Adapter基类
 * 主要处理item点击长按事件监听器的绑定
 * [H] Holder
 * [T] 数据Model
 * 继承时要指定
 * // TODO 用databinding的adapter替换
 */

abstract class BaseAdapter<H : RecyclerView.ViewHolder, T : Any?>(
    context: Context?,
    val dataList: List<T>
) : RecyclerView.Adapter<H>(),
    View.OnLongClickListener, View.OnClickListener {
    var mOnItemClickListener: OnItemClickListener? = null
    var mOnItemLongClickListener: OnItemLongClickListener? = null
    var mInflater: LayoutInflater? = LayoutInflater.from(context)
    var mContext: Context? = context

//    abstract fun refreshItem(data: List<T>)
//    abstract fun addItem(data: T)


    override fun onClick(v: View) {
        mOnItemClickListener?.onItemClick(v, v.tag as Int)
    }

    override fun onLongClick(v: View): Boolean {
        mOnItemLongClickListener?.onItemLongClick(v, v.tag as Int)
        return true
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(view: View, position: Int)
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}
