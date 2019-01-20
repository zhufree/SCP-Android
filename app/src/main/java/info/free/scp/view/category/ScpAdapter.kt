package info.free.scp.view.category

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import info.free.scp.R
import info.free.scp.SCPConstants.LATER_TYPE
import info.free.scp.bean.ScpModel
import info.free.scp.bean.SimpleScp
import info.free.scp.db.ScpDao
import info.free.scp.view.base.BaseAdapter

/**
 * Created by zhufree on 2018/8/22.
 *
 */

class ScpAdapter(mContext: Context, dataList: MutableList<ScpModel?>)
    : BaseAdapter<ScpHolder, ScpModel?>(mContext, dataList) {
    var laterViewList = emptyList<SimpleScp>().toMutableList()

    init {
        laterViewList = ScpDao.getInstance().getViewListByTypeAndOrder(LATER_TYPE, 0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScpHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_category, parent, false)
        view?.setOnLongClickListener(this)
        view?.setOnClickListener(this)
        return ScpHolder(view)
    }

    override fun onBindViewHolder(holder: ScpHolder, position: Int) {
        holder.itemView.tag = position
        holder.setData(dataList?.get(position), laterViewList)
    }
}
