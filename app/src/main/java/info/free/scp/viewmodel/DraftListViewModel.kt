package info.free.scp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import info.free.scp.bean.DraftModel

class DraftListViewModel : ViewModel() {
    var repo = DraftRepository()

    fun getDraft(): LiveData<List<DraftModel>>? {
        return repo.draftList
    }
}