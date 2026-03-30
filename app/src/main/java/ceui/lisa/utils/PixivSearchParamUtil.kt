package ceui.lisa.utils

import android.content.res.Resources
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import java.util.Arrays

class PixivSearchParamUtil private constructor() {
    companion object {
        private val resources: Resources = Shaft.getContext().resources

        const val POPULAR_SORT_VALUE = "popular_desc"

        @JvmField
        val TAG_MATCH_VALUE = arrayOf(
            "partial_match_for_tags",
            "exact_match_for_tags",
            "title_and_caption"
        )

        @JvmField
        val TAG_MATCH_VALUE_NOVEL = arrayOf(
            "partial_match_for_tags",
            "exact_match_for_tags",
            "text",
            "keyword"
        )

        @JvmField
        val ALL_SIZE_VALUE = arrayOf(
            "",
            "500users入り",
            "1000users入り",
            "2000users入り",
            "5000users入り",
            "7500users入り",
            "10000users入り",
            "20000users入り",
            "50000users入り",
            "100000users入り"
        )

        @JvmField
        val SORT_TYPE_VALUE = arrayOf("date_desc", "date_asc", POPULAR_SORT_VALUE)

        @JvmField
        val R18_RESTRICTION_VALUE = arrayOf("", "-R-18", "R-18")

        @JvmField
        val TAG_MATCH_NAME = arrayOf(
            resources.getString(R.string.string_284),
            resources.getString(R.string.string_285),
            resources.getString(R.string.string_286)
        )

        @JvmField
        val TAG_MATCH_NAME_NOVEL = arrayOf(
            resources.getString(R.string.string_284),
            resources.getString(R.string.string_285),
            resources.getString(R.string.string_394),
            resources.getString(R.string.string_395)
        )

        @JvmField
        val ALL_SIZE_NAME = arrayOf(
            resources.getString(R.string.string_289),
            resources.getString(R.string.string_290),
            resources.getString(R.string.string_291),
            resources.getString(R.string.string_292),
            resources.getString(R.string.string_293),
            resources.getString(R.string.string_294),
            resources.getString(R.string.string_295),
            resources.getString(R.string.string_296),
            resources.getString(R.string.string_297),
            resources.getString(R.string.string_375)
        )

        @JvmField
        val SORT_TYPE_NAME = arrayOf(
            resources.getString(R.string.string_287),
            resources.getString(R.string.string_288),
            resources.getString(R.string.string_64_1)
        )

        @JvmField
        val R18_RESTRICTION_NAME = arrayOf(
            resources.getString(R.string.string_289),
            resources.getString(R.string.string_440),
            resources.getString(R.string.string_441)
        )

        @JvmStatic
        fun getSizeIndex(sizeFilterValue: String?): Int {
            val index = Arrays.asList(*ALL_SIZE_VALUE).indexOf(sizeFilterValue)
            return maxOf(index, 0)
        }

        @JvmStatic
        fun getSizeName(sizeFilterValue: String?): String {
            return ALL_SIZE_NAME[getSizeIndex(sizeFilterValue)]
        }

        @JvmStatic
        fun getSortTypeIndex(sortTypeValue: String?): Int {
            val index = Arrays.asList(*SORT_TYPE_VALUE).indexOf(sortTypeValue)
            return if (index < 0) 2 else index
        }

        @JvmStatic
        fun getSortTypeName(sortTypeValue: String?): String {
            return SORT_TYPE_NAME[getSortTypeIndex(sortTypeValue)]
        }
    }
}
