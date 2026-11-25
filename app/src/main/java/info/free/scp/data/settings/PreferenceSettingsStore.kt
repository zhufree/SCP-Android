package info.free.scp.data.settings

import info.free.scp.util.PreferenceUtil

interface UserPreferencesStore {
    val nickname: String
    val points: Int
    /** 稳定的“编号”，首次读取时生成并保存。 */
    val userCode: Int
    val showOtherWork: Boolean
    val queryLink: String
    val hasAcceptedPrivacy: Boolean

    fun saveNickname(nickname: String)
    fun saveJob(job: String)
    fun getJobNameForLevel(level: Int): String
}

interface HomePreferencesStore {
    val notice: String
    val shouldShowNotice: Boolean

    fun dismissNotice()
}

interface ReadingPreferencesStore {
    var hideFinishedArticles: Boolean
    /** 0 简体 / 1 繁体（[info.free.scp.SCPConstants.SIMPLE] / TRADITIONAL）。 */
    var hanzType: Int
    val shouldShowReadSuggestion: Boolean

    fun markReadSuggestionShown()
}

data class DraftDefaults(
    val title: String,
    val content: String
)

interface DraftPreferencesStore {
    fun consumeDraftDefaults(): DraftDefaults
}

interface InteractionPointsStore {
    fun increasePoints(points: Int)
}

class PreferenceSettingsStore :
    UserPreferencesStore,
    HomePreferencesStore,
    ReadingPreferencesStore,
    DraftPreferencesStore,
    InteractionPointsStore {
    override val nickname: String
        get() = PreferenceUtil.getNickname()

    override val points: Int
        get() = PreferenceUtil.getPoint()

    override val showOtherWork: Boolean
        get() = PreferenceUtil.getShowMeal()

    override val queryLink: String
        get() = PreferenceUtil.getQueryLink()

    override val hasAcceptedPrivacy: Boolean
        get() = PreferenceUtil.hasCheckPrivacy()

    override val notice: String
        get() = PreferenceUtil.getNotice()

    override val shouldShowNotice: Boolean
        get() = PreferenceUtil.getShowNotice()

    override var hideFinishedArticles: Boolean
        get() = PreferenceUtil.getHideFinished()
        set(value) = PreferenceUtil.setHideFinished(value)

    override var hanzType: Int
        get() = PreferenceUtil.getHanzType()
        set(value) = PreferenceUtil.setHanzType(value)

    override val userCode: Int
        get() = PreferenceUtil.getUserCode()

    override val shouldShowReadSuggestion: Boolean
        get() = !PreferenceUtil.getShownReadSuggest()

    override fun saveNickname(nickname: String) {
        PreferenceUtil.saveNickname(nickname)
    }

    override fun saveJob(job: String) {
        PreferenceUtil.setJob(job)
    }

    override fun getJobNameForLevel(level: Int): String {
        return PreferenceUtil.getJobNameByLevel(level)
    }

    override fun dismissNotice() {
        PreferenceUtil.setShowNotice(false)
    }

    override fun markReadSuggestionShown() {
        PreferenceUtil.setShownReadSuggest()
    }

    override fun consumeDraftDefaults(): DraftDefaults {
        val defaults = DraftDefaults(
            title = PreferenceUtil.getDraftTitle(),
            content = PreferenceUtil.getDraftContent()
        )
        PreferenceUtil.saveDraftContent("")
        PreferenceUtil.saveDraftTitle("")
        return defaults
    }

    override fun increasePoints(points: Int) {
        PreferenceUtil.addPoints(points)
    }
}
