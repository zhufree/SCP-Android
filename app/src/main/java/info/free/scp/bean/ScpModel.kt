package info.free.scp.bean

import kotlinx.serialization.Optional
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by zhufree on 2018/8/24.
 * SCP数据Model
 */

@Serializable
data class ScpModel(@SerialName("objectId") val sId: String,
                    val createdAt: String, val updatedAt: String,  // Bmob自带字段
                    var link: String, var title: String, // 都有的
                    @Optional var detailHtml: String = "", // 预留保存正文
                    @Optional var hasRead: Boolean = false, // 预留记录是否读过
                    @Optional var saveType: Int = -1, // 本地存储用
                    @Optional var author: String = "", // 部分有的
                    @Optional var subtext: String = "", @Optional var snippet: String = "",
                    @Optional var desc: String = "", // 设定
                    @Optional var number: String = "", @Optional var story_num: String = "", // 故事版
                    @Optional var index: Int = -1, // 本地数据表中的次序
                    @Optional var page_code: String = "", // 基金会故事按字母排序
                    @Optional @SerialName("created_time") var createdTime: String = "", // 基金会故事创建时间
                    @Optional var contest_name: String = "", @Optional var contest_link: String = "", // 征文竞赛
                    @Optional val type: String = "", @Optional val cn: String = "" // 查询带的字段
                    //@Optional var updatedTime: String = "",
                    )