package info.free.scp.view.download

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import info.free.scp.R

class DownloadNoticeHeader : ConstraintLayout {
    constructor(context: Context): super(context) {
        initView(context)
    }
    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet){
        initView(context)
    }

    private fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.layout_download_header, this)
    }
}