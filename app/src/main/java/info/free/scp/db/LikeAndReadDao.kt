package info.free.scp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Delete
import info.free.scp.bean.ScpLikeBox
import info.free.scp.bean.ScpLikeModel

/**
 * 读过和like信息
 */
@Dao
interface LikeAndReadDao {
    @Insert(onConflict = REPLACE)
    fun save(info: ScpLikeModel)

    @Insert(onConflict = REPLACE)
    fun saveAll(info: List<ScpLikeModel>)

    @Query("SELECT * FROM like_table WHERE link = :link LIMIT 1")
    fun getInfoByLink(link: String): ScpLikeModel?

    @Query("SELECT * FROM like_table WHERE link = :link LIMIT 1")
    fun getLiveInfoByLink(link: String): LiveData<ScpLikeModel>?

    @Query("SELECT `like` FROM like_table WHERE link = :link LIMIT 1")
    fun getLikeByLink(link: String): Boolean?

    @Query("SELECT hasRead FROM like_table WHERE link = :link LIMIT 1")
    fun getHasReadByLink(link: String): Boolean?

    @Query("SELECT * FROM like_table WHERE `like` = 1")
    fun getLikeList(): List<ScpLikeModel>

    @Query("SELECT * FROM like_table WHERE `like` = 1 AND `boxId` = :boxId")
    fun getLikeListByBoxId(boxId: Int): List<ScpLikeModel>

    @Delete
    fun deleteLike(like: ScpLikeModel)

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

    @Query("SELECT * FROM like_box_table;")
    fun getLiveLikeBox(): LiveData<List<ScpLikeBox>>

    @Query("SELECT * FROM like_box_table WHERE id = :boxId")
    fun getLikeBoxById(boxId: Int): ScpLikeBox

    @Insert(onConflict = REPLACE)
    fun saveLikeBox(box: ScpLikeBox)

    @Query("DELETE FROM like_box_table WHERE id = :boxId")
    fun deleteLikeBoxById(boxId: Int)
}

