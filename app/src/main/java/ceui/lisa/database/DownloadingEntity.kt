package ceui.lisa.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "illust_downloading_table")
class DownloadingEntity : Serializable {

    @PrimaryKey
    var fileName: String = ""
    var uuid: String = ""
    var taskGson: String? = null

    override fun toString(): String {
        return "DownloadingEntity{" +
            "uuid='" + uuid + '\'' +
            ", taskGson='" + taskGson + '\'' +
            '}'
    }
}
