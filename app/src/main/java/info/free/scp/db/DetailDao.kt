package info.free.scp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import info.free.scp.bean.ScpDetail


@Dao
interface DetailDao {
    @Insert(onConflict = REPLACE)
    fun save(article: ScpDetail)

    @Insert(onConflict = REPLACE)
    fun saveAll(scps: List<ScpDetail>)

    @Query("SELECT detail FROM details WHERE link = :link")
    fun getDetail(link: String): String

    @Query("DELETE FROM details")
    fun clear()
    // AND last_update >= :timeout
}

