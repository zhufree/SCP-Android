package info.free.scp.data.backup

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class RestoreSwapTest {
    @get:Rule val temp = TemporaryFolder()

    @Test fun rollsBackDatabaseAndBothPreferencesIfOpeningTheNewDatabaseFails() {
        val pending = temp.newFolder("pending")
        val live = temp.newFolder("live")
        val targets = listOf("user.db", "app.xml", "level.xml").map { name ->
            File(pending, name).writeText("new $name")
            File(live, name).apply { writeText("old $name") }
        }
        val messages = mutableListOf<String>()
        RestoreSwap(pending, targets).install({}, {}, {
            assertTrue(targets.all { it.readText() == "new ${it.name}" })
            assertTrue(targets.all { File(pending, it.name + ".bak").isFile })
            error("Room identity mismatch")
        }, messages::add)

        assertTrue(targets.all { it.readText() == "old ${it.name}" })
        assertFalse(pending.exists())
        assertTrue(messages.single().contains("已还原"))
    }

    @Test fun interruptedRestoreRollsBackPromotedFilesAndKeepsUntouchedFiles() {
        val pending = temp.newFolder("pending")
        val live = temp.newFolder("live")
        val database = File(live, "user.db").apply { writeText("new database") }
        val prefs = File(live, "app.xml").apply { writeText("old preferences") }
        val added = File(live, "level.xml").apply { writeText("new level") }
        File(pending, "originals").writeText("user.db\napp.xml")
        File(pending, "user.db.bak").writeText("old database")

        val messages = mutableListOf<String>()
        RestoreSwap(pending, listOf(database, prefs, added)).install(
            { error("must recover first") }, {}, { error("must recover first") }, messages::add
        )

        assertEquals("old database", database.readText())
        assertEquals("old preferences", prefs.readText())
        assertFalse(added.exists())
        assertTrue(messages.single().contains("中断"))
    }

    @Test fun aMissingStagedPreferenceLeavesTheOriginalDatabaseInPlace() {
        val pending = temp.newFolder("pending")
        val database = temp.newFile("user.db").apply { writeText("old") }
        val prefs = temp.newFile("app.xml").apply { writeText("old preferences") }
        File(pending, database.name).writeText("new")
        val messages = mutableListOf<String>()
        RestoreSwap(pending, listOf(database, prefs)).install(
            { error("must check all staged files first") }, {}, { error("must not promote") }, messages::add
        )
        assertEquals("old", database.readText())
        assertEquals("old preferences", prefs.readText())
        assertTrue(messages.single().contains("app.xml"))
        assertFalse(pending.exists())
    }

    @Test fun commitsOnlyAfterVerificationAndRemovesRollbackFiles() {
        val pending = temp.newFolder("pending")
        val target = temp.newFile("user.db").apply { writeText("old") }
        File(pending, target.name).writeText("new")
        val messages = mutableListOf<String>()
        RestoreSwap(pending, listOf(target)).install({}, {}, {
            assertEquals("old", File(pending, target.name + ".bak").readText())
            assertEquals("new", target.readText())
        }, messages::add)
        assertFalse(pending.exists())
        assertFalse(File(pending.path + ".finished").exists())
        assertTrue(messages.single().contains("恢复完成"))
    }
}
