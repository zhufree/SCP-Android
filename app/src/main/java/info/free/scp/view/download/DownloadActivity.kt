package info.free.scp.view.download

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import info.free.scp.bean.DownloadModel
import info.free.scp.databinding.ActivityDownloadBinding
import info.free.scp.util.InjectorUtils
import info.free.scp.view.base.BaseActivity
import pub.devrel.easypermissions.EasyPermissions
import info.free.scp.R
import android.Manifest.permission
import pub.devrel.easypermissions.AfterPermissionGranted
import info.free.scp.SCPConstants.RequestCode.REQUEST_FILE_PERMISSION


class DownloadActivity : BaseActivity(), EasyPermissions.PermissionCallbacks {

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
        requireFilePermission()
    }

    @AfterPermissionGranted(REQUEST_FILE_PERMISSION)
    private fun requireFilePermission() {
        val perms = arrayOf(permission.WRITE_EXTERNAL_STORAGE)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            // Already have permission, do the thing
            // ...
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.request_permission_notice),
                    REQUEST_FILE_PERMISSION, *perms)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }
}
