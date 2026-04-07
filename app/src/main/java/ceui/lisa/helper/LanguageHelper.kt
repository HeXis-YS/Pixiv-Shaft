package ceui.lisa.helper

import ceui.lisa.activities.Shaft
import ceui.lisa.utils.Settings

object LanguageHelper {
    @JvmStatic
    fun getRequestHeaderAcceptLanguageFromAppLanguage(): String {
        val allLanguages = Settings.ALL_LANGUAGE
        val currentLanguage = Shaft.sSettings.appLanguage
        val index = allLanguages.indexOf(currentLanguage)

        return when (index) {
            0 -> "zh_CN"
            1 -> "ja"
            3 -> "zh_TW"
            5 -> "ko"
            else -> "en"
        }
    }
}
