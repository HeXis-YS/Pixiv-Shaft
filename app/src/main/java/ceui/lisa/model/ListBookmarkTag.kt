package ceui.lisa.model

import ceui.lisa.interfaces.ListShow
import ceui.lisa.models.TagsBean

class ListBookmarkTag : ListShow<TagsBean> {

    var bookmark_detail: BookmarkDetailBean? = null
    var next_url: String? = null

    override val list: List<TagsBean>
        get() = bookmark_detail?.tags ?: emptyList()

    override val nextUrl: String?
        get() = next_url

    class BookmarkDetailBean {
        var is_bookmarked: Boolean = false
        var restrict: String? = null
        var tags: List<TagsBean> = emptyList()
    }
}
