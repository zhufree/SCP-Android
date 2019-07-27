package info.free.scp.bean

import androidx.room.Entity
import androidx.room.PrimaryKey
import info.free.scp.util.TimeUtil

@Entity(tableName = "draft")
data class DraftModel(@PrimaryKey var draftId: Int, var lastModifyTime: Long,
                      var title: String = "", var content: String = "") {
    fun getLastTimeString(): String {
        return TimeUtil.timeStampToStr(lastModifyTime)
    }
}