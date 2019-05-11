package info.free.scp.view.download

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.lzy.okgo.OkGo
import com.lzy.okgo.request.GetRequest
import com.lzy.okserver.OkDownload
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.util.PreferenceUtil
import kotlinx.android.synthetic.main.layout_download_header.view.*
import java.io.File

class DownloadNoticeHeader : ConstraintLayout {
    constructor(context: Context): super(context) {
        initView(context)
    }
    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet){
        initView(context)
    }

    private fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.layout_download_header, this)
        sw_auto_download.isChecked = PreferenceUtil.getAutoDownload()
        sw_auto_download.setOnCheckedChangeListener { _, isChecked ->
            PreferenceUtil.setAutoDownload(isChecked)
            if (isChecked && !PreferenceUtil.getDetailDataLoadFinish(-1)) {
                // 没有离线主数据库

            }
        }
    }

}