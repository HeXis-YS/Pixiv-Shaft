package ceui.lisa.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "upload_image_table")
class ImageEntity {

    @PrimaryKey
    var id: Int = 0
    var fileName: String? = null
    var filePath: String? = null
    var uploadTime: Long = 0

    override fun toString(): String {
        return "ImageEntity{" +
            "id=" + id +
            ", fileName='" + fileName + '\'' +
            ", filePath='" + filePath + '\'' +
            ", uploadTime=" + uploadTime +
            '}'
    }
}
