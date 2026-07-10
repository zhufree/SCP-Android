package info.free.scp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import info.free.scp.bean.DraftModel
import info.free.scp.data.draft.DraftRepository

class DraftListViewModel : ViewModel() {
    private val repo = DraftRepository()

    fun getDraft(): LiveData<List<DraftModel>> {
        return repo.draftList
    }

    fun deleteDraft(draftId: Int) {
        repo.deleteDraft(draftId)
    }
}
