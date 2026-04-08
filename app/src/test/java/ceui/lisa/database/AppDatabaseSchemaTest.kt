package ceui.lisa.database

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.File

class AppDatabaseSchemaTest {

    @Test
    fun nullableColumnsStayCompatibleWithExistingRoomSchema() {
        val root = locateProjectRoot()
        val schemaFile = File(root, "app/schemas/ceui.lisa.database.AppDatabase/25.json")
        val schema = JsonParser.parseReader(schemaFile.reader()).asJsonObject
        val database = schema.getAsJsonObject("database")

        val downloadTable = findEntity(database, "illust_download_table")
        val downloadingTable = findEntity(database, "illust_downloading_table")

        assertFalse(findField(downloadTable, "filePath").get("notNull").asBoolean)
        assertFalse(findField(downloadingTable, "uuid").get("notNull").asBoolean)
    }

    private fun locateProjectRoot(): File {
        var current: File? = File(System.getProperty("user.dir"))
        while (current != null) {
            if (File(current, "app/schemas").isDirectory) {
                return current
            }
            current = current.parentFile
        }
        error("Unable to locate project root containing app/schemas")
    }

    private fun findEntity(database: JsonObject, tableName: String): JsonObject {
        val entity =
            database.getAsJsonArray("entities").firstOrNull {
                it.asJsonObject.get("tableName").asString == tableName
            }?.asJsonObject
        assertNotNull("Missing schema entity for $tableName", entity)
        return entity!!
    }

    private fun findField(entity: JsonObject, columnName: String): JsonObject {
        val field =
            entity.getAsJsonArray("fields").firstOrNull {
                it.asJsonObject.get("columnName").asString == columnName
            }?.asJsonObject
        assertNotNull("Missing schema field $columnName", field)
        return field!!
    }
}
