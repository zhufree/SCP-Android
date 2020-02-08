package info.free.scp.view.game

import android.content.Context
import androidx.viewpager.widget.PagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import android.view.View
import android.view.ViewGroup
import info.free.scp.bean.GameModel

/**
 * Created by zhufree on 2018/11/21.
 *
 */

class GamePagerAdapter(val mContext: Context) : androidx.viewpager.widget.PagerAdapter() {
    private var gameTypeList: MutableList<RecyclerView> = emptyList<RecyclerView>().toMutableList()
    private var pcGameList: MutableList<GameModel> = emptyList<GameModel>().toMutableList()
    private var mobileGameList: MutableList<GameModel> = emptyList<GameModel>().toMutableList()
    private var pcGameAdapter: GameListAdapter? = null
    private var mobileGameAdapter: GameListAdapter? = null

    init {
        pcGameList.add(GameModel("【SCP】器关的彷徨", "——“你相信眼前的现实吗？”当SCP基金会行动之时，" +
                "您将成为『MA13号计划』的见证人。", "PC", "steam",
                "https://store.steampowered.com/app/836940/SCP_The_will_of_a_single_TaleDEMOver/",
                "https://media.st.dl.bscstorage.net/steam/apps/836940/header.jpg?t=1535215559","Free"))
        pcGameList.add(GameModel("SCP：秘密实验室", "在 SCP 基金会的收容失效事件中，" +
                "部分“异常”跨过了保全措施并且逃离了收容室，它们并不怀好意。成为该站点的一员：一个负责重新收容的特工，" +
                "抑或是一个异常事物（SCP）。来决定这起事件的走向和结果。","PC", "steam",
                "https://store.steampowered.com/app/700330/SCP_Secret_Laboratory",
                "https://images.igdb.com/igdb/image/upload/t_720p/illclvtarenkeql8yszj.jpg","Free"))
        pcGameList.add(GameModel("Daughter of Shadows: An SCP Breach Event", "玩家扮演一个被" +
                "称为SCP-029的危险而强大的女孩。在设施大范围停电后，许多SCP突破收容并继续在整个设施中造成严重破坏。 " +
                "SCP-029必须利用她的精神控制力量生存并找到逃离设施的方法。","PC", "steam",
                "https://store.steampowered.com/app/449820/",
                "https://cdn.steamstatic.com.8686c.com/steam/apps/449820/ss_0d5fc97c254c5c4" +
                        "4c33e41264bdb7069a45efaca.1920x1080.jpg","6¥"))
        pcGameList.add(GameModel("SCP-087: Recovered document", "SCP-087: Recovered documents " +
                "第一人称恐怖游戏，讲述了一个机密项目的故事。","PC", "steam",
                "https://store.steampowered.com/app/765180/", "https://images.igdb.com" +
                "/igdb/image/upload/t_720p/vnmpzozwhg0yimdpq4g4.jpg","22¥"))
        pcGameList.add(GameModel("SCP - Containment Breach", "SCP - Containment Breach " +
                "是一个用Blitz3D编写的免费生存恐怖游戏","PC", "none",
                "http://www.scpcbgame.com/", "http://www.scpcbgame.com/images/header3.png","Free"))
        pcGameList.add(GameModel("SCP-087-B", "SCP-087-B是一个基于SCP-087的小型实验性恐怖游戏。" +
                "你会发现自己身处一组随机生成的黑暗走廊和楼梯，下面隐藏着一些东西，你唯一的路是深入到黑暗中。" +
                "\n你可以走多远？","PC", "直接下载",
                "http://www.scpcbgame.com/scp-087-b.html", "http://www.scpcbgame.com/images/087header.jpg","Free"))

        mobileGameList.add(GameModel("SCP - Containment Breach", "使用Unity制作的SCP - " +
                "Containment Breach手机版","mobile", "Google Play",
                "https://pan.baidu.com/s/1WrCWgMYnhZZUfAlpkv-P6A", "http://bmob-cdn-224" +
                "26.b0.upaiyun.com/2018/11/25/3e57c00e40a1f882801b860e52296a63.webp","Free"))
        mobileGameList.add(GameModel("SCP: Breach 2D", "检查你是否能活下来，并可能逃离危险的SCP综合体！ " +
                "目前游戏有：SCP-173|SCP-106|SCP-966|SCP-714|SCP-096。","mobile", "Google Play",
                "https://pan.baidu.com/s/1UlsTOSrKJkVcrwgM92T_BA", "http://bmob-cdn-22426." +
                "b0.upaiyun.com/2018/11/25/421d41ec406a0ebc8048714322a28cbe.webp","Free"))
        mobileGameList.add(GameModel("SCP-087-B", "SCP-087-B手机版","mobile", "Google Play",
                "https://pan.baidu.com/s/1Q57d1IrwPSuwyczsct00eQ", "http://bmob-cdn-22426." +
                "b0.upaiyun.com/2018/11/25/6ddf383c401e18dc8013b331e1c125b2.webp","Free"))
        mobileGameList.add(GameModel("The Lost Signal: SCP", "在这个3D游戏中调查，其中有很大的作用，并精心制作的3D图形超自然的活动！\n" +
                "有趣的任务和爬行的怪物等待着你！新的工件或在每次更新超自然事件！","mobile", "Google Play",
                "https://pan.baidu.com/s/1Rfy-k4GZd43iopI4jiCMTQ", "http://bmob-cdn-22426." +
                "b0.upaiyun.com/2018/11/25/cc17e819409dfff980e222ed96c456ce.webp","Free"))
        mobileGameList.add(GameModel("SCP: Run", "逃出工厂！逃离SCP-173和SCP-106和SCP-049和" +
                "SCP-1048和SCP-1048-A。","mobile", "Google Play",
                "https://pan.baidu.com/s/1Hz9UVVt1svPYp6VN_8w_EA", "http://bmob-cdn-22426." +
                "b0.upaiyun.com/2018/11/25/3c13987640e7d98980d3ee39470cc231.webp","Free"))
        mobileGameList.add(GameModel("SCP: Site-19", "欢迎来到Site-19。\n" +
                "Site-19是目前运营中最大的基础设施，包含数百个安全和欧几里德级异常。" +
                "此设施中包含的物体（某些SCP尚未在游戏中）包括：SCP-055，SCP-131，SCP-173，SCP-387，SCP-668，SCP-931。",
                "mobile", "Google Play",
                "https://pan.baidu.com/s/1gsPop38_dZ0ryisC7sx82Q", "http://bmob-cdn-22426." +
                "b0.upaiyun.com/2018/11/25/39a74ddd40b4989180654e9c5708c952.webp","Free"))
        mobileGameList.add(GameModel("SCP-087-Remake Horror Quest", "作为测试对象（d类员工），" +
                "你派人去调查与SCP-087的编号的对象。游戏一开始用深色的楼梯，路径沿着伴随着可怕的声音。很多困难的解决方案，" +
                "有趣的谜题会遇到自己的方式，以及怪物渴望抓住你。","mobile", "Google Play",
                "https://pan.baidu.com/s/1wqJ3QlDhhydyIkH_uvW7wQ", "http://bmob-cdn-22426." +
                "b0.upaiyun.com/2018/11/25/70735ce7401e74678079647082bfabfd.webp","Free"))


        pcGameAdapter = GameListAdapter(mContext, pcGameList, 0)
        mobileGameAdapter = GameListAdapter(mContext, mobileGameList, 1)
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
            val lm = StaggeredGridLayoutManager(2, VERTICAL)
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