package info.free.scp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import info.free.scp.bean.DraftModel

/**
 * 阅读历史和稍后阅读信息
 */
@Dao
interface DraftDao {
    @Insert(onConflict = REPLACE)
    fun save(info: DraftModel)

    @Query("SELECT * FROM draft ORDER BY lastModifyTime DESC;")
    fun getAllDraft(): LiveData<List<DraftModel>>

    @Query("SELECT * FROM draft WHERE draftId = :draftId;")
    fun getDraft(draftId: Int): DraftModel

    @Query("SELECT count(*) FROM draft;")
    fun getDraftCount(): Int

    @Query("DELETE FROM draft WHERE draftId = :draftId")
    fun delete(draftId: Int)

    @Query("DELETE FROM draft")
    fun clearHistory()
    // AND last_update >= :timeout
}

