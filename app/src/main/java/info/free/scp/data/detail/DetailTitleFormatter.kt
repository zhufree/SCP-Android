package info.free.scp.data.detail

object DetailTitleFormatter {
    fun toScreenshotFileName(title: String): String {
        return title.replace(" ", "")
    }
}
