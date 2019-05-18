package info.free.scp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import info.free.scp.bean.ScpCollectionModel
import info.free.scp.bean.ScpItemModel


@Dao
interface ScpDao {
    @Insert(onConflict = REPLACE)
    fun save(article: ScpItemModel)

    @Insert(onConflict = REPLACE)
    fun saveAll(scps: List<ScpItemModel>)

    @Query("SELECT * FROM scps WHERE link = :link ORDER BY _index LIMIT 1")
    fun getScpByLink(link: String): ScpItemModel?

    @Query("SELECT * FROM scp_collection WHERE link = :link ORDER BY _index LIMIT 1")
    fun getCollectionByLink(link: String): ScpCollectionModel?

    @Query("SELECT * FROM scps WHERE `_index` = :index+1 AND scp_type = :scpType LIMIT 1")
    fun getNextScp(index: Int, scpType: Int): ScpItemModel?

    @Query("SELECT * FROM scps WHERE `_index` = :index-1 AND scp_type = :scpType LIMIT 1")
    fun getPreviewScp(index: Int, scpType: Int): ScpItemModel?

    @Query("SELECT * FROM scp_collection WHERE `_index` = :index+1 AND scp_type = :scpType LIMIT 1")
    fun getNextCollection(index: Int, scpType: Int): ScpCollectionModel?

    @Query("SELECT * FROM scp_collection WHERE `_index` = :index-1 AND scp_type = :scpType LIMIT 1")
    fun getPreviewCollection(index: Int, scpType: Int): ScpCollectionModel?

    @Query("SELECT * FROM scps WHERE `scp_type` = :type ORDER BY _index")
    fun getAllScpListByType(type: Int): List<ScpItemModel>

    @Query("SELECT * FROM scp_collection WHERE `scp_type` = :type ORDER BY _index")
    fun getAllCollectionByType(type: Int): List<ScpCollectionModel>

    @Query("SELECT * FROM scps WHERE `scp_type` = :type AND sub_scp_type = :letterOrMonth ")
    fun getTalesByTypeAndSubType(type: Int, letterOrMonth: String): List<ScpItemModel>

    @Query("SELECT * FROM scps WHERE `title` LIKE :keyword;")
    fun searchScpByTitle(keyword: String): List<ScpItemModel>

    @Query("SELECT * FROM scps as scp left join scp_detail as detail on scp.link = detail.link WHERE detail.detail LIKE :keyword;")
    fun searchScpByDetail(keyword: String): List<ScpItemModel>

    @Query("SELECT * FROM scps WHERE scp_type = :type1 OR scp_type = :type2 ORDER BY random() LIMIT 1;")
    fun getRandomScpByType(type1: String, type2: String): ScpItemModel?

    @Query("SELECT * FROM scps ORDER BY random() LIMIT 1;")
    fun getRandomScp(): ScpItemModel?

    @Query("SELECT * FROM scps WHERE scp_type = :type AND title LIKE :number;")
    fun getScpByNumber(type: Int, number: String): ScpItemModel?

    @Query("DELETE FROM scps")
    fun clear()
    // AND last_update >= :timeout
}

