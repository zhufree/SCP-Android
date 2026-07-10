package info.free.scp.data.draft

import androidx.lifecycle.LiveData
import info.free.scp.bean.DraftModel
import info.free.scp.db.AppInfoDatabase

interface DraftDataSource {
    fun getDraftList(): LiveData<List<DraftModel>>
    fun getDraft(draftId: Int): DraftModel
    fun getDraftCount(): Int
    fun saveDraft(draft: DraftModel)
    fun deleteDraft(draftId: Int)
}

class RoomDraftDataSource : DraftDataSource {
    private val draftDao by lazy { AppInfoDatabase.getInstance().draftDao() }

    override fun getDraftList(): LiveData<List<DraftModel>> {
        return draftDao.getAllDraft()
    }

    override fun getDraft(draftId: Int): DraftModel {
        return draftDao.getDraft(draftId)
    }

    override fun getDraftCount(): Int {
        return draftDao.getDraftCount()
    }

    override fun saveDraft(draft: DraftModel) {
        draftDao.save(draft)
    }

    override fun deleteDraft(draftId: Int) {
        draftDao.delete(draftId)
    }
}

class DraftRepository(
    private val dataSource: DraftDataSource = RoomDraftDataSource(),
    private val nowProvider: () -> Long = { System.currentTimeMillis() }
) {
    val draftList: LiveData<List<DraftModel>> = dataSource.getDraftList()

    fun loadDraft(draftId: Int, defaultTitle: String, defaultContent: String): DraftModel {
        return if (draftId > 0) {
            dataSource.getDraft(draftId)
        } else {
            DraftModel(
                draftId = dataSource.getDraftCount() + 1,
                lastModifyTime = nowProvider(),
                title = defaultTitle,
                content = defaultContent
            )
        }
    }

    fun saveDraft(draft: DraftModel, title: String, content: String): DraftModel {
        draft.title = title
        draft.content = content
        draft.lastModifyTime = nowProvider()
        dataSource.saveDraft(draft)
        return draft
    }

    fun deleteDraft(draftId: Int) {
        dataSource.deleteDraft(draftId)
    }
}
