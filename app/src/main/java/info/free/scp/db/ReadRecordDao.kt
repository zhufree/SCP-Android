package info.free.scp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import info.free.scp.bean.ScpRecordModel

/**
 * 阅读历史和稍后阅读信息
 */
@Dao
interface ReadRecordDao {
    @Insert(onConflict = REPLACE)
    fun save(info: ScpRecordModel)

    @Query("SELECT * FROM LaterAndHistoryTable WHERE link = :link LIMIT 1")
    fun getInfoByLink(link: String): ScpRecordModel?

    @Query("SELECT * FROM LaterAndHistoryTable WHERE viewListType = :viewType ORDER BY viewTime ASC")
    fun getInfoByLinkAsc(viewType: Int): List<ScpRecordModel>

    @Query("SELECT * FROM LaterAndHistoryTable WHERE viewListType = :viewType ORDER BY viewTime DESC")
    fun getInfoByLinkDesc(viewType: Int): List<ScpRecordModel>

    @Query("DELETE FROM LaterAndHistoryTable WHERE link = :link AND viewListType = :viewType")
    fun delete(link: String, viewType: Int)
    // AND last_update >= :timeout
}

