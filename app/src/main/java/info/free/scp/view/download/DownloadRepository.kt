package info.free.scp.view.download

import androidx.lifecycle.MutableLiveData
import info.free.scp.bean.DownloadModel
import info.free.scp.util.DownloadUtil.Status.FINISH
import info.free.scp.util.DownloadUtil.Status.NONE
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.Utils
import okhttp3.internal.Util
import java.util.*
import kotlin.collections.ArrayList

class DownloadRepository {
    var downloadList = MutableLiveData<ArrayList<DownloadModel>>()

    fun loadDownloadList() {
        var innerList = downloadList.value
        if (innerList == null) {
            innerList = ArrayList()
        }
        if (innerList.isEmpty()) {
            innerList.add(DownloadModel("全部数据", "", "", -1,NONE))
            innerList.add(DownloadModel("仅SCP系列", "", "", 0,NONE))
            innerList.add(DownloadModel("仅SCP-CN系列", "", "", 1,NONE))
            innerList.add(DownloadModel("仅故事外围", "", "", 2,NONE))
            innerList.add(DownloadModel("其他SCP", "", "", 3,NONE))
            innerList.add(DownloadModel("设定中心，竞赛等", "", "", 4,NONE))
        }

        // 从preference里面同步数据
        innerList.forEach {
            it.lastDownloadTime = Utils.formatDate(PreferenceUtil.getDetailLastLoadTime(it.dbIndex))
            it.lastUpdateTime = Utils.formatDate(PreferenceUtil.getServerLastUpdateTime(it.dbIndex))
            if (PreferenceUtil.getDetailDataLoadFinish(it.dbIndex)) {
                it.status = FINISH // 0 未下载 1 下载完成
            }
        }
        downloadList.value = innerList
    }
}