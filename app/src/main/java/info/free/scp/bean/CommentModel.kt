package info.free.scp.bean

data class CommentModel(var comment: String = "", var reply: List<CommentModel>)