package info.free.scp

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import info.free.scp.SCPConstants.SERIES
import info.free.scp.view.category.CategoryActivity
import info.free.scp.view.feed.FeedFragment
import info.free.scp.view.home.HomeFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), HomeFragment.CategoryListener {
    private val homeFragment = HomeFragment.newInstance()
    private val feedFragment = FeedFragment.newInstance()
    private val aboutFragment = AboutFragment.newInstance()

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val transaction = fragmentManager.beginTransaction()
        when (item.itemId) {
            R.id.navigation_home -> {
                if (homeFragment.isAdded) {
                    transaction.show(homeFragment)
                } else {
                    transaction.add(R.id.flMainContainer, homeFragment)
                }
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_feed -> {
                if (feedFragment.isAdded) {
                    transaction.show(feedFragment)
                } else {
                    transaction.add(R.id.flMainContainer, feedFragment)
                }
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_about -> {
                if (aboutFragment.isAdded) {
                    transaction.show(aboutFragment)
                } else {
                    transaction.add(R.id.flMainContainer, aboutFragment)
                }
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val transaction = fragmentManager.beginTransaction()
        transaction.add(R.id.flMainContainer, homeFragment)
        transaction.commitAllowingStateLoss()
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    override fun onCategoryClick(type: Int) {
        val intent = Intent()
        intent.putExtra("type", type)
        intent.setClass(this, CategoryActivity::class.java)
        startActivity(intent)
    }
}
