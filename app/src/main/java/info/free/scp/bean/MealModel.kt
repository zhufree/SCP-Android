package info.free.scp.bean

data class MealModel(
        val title: String,
        val desc: String,
        val link: String,
        val order: Int,
        val recommend: String,
        val logoUrl: String
) : BaseModel()