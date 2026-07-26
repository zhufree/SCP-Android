package info.free.scp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import info.free.scp.bean.DraftModel
import info.free.scp.data.draft.DraftRepository

data class DraftListUiState(
    val drafts: List<DraftModel> = emptyList()
)

class DraftListViewModel : ViewModel() {
    private val repo = DraftRepository()

    val uiState: LiveData<DraftListUiState> = repo.draftList.map(::DraftListUiState)

    fun deleteDraft(draftId: Int) {
        repo.deleteDraft(draftId)
    }
}
