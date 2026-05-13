package info.free.scp.ui.user

import info.free.scp.bean.ScpRecordModel

data class UserDashboardUiState(
    val readCount: Int = 0,
    val likeCount: Int = 0,
    val recentHistory: List<ScpRecordModel> = emptyList()
)
