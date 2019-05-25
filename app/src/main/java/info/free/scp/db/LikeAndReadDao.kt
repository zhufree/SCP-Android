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

    @Query("SELECT link, title,hasRead, `like` FROM like_table WHERE link = :link LIMIT 1")
    fun getInfoByLink(link: String): ScpLikeModel?

    @Query("SELECT `like` FROM like_table WHERE link = :link LIMIT 1")
    fun getLikeByLink(link: String): Boolean?

    @Query("SELECT hasRead FROM like_table WHERE link = :link LIMIT 1")
    fun getHasReadByLink(link: String): Boolean?

    @Query("SELECT * FROM like_table WHERE `like` = 1")
    fun getLikeList(): List<ScpLikeModel>

    @Query("SELECT * FROM like_table WHERE `like` = 1 ORDER BY title")
    fun getOrderedLikeList(): List<ScpLikeModel>

    @Query("SELECT * FROM like_table WHERE hasRead = 1")
    fun getHasReadList(): List<ScpLikeModel>

    @Query("SELECT count(*) FROM like_table WHERE `like` = 1")
    fun getLikeCount(): Int

    @Query("SELECT count(*) FROM like_table WHERE hasRead = 1")
    fun getReadCount(): Int

    @Query("DELETE FROM like_table")
    fun clear()
    // AND last_update >= :timeout
}

