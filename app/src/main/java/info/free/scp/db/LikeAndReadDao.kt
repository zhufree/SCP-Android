package info.free.scp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import info.free.scp.bean.ScpModel
import info.free.scp.bean.ScpReadModel


@Dao
interface LikeAndReadDao {
    @Insert(onConflict = REPLACE)
    fun save(info: ScpReadModel)

    @Query("SELECT * FROM LikeAndReadTable WHERE link = :link LIMIT 1")
    fun getInfoByLink(link: String): ScpReadModel?

    @Query("DELETE FROM LikeAndReadTable")
    fun clear()
    // AND last_update >= :timeout
}

