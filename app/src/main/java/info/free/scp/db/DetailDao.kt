package info.free.scp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import info.free.scp.bean.ScpDetail
import info.free.scp.bean.ScpItemModel


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

    @Query("SELECT link FROM scp_detail WHERE detail LIKE :keyword;")
    suspend fun searchScpByDetail(keyword: String): List<String>
    // AND last_update >= :timeout
}

