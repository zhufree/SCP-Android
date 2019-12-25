package info.free.scp.view.portal

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import info.free.scp.R
import info.free.scp.bean.PortalModel
import info.free.scp.databinding.ActivityPortalBinding
import info.free.scp.view.base.BaseActivity

class PortalActivity : BaseActivity() {
    private val model by lazy {
        ViewModelProvider(this)
                .get(PortalListViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bd: ActivityPortalBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_portal)

        baseToolbar = bd.portalToolbar

        val adapter = PortalAdapter()
        bd.rlPortalList.adapter = adapter
        val lm = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        bd.rlPortalList.layoutManager = lm

        model.getPortalList()
                ?.observe(this, Observer<List<PortalModel>> { portals ->
                    // update UI
                    if (portals.isNotEmpty()) {
                        bd.tvEmptyPortal.visibility = View.GONE
                        adapter.submitList(portals)
                    } else {
                        bd.tvEmptyPortal.visibility = View.VISIBLE
                        bd.tvEmptyPortal.text = "加载失败"
                    }
                })
        model.loadPortalModels()
    }
}
