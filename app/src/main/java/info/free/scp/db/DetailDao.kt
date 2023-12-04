package info.free.scp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.RoomWarnings
import info.free.scp.bean.ScpDetail


@Dao
interface DetailDao {
    @Insert(onConflict = REPLACE)
    fun save(article: ScpDetail)

    @Insert(onConflict = REPLACE)
    fun saveAll(scps: List<ScpDetail>)

    @Query("SELECT detail FROM scp_detail WHERE link = :link")
    fun getDetail(link: String): String?

    @Query("SELECT detail FROM scp_detail WHERE link = :link")
    fun getLiveDetail(link: String): LiveData<String>?

    @Query("SELECT tags FROM scp_detail WHERE link = :link")
    fun getTag(link: String): String?

    @Query("SELECT tags FROM scp_detail WHERE link = :link")
    fun getLiveTag(link: String): LiveData<String>?

    @Query("SELECT link FROM scp_detail WHERE tags LIKE :tag")
    fun getLinksByTag(tag: String): List<String>

    @Query("SELECT tags FROM scp_detail;")
    fun getAllTags(): List<String?>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT link FROM scp_detail WHERE detail LIKE :keyword;")
    fun searchScpByDetail(keyword: String): List<String>
    // AND last_update >= :timeout
}

