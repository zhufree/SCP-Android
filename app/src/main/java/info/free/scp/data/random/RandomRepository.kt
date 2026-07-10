package info.free.scp.data.random

import info.free.scp.bean.ScpItemModel
import info.free.scp.db.ScpDataHelper

interface RandomDataSource {
    fun getRandomList(range: String): List<ScpItemModel>
}

class LocalRandomDataSource : RandomDataSource {
    override fun getRandomList(range: String): List<ScpItemModel> {
        return ScpDataHelper.getInstance().getRandomScpList(range)
    }
}

class RandomRepository(
    private val dataSource: RandomDataSource = LocalRandomDataSource()
) {
    fun getRandomList(
        range: String,
        currentList: List<ScpItemModel> = emptyList(),
        append: Boolean = false
    ): List<ScpItemModel> {
        val nextBatch = dataSource.getRandomList(range)
        return if (append) currentList + nextBatch else nextBatch
    }
}
