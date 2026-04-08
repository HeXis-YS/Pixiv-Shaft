package ceui.lisa.database

import org.junit.Assert.assertEquals
import org.junit.Test

class UUIDEntityTest {

    @Test
    fun setUuid_preservesOldJavaSemantics() {
        val entity = UUIDEntity()

        entity.uuid = "origin"
        entity.setUuid(null)
        assertEquals("origin", entity.uuid)

        entity.uuid = ""
        assertEquals("", entity.uuid)
    }
}
