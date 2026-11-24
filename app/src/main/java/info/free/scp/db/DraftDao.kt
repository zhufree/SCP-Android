package info.free.scp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Update
import info.free.scp.data.draft.INSERT_DRAFT_SQL
import androidx.room.Query
import info.free.scp.bean.DraftModel

/**
 * 阅读历史和稍后阅读信息
 */
@Dao
interface DraftDao {
    @Query(INSERT_DRAFT_SQL)
    fun insert(lastModifyTime: Long, title: String, content: String): Long

    @Update
    fun update(info: DraftModel): Int

    @Query("SELECT * FROM draft ORDER BY lastModifyTime DESC;")
    fun getAllDraft(): LiveData<List<DraftModel>>

    @Query("SELECT * FROM draft WHERE draftId = :draftId;")
    fun getDraft(draftId: Int): DraftModel

    @Query("DELETE FROM draft WHERE draftId = :draftId")
    fun delete(draftId: Int)

    @Query("DELETE FROM draft")
    fun clearHistory()
    // AND last_update >= :timeout
}

