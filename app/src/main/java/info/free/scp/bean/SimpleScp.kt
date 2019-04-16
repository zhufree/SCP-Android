package info.free.scp.bean

/**
 * 用于列表展示，不带太多信息
 */
data class SimpleScp(var link: String, var title: String, var viewTime: String, var hasRead: Int = 0,
                     var like: Int = 0)