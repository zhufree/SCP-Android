package info.free.scp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import info.free.scp.bean.ScpLikeBox
import info.free.scp.bean.ScpLikeModel

/**
 * 读过和like信息
 */
@Dao
interface LikeAndReadDao {
    @Insert(onConflict = REPLACE)
    fun save(info: ScpLikeModel)

    @Query("SELECT * FROM like_table WHERE link = :link LIMIT 1")
    fun getInfoByLink(link: String): ScpLikeModel?

    @Query("SELECT `like` FROM like_table WHERE link = :link LIMIT 1")
    fun getLikeByLink(link: String): Boolean?

    @Query("SELECT hasRead FROM like_table WHERE link = :link LIMIT 1")
    fun getHasReadByLink(link: String): Boolean?

    @Query("SELECT * FROM like_table WHERE `like` = 1")
    fun getLikeList(): List<ScpLikeModel>

    @Query("SELECT * FROM like_table WHERE `boxId` = :boxId")
    fun getLikeListByBoxId(boxId: Int): List<ScpLikeModel>

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

    @Query("SELECT * FROM like_box_table;")
    fun getLikeBox(): List<ScpLikeBox>

    @Insert(onConflict = REPLACE)
    fun addLikeBox(box: ScpLikeBox)
}

