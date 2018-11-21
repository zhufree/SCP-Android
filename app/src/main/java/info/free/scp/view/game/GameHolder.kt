package info.free.scp.view.game

import android.support.v7.widget.RecyclerView
import android.view.View
import info.free.scp.bean.GameModel
import kotlinx.android.synthetic.main.item_category.view.*

/**
 * Created by zhufree on 2018/11/21.
 *
 */

class GameHolder(view: View) : RecyclerView.ViewHolder(view) {
    fun setData(model: GameModel) {
        itemView?.tvScpTitle?.text = model.name
    }
}