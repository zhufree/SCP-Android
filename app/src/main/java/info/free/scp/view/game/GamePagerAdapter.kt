package info.free.scp.view.game

import android.support.v4.view.PagerAdapter
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearLayoutManager.VERTICAL
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import info.free.scp.bean.GameModel

/**
 * Created by zhufree on 2018/11/21.
 *
 */

class GamePagerAdapter : PagerAdapter() {
    private var gameTypeList: MutableList<RecyclerView> = emptyList<RecyclerView>().toMutableList()
    private var pcGameList: MutableList<GameModel> = emptyList<GameModel>().toMutableList()
    private var mobileGameList: MutableList<GameModel> = emptyList<GameModel>().toMutableList()

    init {
        pcGameList.add(GameModel("【SCP】器关的彷徨", "PC", "steam",
                "https://store.steampowered.com/app/836940/SCP_The_will_of_a_single_TaleDEMOver/", "Free"))
        pcGameList.add(GameModel("SCP：秘密实验室", "PC", "steam",
                "https://store.steampowered.com/app/700330/SCP_Secret_Laboratory", "Free"))
        pcGameList.add(GameModel("Daughter of Shadows: An SCP Breach Event", "PC", "steam",
                "https://store.steampowered.com/app/449820/", "Free"))
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val context = container.context
        if (position == 0) {
            val rvPcGame = RecyclerView(container.context)
            val lm = LinearLayoutManager(container.context, VERTICAL, false)
            rvPcGame.layoutManager = lm
            rvPcGame.adapter = GameListAdapter(context, pcGameList)
            return rvPcGame
        } else {
            val rvMobileGame = RecyclerView(container.context)
            val lm = LinearLayoutManager(container.context, VERTICAL, false)
            rvMobileGame.layoutManager = lm
            rvMobileGame.adapter = GameListAdapter(context, mobileGameList)
            return rvMobileGame
        }
    }

    override fun isViewFromObject(p0: View, p1: Any): Boolean {
        return p0 == p1
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeViewAt(position)
    }

    override fun getCount(): Int {
        return 2
    }
}