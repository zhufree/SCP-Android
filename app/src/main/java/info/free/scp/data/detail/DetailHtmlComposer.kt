package info.free.scp.data.detail

class DetailHtmlComposer(
    textSize: String,
    initialNightTheme: Boolean
) {
    private val siteStyle = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />"
    private val nightTextStyle = "<style>body{background-color:#222;}p {font-size:" +
        "$textSize;}* {color:#aaa;} blockquote, .forum-category-box  .description-block, .content-panel, .scp-image-block .scp-image-caption, table.wiki-content-table th, #toc, #lock-info, .change-textarea-size a, .mobile-top-bar li ul li a, .mobile-top-bar li ul, .top-bar li ul, .description, .btn-default, div[style*=\"background:#F5F5F5;\"]{background:#333!important;} div[style*=\"background:#f2f2c2;\"]{background:#f2f2c22b!important;} div[style*=\"background:#d3d3d3;\"]{background:#d3d3d32b!important;}</style>"
    private val dayTextStyle =
        "<style>body{background-color:#fff;}p {font-size:$textSize;}* {color:#000;}</style>"
    private val darkReaderScript =
        "<script type=\"text/javascript\" src=\"darkreader.min.js\"></script><script>document.addEventListener('DOMContentLoaded', ()=>{DarkReader.enable({\n" +
            "    brightness: 100,\n" +
            "    contrast: 90,\n" +
            "    sepia: 10\n" +
            "})});</script>"
    private val pageScripts = buildString {
        if (initialNightTheme) append(darkReaderScript)
        append("<script type=\"text/javascript\" src=\"jquery-ui.min.js\"></script>\n")
        append("<script type=\"text/javascript\" src=\"init.combined.js\"></script>")
        append("<script type=\"text/javascript\" src=\"tabview-min.js\"></script>")
        append("<script type=\"text/javascript\" src=\"WIKIDOT.combined.js\"></script>")
    }

    fun compose(content: String, nightTheme: Boolean): String {
        val textStyle = if (nightTheme) nightTextStyle else dayTextStyle
        return siteStyle + textStyle + content + pageScripts
    }
}
