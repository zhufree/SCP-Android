package info.free.scp.view.game

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import info.free.scp.R
import info.free.scp.bean.GameModel
import info.free.scp.view.base.BaseAdapter

/**
 * Created by zhufree on 2018/11/21.
 *
 */

class GameListAdapter(mContext: Context, dataList: MutableList<GameModel>)
    : BaseAdapter<GameHolder, GameModel>(mContext, dataList) {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): GameHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_game_list, parent, false)
        view?.setOnLongClickListener(this)
        view?.setOnClickListener(this)
        return GameHolder(view)
    }

    override fun onBindViewHolder(holder: GameHolder, position: Int) {
        holder.itemView.tag = position
        holder.setData(dataList[position])
    }
}