package ceui.lisa.utils

import android.content.res.Resources
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import ceui.lisa.models.UserBean

object UserFolderNameUtil {

    private val resources: Resources = Shaft.getContext().resources

    @JvmField
    val USER_FOLDER_NAME_NAMES = arrayOf(
        resources.getString(R.string.string_445),
        resources.getString(R.string.string_446) + "_" + resources.getString(R.string.string_447),
        resources.getString(R.string.string_447) + "_" + resources.getString(R.string.string_446),
        resources.getString(R.string.string_446),
        resources.getString(R.string.string_447)
    )

    @JvmStatic
    fun getCurrentStatusName(): String {
        var currentIndex = Shaft.sSettings.getSaveForSeparateAuthorStatus()
        if (currentIndex < 0 || currentIndex >= USER_FOLDER_NAME_NAMES.size) {
            currentIndex = 0
        }
        return USER_FOLDER_NAME_NAMES[currentIndex]
    }

    @JvmStatic
    fun getFolderNameForUser(userBean: UserBean): String {
        var currentIndex = Shaft.sSettings.getSaveForSeparateAuthorStatus()
        if (currentIndex < 0 || currentIndex >= USER_FOLDER_NAME_NAMES.size) {
            currentIndex = 0
        }
        return when (currentIndex) {
            1 -> userBean.name + "_" + userBean.id
            2 -> userBean.id.toString() + "_" + userBean.name
            3 -> userBean.name
            4 -> userBean.id.toString()
            else -> ""
        }
    }
}
