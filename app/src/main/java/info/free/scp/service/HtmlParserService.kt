package info.free.scp.service

import info.free.scp.bean.ApiBean
import info.free.scp.bean.CommentModel
import okhttp3.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class HtmlParserService {
    val SCP_BASE = "https://scp-wiki-cn.wikidot.com"
    val client = OkHttpClient()


    // Parse all comments from the HTML document
    fun parseComments(htmlCode: String): List<CommentModel> {
        val document = Jsoup.parse(htmlCode)
        val postContainers = document.select("#thread-container-posts > div.post-container")
        return postContainers.mapNotNull { parseComment(it) }
    }
    // Parse a single comment block
    private fun parseComment(postContainer: Element): CommentModel? {
        // Extract title, username, time, and comment content
        val title = postContainer.selectFirst("div.title")?.text().orEmpty()
        val username = postContainer.selectFirst("span.printuser")?.text().orEmpty()
        val time = postContainer.selectFirst("span.odate")?.text().orEmpty()

        // Extract the comment content and remove <blockquote> elements
        val contentElement = postContainer.selectFirst("div.content") ?: return null
        contentElement.select("blockquote").remove()  // Remove blockquotes
        val comment = contentElement.text()  // Extract text-only content

        // Parse replies (nested post-container elements)
        val replyContainers = postContainer.select("> div.post-container")
        val replies = replyContainers.mapNotNull { parseComment(it) }

        return CommentModel(comment, title, username, time, replies)
    }

    fun parseCommentUrl(htmlCode: String): String {
        var discussButton = Jsoup.parse(htmlCode).select("#discuss-button").first()
        if (discussButton == null){
            val discussButtons = Jsoup.parse(htmlCode).select("a") // find <a> starting with "讨论"
            discussButtons.forEach({ element ->
                if (element.text().startsWith("讨论 "))
                    discussButton = element
            })
        }
        if (discussButton !== null) {
            if (discussButton!!.text() == "讨论 (0)"){
                noComment = true // use it in DetailActivity later
            }
            return discussButton!!.attr("href")
        }
        throw Exception("Comment link not found")
    }

    suspend fun getComment(
        scpId: String,
        cookie: String,
        agent: String
    ): ApiBean.ApiListResponse<CommentModel> = suspendCoroutine { continuation ->
        noComment = false // initialize this status every time we start
        val requestMainPage: Request = Request.Builder()
            .url(SCP_BASE + "/" + scpId)
            .addHeader("Cookie", cookie)
            .addHeader("User-Agent", agent)
            .build()
        client.newCall(requestMainPage).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(
                    IOException("$e on $scpId")
                )
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    try {
                        if (!response.isSuccessful || response.body == null) {
                            throw IOException("Unexpected code $response on $scpId")
                        }
                        val commentUrl = parseCommentUrl(response.body!!.string())
                        val requestForComment: Request = Request.Builder()
                            .url(SCP_BASE + commentUrl)
                            .addHeader("Cookie", cookie)
                            .addHeader("User-Agent", agent)
                            .build()

                        val callForComment: Call = client.newCall(requestForComment)
                        callForComment.enqueue(object: Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                throw IOException(
                                    "$e on $commentUrl"
                                )
                            }

                            override fun onResponse(call: Call, response: Response) {
                                response.use {
                                    if (!response.isSuccessful || response.body == null) {
                                        throw IOException("Unexpected code $response on $commentUrl")
                                    }
                                    val comments = parseComments(response.body!!.string())
                                    continuation.resume(ApiBean.ApiListResponse<CommentModel>(comments))
                                }
                            }
                        })
                    } catch (e: Exception){
                        continuation.resumeWithException(e)
                    }
                }
            }
        })

    }

    companion object {
        var noComment: Boolean = false
    }

}