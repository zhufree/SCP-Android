package info.free.scp.ui.detail

import info.free.scp.R

enum class DetailMenuAction {
    SWITCH_READ_MODE,
    OPEN_IN_BROWSER,
    COPY_LINK,
    TOGGLE_LIKE,
    CHANGE_THEME,
    ADD_READ_LATER,
    SHARE_PICTURE,
    SET_COOKIE;

    companion object {
        fun fromMenuItemId(itemId: Int): DetailMenuAction? {
            return when (itemId) {
                R.id.switch_read_mode -> SWITCH_READ_MODE
                R.id.open_in_browser -> OPEN_IN_BROWSER
                R.id.copy_link -> COPY_LINK
                R.id.like -> TOGGLE_LIKE
                R.id.change_theme -> CHANGE_THEME
                R.id.add_read_later -> ADD_READ_LATER
                R.id.share_picture -> SHARE_PICTURE
                R.id.set_cookie -> SET_COOKIE
                else -> null
            }
        }
    }
}
