package ceui.lisa.model

import ceui.lisa.interfaces.ListShow
import ceui.lisa.models.WatchlistNovelItem

class ListWatchlistNovel : ListShow<WatchlistNovelItem> {

    var next_url: String? = null
    var series: List<WatchlistNovelItem> = emptyList()

    override val list: List<WatchlistNovelItem>
        get() = series

    override val nextUrl: String?
        get() = next_url
}
