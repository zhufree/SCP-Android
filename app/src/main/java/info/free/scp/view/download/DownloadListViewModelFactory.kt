package info.free.scp.view.download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


/**
 *
 * Created by zhufree on 2019/5/9.
 */
class DownloadListViewModelFactory(
        private val repository: DownloadRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = DownloadListViewModel(repository) as T
}


