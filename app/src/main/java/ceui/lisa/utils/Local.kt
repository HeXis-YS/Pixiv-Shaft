package ceui.lisa.utils

import ceui.lisa.activities.Shaft
import ceui.lisa.models.UserModel

object Local {

    @JvmField
    val LOCAL_DATA = "local_data"

    @JvmField
    val USER = "user"

    @JvmField
    val SETTINGS = "settings"

    @JvmStatic
    fun saveUser(userModel: UserModel?) {
        if (userModel != null) {
            val userString = Shaft.sGson.toJson(userModel, UserModel::class.java)
            val editor = Shaft.sPreferences.edit()
            editor.putString(USER, userString)
            if (editor.commit()) {
                Shaft.sUserModel = userModel
            }
        }
    }

    @JvmStatic
    fun getUser(): UserModel {
        return Shaft.sGson.fromJson(
            Shaft.sPreferences.getString(USER, ""),
            UserModel::class.java
        )
    }

    @JvmStatic
    fun getSettings(): Settings {
        val settingsString = Shaft.sPreferences.getString(SETTINGS, "")
        val settings = Shaft.sGson.fromJson(settingsString, Settings::class.java)
        return settings ?: Settings()
    }

    @JvmStatic
    fun setSettings(settings: Settings) {
        val settingsGson = Shaft.sGson.toJson(settings)
        val editor = Shaft.sPreferences.edit()
        editor.putString(SETTINGS, settingsGson)
        editor.apply()
        Shaft.sSettings = settings
    }
}
