package ceui.lisa.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "illust_recmd_table")
class IllustRecmdEntity {

    @PrimaryKey
    var illustID: Int = 0
    var illustJson: String? = null
    var time: Long = 0

    override fun toString(): String {
        return "IllustHistoryEntity{" +
            "illustID=" + illustID +
            ", illustJson='" + illustJson + '\'' +
            ", time=" + time +
            '}'
    }
}
