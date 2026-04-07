package ceui.lisa.model

import ceui.lisa.interfaces.ListShow
import ceui.lisa.models.IllustsBean
import ceui.lisa.models.ImageUrlsBean
import ceui.lisa.models.UserBean

class ListMangaOfSeries : ListShow<IllustsBean> {

    var illust_series_first_illust: IllustsBean? = null
    var illusts: List<IllustsBean> = emptyList()
    var next_url: String? = null
    var illust_series_detail: SeriesDetail? = null

    override val list: List<IllustsBean>
        get() = illusts

    override val nextUrl: String?
        get() = next_url

    class SeriesDetail {
        var id: Int = 0
        var series_work_count: Int = 0
        var width: Int = 0
        var height: Int = 0
        var title: String? = null
        var create_date: String? = null
        var caption: String? = null
        var user: UserBean? = null
        var cover_image_urls: ImageUrlsBean? = null
    }
}
