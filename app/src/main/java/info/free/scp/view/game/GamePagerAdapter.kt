package info.free.scp.view.game

import android.content.Context
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

class GamePagerAdapter(val mContext: Context) : PagerAdapter() {
    private var gameTypeList: MutableList<RecyclerView> = emptyList<RecyclerView>().toMutableList()
    private var pcGameList: MutableList<GameModel> = emptyList<GameModel>().toMutableList()
    private var mobileGameList: MutableList<GameModel> = emptyList<GameModel>().toMutableList()
    private var pcGameAdapter: GameListAdapter? = null
    private var mobileGameAdapter: GameListAdapter? = null

    init {
        pcGameList.add(GameModel("【SCP】器关的彷徨", "-“- 你现在相信现实吗？”\n当 SCP 基金会移动时，" +
                "您将成为 {MA 13 号计划} 的见证人", "PC", "steam",
                "https://store.steampowered.com/app/836940/SCP_The_will_of_a_single_TaleDEMOver/",
                "https://static.indienova.com/ranch/gamedb/2018/10/cover/1540086818cXDY.jpg","Free"))
        pcGameList.add(GameModel("SCP：秘密实验室", "在 SCP 基金会的收容失效事件中，" +
                "部分“异常”跨过了保全措施并且逃离了收容室，它们并不怀好意。成为该站点的一员：一个负责重新收容的特工，" +
                "抑或是一个异常事物（SCP）。来决定这起事件的走向和结果。","PC", "steam",
                "https://store.steampowered.com/app/700330/SCP_Secret_Laboratory",
                "https://images.igdb.com/igdb/image/upload/t_720p/illclvtarenkeql8yszj.jpg","Free"))
        pcGameList.add(GameModel("Daughter of Shadows: An SCP Breach Event", "The player " +
                "assumes the role of a dangerous and powerful girl known as SCP-029. After a facility " +
                "wide power outage occurs, numerous SCPs breach containment and proceed to wreak havoc " +
                "throughout the facility. SCP-029 must use her mind-controlling powers to survive and " +
                "find a way to escape from the facility.","PC", "steam",
                "https://store.steampowered.com/app/449820/",
                "https://cdn.steamstatic.com.8686c.com/steam/apps/449820/ss_0d5fc97c254c5c4" +
                        "4c33e41264bdb7069a45efaca.1920x1080.jpg","6¥"))
        pcGameList.add(GameModel("SCP-087: Recovered document", "SCP-087: Recovered documents " +
                "- first-person horror, that tells the story of a classified object.","PC", "steam",
                "https://store.steampowered.com/app/765180/", "https://images.igdb.com" +
                "/igdb/image/upload/t_720p/vnmpzozwhg0yimdpq4g4.jpg","22¥"))
        pcGameList.add(GameModel("SCP - Containment Breach", "SCP - Containment Breach " +
                "is a free survival horror game written in Blitz3D.","PC", "none",
                "http://www.scpcbgame.com/", "https://static.indienova.com/ranch/gamedb" +
                "/2017/07/cover/bjs6waswr6tlkndfan0a.jpg","Free"))
        pcGameList.add(GameModel("SCP-087-B", "","PC", "直接下载",
                "http://www.scpcbgame.com/scp-087-b.html", "http://www.scpcbgame.com/images/087header.jpg","Free"))

        mobileGameList.add(GameModel("SCP - Containment Breach", "","mobile", "Google Play",
                "http://www.scpcbgame.com/scp-087-b.html", "https://lh3.googleusercontent.com" +
                "/ZDHMLbk_mHiyNvFsX_vWHJp519v4YiIp8CdgXDdoXdm6zZk1wsEOWC2FpN8K-VEXOeI=s180-rw","Free"))
        mobileGameList.add(GameModel("SCP: Breach 2D", "检查你是否能活下来，并可能逃离危险的SCP综合体！ " +
                "目前游戏有：SCP-173|SCP-106|SCP-966" +
                "|SCP-714|SCP-096。","mobile", "Google Play",
                "http://www.scpcbgame.com/scp-087-b.html", "https://lh3.googleusercontent" +
                ".com/rF1MS9_ewKpbxB1eKaakqu4j1FtLmoVC-OAbdvq0kSbAskC4w_lr-XWfhSESAKURxWI=s180-rw","Free"))
        mobileGameList.add(GameModel("SCP-087-B", "","mobile", "Google Play",
                "http://www.scpcbgame.com/scp-087-b.html", "https://lh3.googleusercontent.com" +
                "/B4LtOgf6wC7ivPa0Vuwa6GURIKvLQmo0VVMjERCC8oUsN4TfuEkXh7Dl9Cmy47kKt8M=s180-rw","Free"))
        mobileGameList.add(GameModel("The Lost Signal: SCP", "","mobile", "Google Play",
                "http://www.scpcbgame.com/scp-087-b.html", "https://lh3.googleusercontent.com/" +
                "RuNO2F7xkCFUE_f8bysegU8jULYqonUIEhI_bjqSwe1gD8kgnHVYu3j7tZoHeBpojw=s180-rw","Free"))
        mobileGameList.add(GameModel("SCP: Run", "","mobile", "Google Play",
                "http://www.scpcbgame.com/scp-087-b.html", "https://lh3.googleusercontent." +
                "com/B-BGDGvDbBRRw4Gs0zIa9WAWkQLfu90QCSVZLj8eSphJkGqHbNe6mgHlD5dAFLYye5M=s180-rw","Free"))
        mobileGameList.add(GameModel("SCP: Site-19", "","mobile", "Google Play",
                "http://www.scpcbgame.com/scp-087-b.html", "https://lh3.googleusercontent." +
                "com/YFEpmZ2qmybq7heR655CVq7BQ47aFDUhgDHTLmSWNGEeZdN1Az33ehbj8FCKx_Q5xQ=s180-rw","Free"))
        mobileGameList.add(GameModel("SCP-087-Remake Horror Quest", "","mobile", "Google Play",
                "http://www.scpcbgame.com/scp-087-b.html", "https://lh3.googleusercontent.com" +
                "/vlD5h7UAfzCcXRa5dv1hOglFAXUtE9SUki6-2UqCKojhsKTcHgQfdz62u2Vwj_sXZRAB=s180-rw","Free"))


        pcGameAdapter = GameListAdapter(mContext, pcGameList)
        mobileGameAdapter = GameListAdapter(mContext, mobileGameList)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        if (position == 0) {
            val rvPcGame = RecyclerView(mContext)
            val lm = LinearLayoutManager(mContext, VERTICAL, false)
            rvPcGame.layoutManager = lm
            rvPcGame.adapter = pcGameAdapter
            container.addView(rvPcGame)
            return rvPcGame
        } else {
            val rvMobileGame = RecyclerView(mContext)
            val lm = LinearLayoutManager(mContext, VERTICAL, false)
            rvMobileGame.layoutManager = lm
            rvMobileGame.adapter = mobileGameAdapter
            container.addView(rvMobileGame)
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

    override fun getPageTitle(position: Int): CharSequence? {
        return if (position == 0) "PC" else "Mobile"
    }
}