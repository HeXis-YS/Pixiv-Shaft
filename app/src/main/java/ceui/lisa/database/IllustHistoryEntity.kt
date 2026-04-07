package ceui.lisa.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "illust_table")
class IllustHistoryEntity {

    @PrimaryKey
    var illustID: Int = 0
    var illustJson: String? = null
    var time: Long = 0
    var type: Int = 0

    override fun toString(): String {
        return "IllustHistoryEntity{" +
            "illustID=" + illustID +
            ", illustJson='" + illustJson + '\'' +
            ", time=" + time +
            ", type=" + type +
            '}'
    }
}
