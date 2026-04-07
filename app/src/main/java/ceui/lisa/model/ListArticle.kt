package ceui.lisa.model

import ceui.lisa.interfaces.ListShow
import ceui.lisa.models.SpotlightArticlesBean

class ListArticle : ListShow<SpotlightArticlesBean> {

    var next_url: String? = null
    var spotlight_articles: List<SpotlightArticlesBean> = emptyList()

    override val list: List<SpotlightArticlesBean>
        get() = spotlight_articles

    override val nextUrl: String?
        get() = next_url
}
