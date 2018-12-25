package info.free.scp.view.home

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import info.free.scp.R
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_direct.*


class DirectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_direct)
        val numberList = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9") //准备字符串数组or别的什么
        val numberAdapter = ArrayAdapter(this, R.layout.item_direct_number, R.id.tv_direct_number, numberList)
        gv_direct_number?.adapter = numberAdapter
    }
}
