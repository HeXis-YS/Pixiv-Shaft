package ceui.lisa.model

import ceui.lisa.interfaces.ListShow
import ceui.lisa.models.TagsBean

class ListTag : ListShow<TagsBean> {

    var next_url: String? = null
    var bookmark_tags: List<TagsBean>? = null

    override val list: List<TagsBean>
        get() = bookmark_tags ?: emptyList()

    override val nextUrl: String?
        get() = next_url
}
