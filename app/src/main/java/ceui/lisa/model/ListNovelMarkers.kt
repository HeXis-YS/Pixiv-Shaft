package ceui.lisa.model

import ceui.lisa.interfaces.ListShow
import ceui.lisa.models.MarkedNovelItem

class ListNovelMarkers : ListShow<MarkedNovelItem> {

    var next_url: String? = null
    var marked_novels: List<MarkedNovelItem> = emptyList()

    override val list: List<MarkedNovelItem>
        get() = marked_novels

    override val nextUrl: String?
        get() = next_url
}
