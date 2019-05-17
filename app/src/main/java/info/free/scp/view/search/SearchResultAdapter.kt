package info.free.scp.view.search

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.bean.ScpModel
import info.free.scp.bean.ScpRecordModel
import info.free.scp.bean.SimpleScp
import info.free.scp.db.ScpDataHelper
import info.free.scp.view.base.BaseAdapter

/**
 * Created by zhufree on 2018/10/25.
 *
 */

// TODO 把model简化成通用的
class SearchResultAdapter(mContext: Context, dataList: MutableList<ScpModel?>)
    : BaseAdapter<SearchHolder, ScpModel?>(mContext, dataList) {
    private var laterViewList = emptyList<ScpRecordModel>().toMutableList()

    init {
        laterViewList = ScpDataHelper.getInstance().getViewListByTypeAndOrder(SCPConstants.LATER_TYPE, 0)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_search, parent, false)
        //        val newHolder = CategoryHolder(view)
        view?.setOnLongClickListener(this)
        view?.setOnClickListener(this)
        return SearchHolder(view)
    }

    override fun onBindViewHolder(holder: SearchHolder, position: Int) {
        holder.itemView.tag = position
        holder.setData(dataList[position]?.link?:"",dataList[position]?.title?:"", null, laterViewList)
    }
}