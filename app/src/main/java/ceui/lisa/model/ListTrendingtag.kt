package ceui.lisa.model

import ceui.lisa.interfaces.ListShow
import ceui.lisa.models.IllustsBean
import java.io.Serializable

class ListTrendingtag : ListShow<ListTrendingtag.TrendTagsBean>, Serializable {

    var trend_tags: List<TrendTagsBean>? = null
    var tags: List<TrendTagsBean>? = null

    override val list: List<TrendTagsBean>
        get() = if (!trend_tags.isNullOrEmpty()) {
            trend_tags!!
        } else {
            tags ?: emptyList()
        }

    override val nextUrl: String?
        get() = null

    class TrendTagsBean : Serializable {
        var name: String? = null
        var translated_name: String? = null
        var illust: IllustsBean? = null

        private var tag: String? = null

        fun getTag(): String {
            return when {
                !tag.isNullOrEmpty() -> tag.orEmpty()
                !name.isNullOrEmpty() -> name.orEmpty()
                else -> ""
            }
        }

        fun setTag(tag: String?) {
            this.tag = tag
        }
    }
}
