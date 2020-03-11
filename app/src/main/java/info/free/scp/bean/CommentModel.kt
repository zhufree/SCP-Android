package info.free.scp.bean

data class CommentModel(var comment: String = "",
                        var title: String = "",
                        var username: String = "",
                        var time: String = "",
                        var reply: List<CommentModel>)