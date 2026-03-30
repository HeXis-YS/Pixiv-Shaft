package ceui.lisa.utils

import android.content.res.Resources
import android.util.Patterns
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import java.util.regex.Pattern

class SearchTypeUtil private constructor() {
    companion object {
        private val resources: Resources = Shaft.getContext().resources
        const val defaultSearchType = 5

        private val WEB_URL_PATTERN = Patterns.WEB_URL
        private val NUMBERIC_PATTERN = Pattern.compile("(?:\\b|\\D)([1-9]\\d{3,9})(?:\\b|\\D)")

        @JvmField
        val SEARCH_TYPE_NAME = arrayOf(
            resources.getString(R.string.string_430),
            resources.getString(R.string.string_150),
            resources.getString(R.string.string_152),
            resources.getString(R.string.string_153),
            resources.getString(R.string.string_341),
            resources.getString(R.string.string_431)
        )

        const val SEARCH_TYPE_DB_KEYWORD = 0
        const val SEARCH_TYPE_DB_ILLUSTSID = 1
        const val SEARCH_TYPE_DB_USERKEYWORD = 2
        const val SEARCH_TYPE_DB_USERID = 3
        const val SEARCH_TYPE_DB_NOVELID = 4
        const val SEARCH_TYPE_DB_URL = 5

        @JvmStatic
        fun getSuggestSearchType(content: String?): Int {
            return try {
                if (content.isNullOrEmpty()) {
                    return defaultSearchType
                }

                if (WEB_URL_PATTERN.matcher(content).matches()) {
                    return 4
                }

                val matcher = NUMBERIC_PATTERN.matcher(content)
                if (matcher.find()) {
                    val number = matcher.group(1)?.toLongOrNull() ?: return defaultSearchType
                    if (number > 10000000L) {
                        1
                    } else {
                        2
                    }
                } else {
                    defaultSearchType
                }
            } catch (e: Exception) {
                e.printStackTrace()
                defaultSearchType
            }
        }
    }
}
