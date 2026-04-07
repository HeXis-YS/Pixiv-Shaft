package ceui.lisa.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.reflect.TypeToken
import ceui.lisa.activities.Shaft
import ceui.lisa.core.IDWithList
import ceui.lisa.models.IllustsBean

@Entity(tableName = "uuid_list_table")
class UUIDEntity : IDWithList<IllustsBean> {

    @PrimaryKey
    var uuid: String = ""
        set(value) {
            if (value.isNotEmpty()) {
                field = value
            }
        }

    var listJson: String? = null

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
