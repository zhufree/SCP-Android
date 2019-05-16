package info.free.scp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import info.free.scp.bean.ScpLikeModel

/**
 * 读过和like信息
 */
@Dao
interface LikeAndReadDao {
    @Insert(onConflict = REPLACE)
    fun save(info: ScpLikeModel)

    @Query("SELECT link, title,hasRead, `like` FROM LikeAndReadTable WHERE link = :link LIMIT 1")
    fun getInfoByLink(link: String): ScpLikeModel?

    @Query("SELECT * FROM LikeAndReadTable WHERE `like` = 1")
    fun getLikeList(): List<ScpLikeModel>

    @Query("SELECT * FROM LikeAndReadTable WHERE `like` = 1 ORDER BY title")
    fun getOrderedLikeList(): List<ScpLikeModel>

    @Query("SELECT count(*) FROM LikeAndReadTable WHERE `like` = 1")
    fun getLikeCOunt(): List<ScpLikeModel>

    @Query("DELETE FROM LikeAndReadTable")
    fun clear()
    // AND last_update >= :timeout
}

