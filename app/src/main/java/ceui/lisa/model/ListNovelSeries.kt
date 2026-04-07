package ceui.lisa.model

import ceui.lisa.interfaces.ListShow
import ceui.lisa.models.NovelSeriesItem

class ListNovelSeries : ListShow<NovelSeriesItem> {

    var next_url: String? = null
    var novel_series_details: List<NovelSeriesItem> = emptyList()

    override val list: List<NovelSeriesItem>
        get() = novel_series_details

    override val nextUrl: String?
        get() = next_url
}
