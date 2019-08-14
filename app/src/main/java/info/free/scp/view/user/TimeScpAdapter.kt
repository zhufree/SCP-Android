package info.free.scp.view.user

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import info.free.scp.R
import info.free.scp.bean.ScpRecordModel
import info.free.scp.view.base.BaseAdapter
import info.free.scp.view.search.SearchHolder

/**
 * Created by zhufree on 2018/10/25.
 * 用于阅读记录和稍后阅读列表的adapter
 */

class TimeScpAdapter(mContext: Context, dataList: MutableList<ScpRecordModel?>)
    : BaseAdapter<SearchHolder, ScpRecordModel?>(mContext, dataList) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_search, parent, false)
        view?.setOnLongClickListener(this)
        view?.setOnClickListener(this)
        return SearchHolder(view)
    }

    override fun onBindViewHolder(holder: SearchHolder, position: Int) {
        holder.itemView.tag = position
        holder.setData(dataList[position]?.link?:"", dataList[position]?.title?:"",
                dataList[position]?.viewTime.toString(), null)
    }
}