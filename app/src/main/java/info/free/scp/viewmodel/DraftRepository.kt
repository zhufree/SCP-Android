package info.free.scp.viewmodel

import androidx.lifecycle.LiveData
import info.free.scp.bean.DraftModel
import info.free.scp.db.AppInfoDatabase
import info.free.scp.service.HttpManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class DraftRepository {
    var draft: LiveData<DraftModel>? = null
    private var draftDao = AppInfoDatabase.getInstance().draftDao()
    var draftList = draftDao.getAllDraft()
}