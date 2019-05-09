package info.free.scp.view.download

import androidx.lifecycle.MutableLiveData
import info.free.scp.bean.DownloadModel
import info.free.scp.util.PreferenceUtil

class DownloadRepository {
    var downloadList = MutableLiveData<ArrayList<DownloadModel>>()

    fun loadDownloadList() {
        // TODO 初始化离线列表
        var innerList = downloadList.value
        if (innerList == null) {
            innerList = ArrayList()
        }
        if (innerList.isEmpty()) {
            innerList.add(DownloadModel("All", "", -1,0))
            innerList.add(DownloadModel("Only Scp Series", "", 0,0))
            innerList.add(DownloadModel("Only Scp Series CN", "", 1,0))
            innerList.add(DownloadModel("Only Scp Tales", "", 2,0))
            innerList.add(DownloadModel("Other Scps", "", 3,0))
            innerList.add(DownloadModel("Settings and more", "", 4,0))
        }

        // 从preference里面同步数据
        innerList.forEach {
            it.lastDownloadTime = PreferenceUtil.getDetailLastLoadTime(it.type)
            if (PreferenceUtil.getDetailDataLoadFinish(it.type)) {
                it.status = 1 // 0 未下载 1 下载完成
            }
        }
        downloadList.value = innerList
    }
}