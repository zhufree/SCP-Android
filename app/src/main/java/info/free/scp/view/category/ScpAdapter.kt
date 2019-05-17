package info.free.scp.view.category

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import info.free.scp.R
import info.free.scp.SCPConstants.LATER_TYPE
import info.free.scp.bean.ScpLikeModel
import info.free.scp.bean.ScpModel
import info.free.scp.bean.ScpRecordModel
import info.free.scp.bean.SimpleScp
import info.free.scp.db.ScpDataHelper
import info.free.scp.view.base.BaseAdapter

/**
 * Created by zhufree on 2018/8/22.
 *
 */

class ScpAdapter(mContext: Context, dataList: MutableList<ScpModel?>)
    : BaseAdapter<ScpCategoryHolder, ScpModel?>(mContext, dataList) {
    private var laterViewList = emptyList<ScpRecordModel>().toMutableList()

    init {
        laterViewList = ScpDataHelper.getInstance().getViewListByTypeAndOrder(LATER_TYPE, 0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScpCategoryHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_category, parent, false)
        view?.setOnLongClickListener(this)
        view?.setOnClickListener(this)
        return ScpCategoryHolder(view)
    }

    override fun onBindViewHolder(holder: ScpCategoryHolder, position: Int) {
        holder.itemView.tag = position
        holder.setData(dataList?.get(position), laterViewList)
    }
}
