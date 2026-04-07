package ceui.lisa.model

import ceui.lisa.interfaces.ListShow
import ceui.lisa.models.MangaSeriesItem

class ListMangaSeries : ListShow<MangaSeriesItem> {

    var next_url: String? = null
    var illust_series_details: List<MangaSeriesItem> = emptyList()

    override val list: List<MangaSeriesItem>
        get() = illust_series_details

    override val nextUrl: String?
        get() = next_url
}
