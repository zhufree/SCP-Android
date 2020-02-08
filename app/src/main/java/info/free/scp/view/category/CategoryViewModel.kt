package info.free.scp.view.category

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewModelScope
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.ScpType.SAVE_ABNORMAL
import info.free.scp.SCPConstants.ScpType.SAVE_ARCHIVED
import info.free.scp.SCPConstants.ScpType.SAVE_CONTEST
import info.free.scp.SCPConstants.ScpType.SAVE_CONTEST_CN
import info.free.scp.SCPConstants.ScpType.SAVE_DECOMMISSIONED
import info.free.scp.SCPConstants.ScpType.SAVE_EX
import info.free.scp.SCPConstants.ScpType.SAVE_EX_CN
import info.free.scp.SCPConstants.ScpType.SAVE_INFO
import info.free.scp.SCPConstants.ScpType.SAVE_INTERNATIONAL
import info.free.scp.SCPConstants.ScpType.SAVE_INTRO
import info.free.scp.SCPConstants.ScpType.SAVE_JOKE
import info.free.scp.SCPConstants.ScpType.SAVE_JOKE_CN
import info.free.scp.SCPConstants.ScpType.SAVE_REMOVED
import info.free.scp.SCPConstants.ScpType.SAVE_SERIES
import info.free.scp.SCPConstants.ScpType.SAVE_SERIES_CN
import info.free.scp.SCPConstants.ScpType.SAVE_SETTINGS
import info.free.scp.SCPConstants.ScpType.SAVE_SETTINGS_CN
import info.free.scp.SCPConstants.ScpType.SAVE_STORY_SERIES
import info.free.scp.SCPConstants.ScpType.SAVE_STORY_SERIES_CN
import info.free.scp.SCPConstants.ScpType.SAVE_TALES
import info.free.scp.SCPConstants.ScpType.SAVE_TALES_CN
import info.free.scp.SCPConstants.ScpType.SINGLE_PAGE
import info.free.scp.bean.ScpModel
import info.free.scp.db.ScpDataHelper
import info.free.scp.util.PreferenceUtil
import kotlinx.coroutines.launch

class CategoryViewModel : ViewModel() {
    private val catRepo = CategoryRepository()
    private val categoryCount = PreferenceUtil.getCategoryCount()
    private val taleCategory = arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
            "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "0-9")
    private val country = arrayOf("俄国分部", "韩国分部", "法国分部", "波兰分部", "西班牙分部", "泰国分部",
            "日本分部", "德国分部", "意大利分部", "乌克兰分部", "葡萄牙语分部", "捷克分部", "非官方分部")

    fun getCat(): MutableLiveData<List<ScpModel>>? {
        return catRepo.scpList
    }

    fun loadCat(categoryType: Int, clickPosition: Int) {
        var start = 0
        var limit = 100
        var saveType = categoryType
        var subScpType = "0"
        when (categoryType) {
            SCPConstants.Category.SERIES -> {
                // 0,499,999
                start = if (clickPosition == 0) 0 else clickPosition * categoryCount
                limit = categoryCount
                saveType = SAVE_SERIES
            }
            SCPConstants.Category.SERIES_CN -> {
                start = if (clickPosition == 0) 0 else clickPosition * categoryCount
                limit = categoryCount
                saveType = SAVE_SERIES_CN
            }
            SCPConstants.Category.SCP_EX -> {
                saveType = SAVE_EX // + EX_CN
            }
            SCPConstants.Category.SCP_INTERNATIONAL -> {
                saveType = SAVE_INTERNATIONAL
                subScpType = country[clickPosition] + '%'
            }
            SCPConstants.Category.SCP_ABNORMAL, SCPConstants.Category.ABOUT_INFO, SCPConstants.Category.ABOUT_INTRO -> {
                saveType = SINGLE_PAGE
            }

            SCPConstants.Category.SCP_ARCHIVES -> {
                when (clickPosition) {
                    0 -> saveType = SAVE_EX
                    1 -> saveType = SAVE_EX_CN
                    2 -> saveType = SAVE_ARCHIVED
                    3 -> saveType = SAVE_DECOMMISSIONED
                    4 -> saveType = SAVE_REMOVED
                }
            }
            SCPConstants.Category.TALES -> {
                saveType = SAVE_TALES
                subScpType = taleCategory[clickPosition]
            }
            SCPConstants.Category.TALES_CN -> {
                saveType = SAVE_TALES_CN
                subScpType = taleCategory[clickPosition]
            }
            // 废弃
            SCPConstants.Category.STORY_SERIES -> {
                saveType = SAVE_STORY_SERIES
            }
            SCPConstants.Category.STORY_SERIES_CN -> {
                saveType = SAVE_STORY_SERIES_CN
            }
            SCPConstants.Category.JOKE -> {
                // 内容较少，直接全部加载
                saveType = SAVE_JOKE
            }
            SCPConstants.Category.JOKE_CN -> {
                // 内容较少，直接全部加载
                saveType = SAVE_JOKE_CN
            }
            SCPConstants.Category.SETTINGS -> {
                saveType = SAVE_SETTINGS
            }
            SCPConstants.Category.SETTINGS_CN -> {
                saveType = SAVE_SETTINGS_CN
            }
            SCPConstants.Category.CONTEST -> {
                saveType = SAVE_CONTEST
            }
            SCPConstants.Category.CONTEST_CN -> {
                saveType = SAVE_CONTEST_CN
            }

            // 废弃,应该是用createTime判断
            SCPConstants.Category.TALES_BY_TIME -> {
                saveType = SAVE_TALES_CN
                subScpType = when (clickPosition) {
                    0 -> {
                        "2018"
                    }
                    1 -> {
                        "2017"
                    }
                    2 -> {
                        "2016"
                    }
                    3 -> {
                        "2015"
                    }
                    4 -> {
                        "2014"
                    }
                    else -> "2018"
                }
            }
        }
        viewModelScope.launch {
            catRepo.loadCatList(saveType, subScpType, limit, start, categoryType)
        }
    }

    fun reverseCat() {
        catRepo.reverseCat()
    }
}
