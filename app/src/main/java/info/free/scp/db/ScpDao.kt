package info.free.scp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import info.free.scp.bean.ScpModel


@Dao
interface ScpDao {
    @Insert(onConflict = REPLACE)
    fun save(article: ScpModel)

    @Insert(onConflict = REPLACE)
    fun saveAll(scps: List<ScpModel>)

    @Query("SELECT * FROM scps WHERE sId = :articleId")
    fun load(articleId: String): ScpModel

    @Query("SELECT * FROM scps WHERE link = :link LIMIT 1")
    fun getByLink(link: String): ScpModel?

    @Query("SELECT * FROM scps WHERE `index` = :index+1 AND scpType = :scpType LIMIT 1")
    fun getNext(index: Int, scpType: Int): ScpModel?

    @Query("SELECT * FROM scps WHERE `index` = :index-1 AND scpType = :scpType LIMIT 1")
    fun getPreview(index: Int, scpType: Int): ScpModel?

    @Query("SELECT * FROM scps ORDER BY updatedAt DESC")
    fun loadAll(): List<ScpModel>

    @Query("SELECT COUNT(*) FROM scps WHERE sId = :articleId")
    fun hasArticle(articleId: String): Int

    @Query("DELETE FROM scps")
    fun clear()
    // AND last_update >= :timeout
}

