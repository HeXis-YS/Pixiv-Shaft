package ceui.lisa.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "illust_download_table")
class DownloadEntity : Serializable {

    @PrimaryKey
    var fileName: String = ""
    var filePath: String = ""
    var taskGson: String? = null
    var illustGson: String? = null
    var downloadTime: Long = 0

    override fun toString(): String {
        return "DownloadEntity{" +
            ", taskGson='" + taskGson + '\'' +
            ", illustGson='" + illustGson + '\'' +
            ", downloadTime=" + downloadTime +
            '}'
    }
}
