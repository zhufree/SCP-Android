package info.free.scp.view.game

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import com.bumptech.glide.Glide
import info.free.scp.bean.GameModel
import kotlinx.android.synthetic.main.item_pc_game.view.*

/**
 * Created by zhufree on 2018/11/21.
 *
 */

class GameHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
    private var expandStatus = 0 // 0 收起 1 展开

    fun setData(model: GameModel) {
        itemView.tv_game_title?.text = model.name
        itemView.tv_game_desc?.text = model.desc
        itemView.tv_game_platform?.text = "平台: ${model.platform}"
        itemView.tv_game_price?.text = "价格: ${model.price}"
        itemView.iv_game_cover?.let {
            Glide.with(itemView.context).load(model.coverLink).into(it)
        }
        itemView.iv_game_cover?.setOnClickListener {
            itemView.gp_game_second_content?.visibility = if (expandStatus == 0) VISIBLE else GONE
            itemView.gp_game_first_content?.visibility = if (expandStatus == 1) VISIBLE else GONE
            expandStatus = if (expandStatus == 0) 1 else 0
        }
        itemView.btn_view_game?.setOnClickListener {
            val intent = Intent()
            intent.action = ACTION_VIEW
            intent.data = Uri.parse(model.link)
            itemView.context.startActivity(intent)
        }
    }
}