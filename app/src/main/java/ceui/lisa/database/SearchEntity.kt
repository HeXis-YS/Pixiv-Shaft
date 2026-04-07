package ceui.lisa.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_table")
class SearchEntity {

    @PrimaryKey
    var id: Int = 0
    var keyword: String? = null
    var searchTime: Long = 0
    var searchType: Int = 0
    @ColumnInfo(name = "pinned")
    var isPinned: Boolean = false

    override fun toString(): String {
        return "SearchEntity{" +
            "id=" + id +
            ", keyword='" + keyword + '\'' +
            ", searchTime=" + searchTime +
            ", searchType=" + searchType +
            ", pinned=" + isPinned +
            '}'
    }
}
