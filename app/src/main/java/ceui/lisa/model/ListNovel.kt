package ceui.lisa.model

import ceui.lisa.interfaces.ListShow
import ceui.lisa.models.NovelBean
import java.io.Serializable

class ListNovel : ListShow<NovelBean>, Serializable {

    var next_url: String? = null
    var novels: List<NovelBean>? = null
    var ranking_novels: List<NovelBean>? = null

    override val list: List<NovelBean>
        get() = novels ?: emptyList()

    override val nextUrl: String?
        get() = next_url
}
