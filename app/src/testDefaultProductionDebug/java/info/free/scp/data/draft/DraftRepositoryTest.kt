package info.free.scp.data.draft

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import info.free.scp.bean.DraftModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test

class DraftRepositoryTest {
    @Test
    fun createsNewDraftFromDefaultContent() {
        val source = FakeDraftDataSource()
        val repository = DraftRepository(source) { 1234L }

        val draft = repository.loadDraft(-1, "default title", "default content")

        assertEquals(DraftModel(0, 1234L, "default title", "default content"), draft)
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

        source.drafts[4] = draft
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

    @Test
    fun savesNewDraftOnceThenEditsTheAssignedId() {
        val source = FakeDraftDataSource()
        val repository = DraftRepository(source)
        val list = repository.draftList
        val opened = repository.loadDraft(0, "first", "text")
        val saved = repository.saveDraft(opened, "first", "text")
        repository.saveDraft(saved, "edited", "text")

        assertEquals(1, saved.draftId)
        assertEquals("edited", source.drafts.getValue(saved.draftId).title)
        assertEquals(listOf(0, 1), source.savedDrafts.map { it.draftId })
        assertSame(list, repository.draftList)
        assertEquals(1, source.listRequests)
    }

    @Test
    fun editingADeletedDraftDoesNotReinsertIt() {
        val source = FakeDraftDataSource()
        val repository = DraftRepository(source)
        assertThrows(IllegalStateException::class.java) {
            repository.saveDraft(DraftModel(4, 100L), "title", "text")
        }
        assertEquals(emptyMap<Int, DraftModel>(), source.drafts)
    }

}

private class FakeDraftDataSource : DraftDataSource {
    val drafts = mutableMapOf<Int, DraftModel>()
    var listRequests = 0
    val savedDrafts = mutableListOf<DraftModel>()
    val deletedDraftIds = mutableListOf<Int>()

    override fun getDraftList(): LiveData<List<DraftModel>> {
        listRequests++
        return MutableLiveData(drafts.values.toList())
    }

    override fun getDraft(draftId: Int): DraftModel {
        return drafts.getValue(draftId)
    }

    override fun insertDraft(draft: DraftModel): Int {
        check(draft.draftId == 0)
        val id = (drafts.keys.maxOrNull() ?: 0) + 1
        savedDrafts.add(draft.copy())
        drafts[id] = draft.copy(draftId = id)
        return id
    }

    override fun updateDraft(draft: DraftModel) {
        check(draft.draftId in drafts)
        savedDrafts.add(draft.copy())
        drafts[draft.draftId] = draft.copy()
    }

    override fun deleteDraft(draftId: Int) {
        deletedDraftIds.add(draftId)
        drafts.remove(draftId)
    }
}
