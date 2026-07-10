package info.free.scp.data.catalog

import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.ScpType.SAVE_ANOMALOUS_CN
import info.free.scp.SCPConstants.ScpType.SAVE_CANON
import info.free.scp.SCPConstants.ScpType.SAVE_CANON_CN
import info.free.scp.SCPConstants.ScpType.SAVE_CONTEST
import info.free.scp.SCPConstants.ScpType.SAVE_CONTEST_CN
import info.free.scp.SCPConstants.ScpType.SAVE_EX
import info.free.scp.SCPConstants.ScpType.SAVE_EX_CN
import info.free.scp.SCPConstants.ScpType.SAVE_INFO_PAGE
import info.free.scp.SCPConstants.ScpType.SAVE_INTERNATIONAL
import info.free.scp.SCPConstants.ScpType.SAVE_JOKE
import info.free.scp.SCPConstants.ScpType.SAVE_JOKE_CN
import info.free.scp.SCPConstants.ScpType.SAVE_LIBRARY_PAGE
import info.free.scp.SCPConstants.ScpType.SAVE_SERIES
import info.free.scp.SCPConstants.ScpType.SAVE_SERIES_CN
import info.free.scp.SCPConstants.ScpType.SAVE_SHORT_STORY
import info.free.scp.SCPConstants.ScpType.SAVE_STORY_SERIES
import info.free.scp.SCPConstants.ScpType.SAVE_STORY_SERIES_CN
import info.free.scp.SCPConstants.ScpType.SAVE_TALES
import info.free.scp.SCPConstants.ScpType.SAVE_TALES_BY_TIME
import info.free.scp.SCPConstants.ScpType.SAVE_TALES_CN
import info.free.scp.SCPConstants.ScpType.SAVE_WANDER
import info.free.scp.SCPConstants.ScpType.SAVE_WANDER_CN
import info.free.scp.bean.ScpModel
import info.free.scp.db.AppInfoDatabase
import info.free.scp.db.DetailDatabase
import info.free.scp.db.ScpDatabase
import info.free.scp.util.PreferenceUtil

interface ScpCatalogDataSource {
    fun getDocumentsByType(type: Int): List<ScpModel>
    fun getTalesByTypeAndSubType(type: Int, subType: String): List<ScpModel>
    fun getInternationalByCountry(countryPrefix: String): List<ScpModel>
    fun searchByTitle(keyword: String): List<ScpModel>
    fun searchDetailLinks(keyword: String): List<String>
    fun getDocumentByLink(link: String): ScpModel?
    fun getReadLinks(): Set<String>
    fun shouldHideFinished(): Boolean
}

class RoomScpCatalogDataSource : ScpCatalogDataSource {
    private val scpDao by lazy { ScpDatabase.getInstance()?.scpDao() }
    private val detailDao by lazy { DetailDatabase.getInstance()?.detailDao() }
    private val likeDao by lazy { AppInfoDatabase.getInstance().likeAndReadDao() }

    override fun getDocumentsByType(type: Int): List<ScpModel> {
        return scpDao?.getAllScpListByType(type).orEmpty()
    }

    override fun getTalesByTypeAndSubType(type: Int, subType: String): List<ScpModel> {
        return scpDao?.getTalesByTypeAndSubType(type, subType).orEmpty()
    }

    override fun getInternationalByCountry(countryPrefix: String): List<ScpModel> {
        return scpDao?.getInternationalByCountry(countryPrefix).orEmpty()
    }

    override fun searchByTitle(keyword: String): List<ScpModel> {
        return scpDao?.searchScpByTitle(keyword).orEmpty()
    }

    override fun searchDetailLinks(keyword: String): List<String> {
        return detailDao?.searchScpByDetail(keyword).orEmpty()
    }

    override fun getDocumentByLink(link: String): ScpModel? {
        return scpDao?.getScpByLink(link)
    }

    override fun getReadLinks(): Set<String> {
        return likeDao.getHasReadList().mapTo(mutableSetOf()) { it.link }
    }

    override fun shouldHideFinished(): Boolean {
        return PreferenceUtil.getHideFinished()
    }
}

class ScpCatalogRepository(
    private val dataSource: ScpCatalogDataSource = RoomScpCatalogDataSource(),
    private val categoryCount: Int = 100
) {
    fun getDocList(saveType: Int, groupIndex: Int = -1, extraType: String = ""): List<ScpModel> {
        val documents = when (saveType) {
            SAVE_SERIES, SAVE_SERIES_CN, SAVE_JOKE, SAVE_JOKE_CN, SAVE_EX, SAVE_EX_CN -> {
                dataSource.getDocumentsByType(saveType)
            }
            SAVE_TALES, SAVE_TALES_CN, SAVE_TALES_BY_TIME, SAVE_WANDER, SAVE_WANDER_CN -> {
                dataSource.getTalesByTypeAndSubType(saveType, extraType)
            }
            SAVE_CANON, SAVE_CANON_CN, SAVE_STORY_SERIES, SAVE_STORY_SERIES_CN,
            SAVE_CONTEST, SAVE_CONTEST_CN -> {
                dataSource.getDocumentsByType(saveType)
            }
            SCPConstants.Entry.LIBRARY_DOC -> {
                dataSource.getDocumentsByType(SAVE_LIBRARY_PAGE) +
                    dataSource.getDocumentsByType(SAVE_SHORT_STORY) +
                    dataSource.getDocumentsByType(SAVE_ANOMALOUS_CN)
            }
            SCPConstants.Entry.INFORMATION_DOC -> {
                dataSource.getDocumentsByType(SAVE_INFO_PAGE)
            }
            SAVE_INTERNATIONAL -> {
                dataSource.getInternationalByCountry("$extraType%")
            }
            else -> {
                dataSource.getDocumentsByType(saveType)
            }
        }
        val page = documents.page(groupIndex)
        return if (dataSource.shouldHideFinished()) {
            val readLinks = dataSource.getReadLinks()
            page.filterNot { it.link in readLinks }
        } else {
            page
        }
    }

    fun searchTitle(keyword: String): List<ScpModel> {
        return dataSource.searchByTitle(keyword)
    }

    fun searchDetail(keyword: String): List<ScpModel> {
        return dataSource.searchDetailLinks(keyword).mapNotNull { link ->
            dataSource.getDocumentByLink(link)
        }
    }

    private fun List<ScpModel>.page(groupIndex: Int): List<ScpModel> {
        if (groupIndex < 0) return this
        val start = groupIndex * categoryCount
        if (start >= size) return emptyList()
        return drop(start).take(categoryCount)
    }
}
