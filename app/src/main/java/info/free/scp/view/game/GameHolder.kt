package info.free.scp.view.game

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.View
import com.bumptech.glide.Glide
import info.free.scp.bean.GameModel
import kotlinx.android.synthetic.main.item_game_list.view.*

/**
 * Created by zhufree on 2018/11/21.
 *
 */

class GameHolder(view: View) : RecyclerView.ViewHolder(view) {
    var expandStatus = 0 // 0 收起 1 展开

    fun setData(model: GameModel) {
        itemView.tv_game_title?.text = model.name
        itemView.tv_game_desc?.text = model.desc
        itemView.tv_game_platform?.text = model.platform
        itemView.tv_game_price?.text = model.price
        itemView.iv_game_cover?.let {
            Glide.with(itemView.context).load(model.coverLink).into(it)
        }
        itemView.iv_game_cover?.setOnClickListener {
            itemView.gp_game_hide_content?.animate()?.translationY(if (expandStatus == 0) -900f else 0f)
                    ?.setDuration(500)?.start()
        }
        itemView.btn_view_game?.setOnClickListener {
            val intent = Intent()
            intent.action = ACTION_VIEW
            intent.data = Uri.parse(model.link)
            itemView.context.startActivity(intent)
        }
    }
}