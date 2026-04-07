package ceui.lisa.database

import androidx.room.Entity

@Entity(tableName = "tag_mute_table", primaryKeys = ["id", "type"])
class MuteEntity {

    var id: Int = 0
    var tagJson: String? = null
    var searchTime: Long = 0
    var type: Int = 0

    override fun toString(): String {
        return "TagMuteEntity{" +
            "id=" + id +
            ", tagJson='" + tagJson + '\'' +
            ", searchTime=" + searchTime +
            ", type=" + type +
            '}'
    }
}
