package ceui.lisa.database

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.reflect.TypeToken
import ceui.lisa.activities.Shaft
import ceui.lisa.core.IDWithList
import ceui.lisa.models.IllustsBean

@Entity(tableName = "uuid_list_table")
class UUIDEntity : IDWithList<IllustsBean> {

    @get:JvmName("getUuidValue")
    @set:JvmName("setUuidValue")
    @PrimaryKey
    var uuid: String = ""

    var listJson: String? = null

    @Ignore
    fun getUuid(): String {
        return uuid
    }

    @Ignore
    fun setUuid(value: String?) {
        if (value != null) {
            uuid = value
        }
    }

    override fun toString(): String {
        return "UUIDEntity{" +
            "uuid='" + uuid + '\'' +
            ", listJson='" + listJson + '\'' +
            '}'
    }

    override fun getUUID(): String {
        return uuid
    }

    override fun getList(): List<IllustsBean> {
        return Shaft.sGson.fromJson(listJson, object : TypeToken<List<IllustsBean>>() {}.type)
    }
}
