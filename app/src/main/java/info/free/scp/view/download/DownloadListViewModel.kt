package info.free.scp.view.download

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import info.free.scp.bean.DownloadModel

class DownloadListViewModel(private val listRepo: DownloadRepository) : ViewModel() {

    private var listInit = false

    fun getDownloadList(): LiveData<ArrayList<DownloadModel>>? {
        if (!listInit) {
            loadDownloads()
            listInit = true
        }
        return listRepo.downloadList
    }

    private fun loadDownloads() {
        listRepo.loadDownloadList()
    }
}
