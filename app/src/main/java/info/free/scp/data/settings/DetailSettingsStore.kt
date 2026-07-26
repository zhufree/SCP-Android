package info.free.scp.data.settings

import info.free.scp.SCPConstants
import info.free.scp.util.PreferenceUtil

data class DetailWebCredentials(
    val cookie: String,
    val userAgent: String
)

interface DetailSettingsStore {
    val isOfflineReadingEnabled: Boolean
    var detailTextSize: String
    val webCredentials: DetailWebCredentials
    val shouldShowDetailNotice: Boolean

    fun updateWebCredentials(cookie: String, userAgent: String)
    fun markDetailNoticeShown()
    fun increasePoints(points: Int)
    fun decreasePoints(points: Int)

    fun hasCommentCredentials(): Boolean = webCredentials.cookie.isNotEmpty()
}

class PreferenceDetailSettingsStore : DetailSettingsStore {
    override val isOfflineReadingEnabled: Boolean
        get() = PreferenceUtil.getAppMode() == SCPConstants.AppMode.OFFLINE

    override var detailTextSize: String
        get() = PreferenceUtil.getDetailTextSize()
        set(value) = PreferenceUtil.setDetailTextSize(value)

    override val webCredentials: DetailWebCredentials
        get() = DetailWebCredentials(
            cookie = PreferenceUtil.getCookie(),
            userAgent = PreferenceUtil.getAgent()
        )

    override val shouldShowDetailNotice: Boolean
        get() = !PreferenceUtil.getShownDetailNotice()

    override fun updateWebCredentials(cookie: String, userAgent: String) {
        PreferenceUtil.setCookie(cookie)
        PreferenceUtil.setAgent(userAgent)
    }

    override fun markDetailNoticeShown() {
        PreferenceUtil.setShownDetailNotice()
    }

    override fun increasePoints(points: Int) {
        PreferenceUtil.addPoints(points)
    }

    override fun decreasePoints(points: Int) {
        PreferenceUtil.reducePoints(points)
    }
}
