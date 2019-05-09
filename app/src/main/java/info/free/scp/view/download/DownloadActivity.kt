package info.free.scp.view.download

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.view.View
import info.free.scp.R
import info.free.scp.bean.DownloadModel
import info.free.scp.databinding.ActivityDownloadBinding
import info.free.scp.util.InjectorUtils
import info.free.scp.view.base.BaseActivity


class DownloadActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bd: ActivityDownloadBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_download)



        setSupportActionBar(bd.downloadToolbar)
        bd.downloadToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        bd.downloadToolbar.setNavigationOnClickListener { finish() }

        val adapter = DownloadAdapter()
        bd.rlDownloadList.adapter = adapter
        val factory = InjectorUtils.provideDownloadListViewModelFactory()
        val model = ViewModelProviders.of(this, factory)
                .get(DownloadListViewModel::class.java)


        model.getDownloadList()
                ?.observe(this, Observer<List<DownloadModel>> { downloads ->
                    // update UI
                    if (downloads.isNotEmpty()) {
                        adapter.submitList(downloads)
                    }
                })
    }
}
