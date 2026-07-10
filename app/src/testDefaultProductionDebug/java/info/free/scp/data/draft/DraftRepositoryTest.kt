package info.free.scp.data.draft

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import info.free.scp.bean.DraftModel
import org.junit.Assert.assertEquals
import org.junit.Test

class DraftRepositoryTest {
    @Test
    fun createsNewDraftFromDefaultContent() {
        val source = FakeDraftDataSource().apply {
            draftCountValue = 2
        }
        val repository = DraftRepository(source) { 1234L }

        val draft = repository.loadDraft(-1, "default title", "default content")

        assertEquals(DraftModel(3, 1234L, "default title", "default content"), draft)
    }

    @Test
    fun loadsExistingDraftWhenIdIsPositive() {
        val expected = DraftModel(7, 100L, "existing", "body")
        val source = FakeDraftDataSource().apply {
            drafts[7] = expected
        }
        val repository = DraftRepository(source) { 1234L }

        val draft = repository.loadDraft(7, "ignored", "ignored")

        assertEquals(expected, draft)
    }

    @Test
    fun saveDraftUpdatesFieldsAndTimestamp() {
        val source = FakeDraftDataSource()
        val repository = DraftRepository(source) { 5678L }
        val draft = DraftModel(4, 100L, "old", "old body")

        val saved = repository.saveDraft(draft, "new", "new body")

        assertEquals(DraftModel(4, 5678L, "new", "new body"), saved)
        assertEquals(saved, source.savedDrafts.single())
    }

    @Test
    fun deleteDraftDelegatesToDataSource() {
        val source = FakeDraftDataSource()
        val repository = DraftRepository(source)

        repository.deleteDraft(9)

        assertEquals(listOf(9), source.deletedDraftIds)
    }
}

private class FakeDraftDataSource : DraftDataSource {
    val drafts = mutableMapOf<Int, DraftModel>()
    var draftCountValue = 0
    val savedDrafts = mutableListOf<DraftModel>()
    val deletedDraftIds = mutableListOf<Int>()

    override fun getDraftList(): LiveData<List<DraftModel>> {
        return MutableLiveData(drafts.values.toList())
    }

    override fun getDraft(draftId: Int): DraftModel {
        return drafts.getValue(draftId)
    }

    override fun getDraftCount(): Int {
        return draftCountValue
    }

    override fun saveDraft(draft: DraftModel) {
        savedDrafts.add(draft.copy())
        drafts[draft.draftId] = draft.copy()
    }

    override fun deleteDraft(draftId: Int) {
        deletedDraftIds.add(draftId)
        drafts.remove(draftId)
    }
}
