package info.free.scp.data.user

import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.OrderType.ASC
import info.free.scp.bean.ScpRecordModel
import info.free.scp.db.AppInfoDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class UserStats(
    val readCount: Int,
    val likeCount: Int
)

class UserDashboardRepository {
    private val likeDao = AppInfoDatabase.getInstance().likeAndReadDao()
    private val recordDao = AppInfoDatabase.getInstance().readRecordDao()

    suspend fun loadUserStats(): UserStats = withContext(Dispatchers.IO) {
        UserStats(
            readCount = likeDao.getReadCount(),
            likeCount = likeDao.getLikeCount()
        )
    }

    suspend fun loadRecentHistory(limit: Int = 5): List<ScpRecordModel> = withContext(Dispatchers.IO) {
        recordDao.getInfoByLinkDesc(SCPConstants.HISTORY_TYPE)
            .take(limit)
    }

    suspend fun loadRecordList(type: Int, order: Int): List<ScpRecordModel> = withContext(Dispatchers.IO) {
        if (order == ASC) {
            recordDao.getInfoByLinkAsc(type)
        } else {
            recordDao.getInfoByLinkDesc(type)
        }
    }
}
