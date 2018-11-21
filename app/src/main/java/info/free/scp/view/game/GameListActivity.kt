package info.free.scp.view.game

import android.os.Bundle
import info.free.scp.R
import info.free.scp.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_game_list.*

class GameListActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_list)

        setSupportActionBar(game_list_toolbar)
        game_list_toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        game_list_toolbar.setNavigationOnClickListener { finish() }
        vpGame?.adapter = GamePagerAdapter(this)
        tabGame?.setupWithViewPager(vpGame)
    }
}
