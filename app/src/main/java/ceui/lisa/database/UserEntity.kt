package ceui.lisa.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import ceui.lisa.models.UserModel

@Entity(tableName = "user_table")
class UserEntity {

    @PrimaryKey
    var userID: Int = 0
    var userGson: String? = null
    var loginTime: Long = 0

    override fun toString(): String {
        return "UserEntity{" +
            "userID=" + userID +
            ", userGson='" + userGson + '\'' +
            ", loginTime=" + loginTime +
            '}'
    }

    fun getUser(gson: Gson): UserModel {
        return gson.fromJson(userGson, UserModel::class.java)
    }
}
