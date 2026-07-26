package info.free.scp.ui.detail

import info.free.scp.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DetailMenuActionTest {
    @Test
    fun mapsEveryDetailMenuItemToSemanticAction() {
        val mappings = mapOf(
            R.id.switch_read_mode to DetailMenuAction.SWITCH_READ_MODE,
            R.id.open_in_browser to DetailMenuAction.OPEN_IN_BROWSER,
            R.id.copy_link to DetailMenuAction.COPY_LINK,
            R.id.like to DetailMenuAction.TOGGLE_LIKE,
            R.id.change_theme to DetailMenuAction.CHANGE_THEME,
            R.id.add_read_later to DetailMenuAction.ADD_READ_LATER,
            R.id.share_picture to DetailMenuAction.SHARE_PICTURE,
            R.id.set_cookie to DetailMenuAction.SET_COOKIE
        )

        mappings.forEach { (itemId, action) ->
            assertEquals(action, DetailMenuAction.fromMenuItemId(itemId))
        }
    }

    @Test
    fun unknownMenuItemHasNoAction() {
        assertNull(DetailMenuAction.fromMenuItemId(Int.MIN_VALUE))
    }
}
