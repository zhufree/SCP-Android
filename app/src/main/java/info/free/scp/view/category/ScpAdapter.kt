package info.free.scp.view.category

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import info.free.scp.R
import info.free.scp.bean.ScpModel
import info.free.scp.view.base.BaseAdapter

/**
 * Created by zhufree on 2018/8/22.
 *
 */

class ScpAdapter(mContext: Context, dataList: MutableList<ScpModel>)
    : BaseAdapter<CategoryHolder, ScpModel>(mContext, dataList) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_category, parent, false)
//        val newHolder = CategoryHolder(view)
        view?.setOnLongClickListener(this)
        view?.setOnClickListener(this)
        return CategoryHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
        holder.itemView.tag = position
        holder.setData(dataList[position])
    }
}
