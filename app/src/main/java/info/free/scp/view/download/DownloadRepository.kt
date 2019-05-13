package info.free.scp.view.download

import androidx.lifecycle.MutableLiveData
import info.free.scp.bean.DownloadModel
import info.free.scp.util.DownloadUtil.Status.FINISH
import info.free.scp.util.DownloadUtil.Status.NONE
import info.free.scp.util.PreferenceUtil

class DownloadRepository {
    var downloadList = MutableLiveData<ArrayList<DownloadModel>>()

    fun loadDownloadList() {
        var innerList = downloadList.value
        if (innerList == null) {
            innerList = ArrayList()
        }
        if (innerList.isEmpty()) {
            innerList.add(DownloadModel("All", "", -1,NONE))
            innerList.add(DownloadModel("Only Scp Series", "", 0,NONE))
            innerList.add(DownloadModel("Only Scp Series CN", "", 1,NONE))
            innerList.add(DownloadModel("Only Scp Tales", "", 2,NONE))
            innerList.add(DownloadModel("Other Scps", "", 3,NONE))
            innerList.add(DownloadModel("Settings and more", "", 4,NONE))
        }

        // 从preference里面同步数据
        innerList.forEach {
            it.lastDownloadTime = PreferenceUtil.getDetailLastLoadTime(it.dbIndex)
            if (PreferenceUtil.getDetailDataLoadFinish(it.dbIndex)) {
                it.status = FINISH // 0 未下载 1 下载完成
            }
        }
        downloadList.value = innerList
    }
}