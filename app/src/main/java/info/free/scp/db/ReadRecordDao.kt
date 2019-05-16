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

    @Query("DELETE FROM LaterAndHistoryTable")
    fun clear()
    // AND last_update >= :timeout
}

