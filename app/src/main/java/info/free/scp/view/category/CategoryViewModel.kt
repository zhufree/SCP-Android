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
import info.free.scp.bean.ScpModel
import info.free.scp.db.ScpDataHelper
import info.free.scp.util.PreferenceUtil
import kotlinx.coroutines.launch

class CategoryViewModel : ViewModel() {
    private val catRepo = CategoryRepository()
    private val categoryCount = PreferenceUtil.getCategoryCount()

    fun getCat(): MutableLiveData<List<ScpModel>>? {
        return catRepo.scpList
    }

    fun loadCat(categoryType: Int, clickPosition: Int) {
        var start = 0
        var limit = 100
        var saveType = categoryType // TODO
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

            }
            SCPConstants.Category.SCP_ABNORMAL -> {
                saveType = SAVE_ABNORMAL
                // 三句话外围
//                localScpList?.add(ScpDatabase.getInstance()?.scpDao()?.getScpByLink("/short-stories"))
            }
            SCPConstants.Category.ABOUT_INFO -> {
                // 相关材料
                saveType = SAVE_INFO
            }
            SCPConstants.Category.ABOUT_INTRO -> {
                // 相关材料
                saveType = SAVE_INTRO
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
//                localScpList?.addAll(ScpDataHelper.getInstance().getTalesByTypeAndSubType(SCPConstants.ScpType.SAVE_TALES
//                        , taleCategory[clickPosition]))
            }
            SCPConstants.Category.TALES_CN -> {
//                localScpList?.addAll(ScpDataHelper.getInstance().getTalesByTypeAndSubType(SCPConstants.ScpType.SAVE_TALES_CN
//                        , taleCategory[clickPosition]))
            }
            // 废弃
//            SCPConstants.Category.STORY_SERIES -> {
//                saveType = SAVE_STORY_SERIES
//            }
//            SCPConstants.Category.STORY_SERIES_CN -> {
//                saveType = SAVE_STORY_SERIES_CN
//            }
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

            SCPConstants.Category.TALES_BY_TIME -> {
//                if (taleTimeList.isEmpty()) {
//                    taleTimeList.addAll(ScpDataHelper.getInstance().getScpByType(SCPConstants.ScpType.SAVE_TALES_CN))
//                }
//                when (clickPosition) {
//                    0 -> {
//                        localScpList?.addAll(taleTimeList.filter { (it as ScpItemModel).subScpType?.startsWith("2018") == true })
//                    }
//                    1 -> {
//                        localScpList?.addAll(taleTimeList.filter { (it as ScpItemModel).subScpType?.startsWith("2017") == true })
//                    }
//                    2 -> {
//                        localScpList?.addAll(taleTimeList.filter { (it as ScpItemModel).subScpType?.startsWith("2016") == true })
//                    }
//                    3 -> {
//                        localScpList?.addAll(taleTimeList.filter { (it as ScpItemModel).subScpType?.startsWith("2015") == true })
//                    }
//                    4 -> {
//                        localScpList?.addAll(taleTimeList.filter { (it as ScpItemModel).subScpType?.startsWith("2014") == true })
//                    }
//                }
            }
        }
        viewModelScope.launch {
            catRepo.loadCatList(saveType, limit, start)
        }
    }

    fun reverseCat() {
        catRepo.reverseCat()
    }
}
