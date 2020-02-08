package info.free.scp.bean


/**
 * 传送门model
 */
data class PortalModel(
        val title: String,
        val url: String,
        val logoUrl: String
) : BaseModel()