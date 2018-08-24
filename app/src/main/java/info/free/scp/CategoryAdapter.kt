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

class CategoryAdapter(mContext: Context, dataList: MutableList<SimpleSCPModel>)
    : BaseAdapter<CategoryHolder, SimpleSCPModel>(mContext, dataList) {

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
