package ceui.lisa.model

import ceui.lisa.interfaces.ListShow
import ceui.lisa.models.NovelBean
import ceui.lisa.models.NovelSeriesItem

class ListNovelOfSeries : ListShow<NovelBean> {

    var novel_series_detail: NovelSeriesItem? = null
    var novel_series_first_novel: NovelBean? = null
    var novel_series_latest_novel: NovelBean? = null
    var next_url: String? = null
    var novels: List<NovelBean> = emptyList()

    override val list: List<NovelBean>
        get() = novels

    override val nextUrl: String?
        get() = next_url
}
