package info.free.scp.data.draft

import androidx.lifecycle.LiveData
import info.free.scp.bean.DraftModel
import info.free.scp.db.AppInfoDatabase

interface DraftDataSource {
    fun getDraftList(): LiveData<List<DraftModel>>
    fun getDraft(draftId: Int): DraftModel
    fun insertDraft(draft: DraftModel): Int
    fun updateDraft(draft: DraftModel)
    fun deleteDraft(draftId: Int)
}

class RoomDraftDataSource : DraftDataSource {
    private val draftDao get() = AppInfoDatabase.getInstance().draftDao()

    override fun getDraftList(): LiveData<List<DraftModel>> {
        return draftDao.getAllDraft()
    }

    override fun getDraft(draftId: Int): DraftModel {
        return draftDao.getDraft(draftId)
    }

    override fun insertDraft(draft: DraftModel): Int {
        val id = draftDao.insert(draft.lastModifyTime, draft.title, draft.content)
        check(id in 1..Int.MAX_VALUE.toLong()) { "无法分配草稿 ID" }
        return id.toInt()
    }

    override fun updateDraft(draft: DraftModel) {
        check(draftDao.update(draft) == 1) { "该草稿已被删除" }
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
                draftId = 0,
                lastModifyTime = nowProvider(),
                title = defaultTitle,
                content = defaultContent
            )
        }
    }

    fun saveDraft(draft: DraftModel, title: String, content: String): DraftModel {
        val saved = draft.copy(title = title, content = content, lastModifyTime = nowProvider())
        if (saved.draftId == 0) {
            saved.draftId = dataSource.insertDraft(saved)
        } else {
            dataSource.updateDraft(saved)
        }
        return saved
    }

    fun deleteDraft(draftId: Int) {
        dataSource.deleteDraft(draftId)
    }
}
