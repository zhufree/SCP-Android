package info.free.scp

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import info.free.scp.SCPConstants.CATEGORY_SCP_SERIES
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), HomeFragment.CategoryListener {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val transaction = fragmentManager.beginTransaction()
        transaction.add(R.id.flMainContainer, HomeFragment.newInstance())
        transaction.commitAllowingStateLoss()
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    override fun onCategoryClick(type: Int) {
        when (type) {
            CATEGORY_SCP_SERIES -> {
                val intent = Intent()
                intent.setClass(this, CategoryActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
