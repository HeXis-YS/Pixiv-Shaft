package ceui.lisa.model

import ceui.lisa.interfaces.ListShow
import ceui.lisa.models.IllustsBean
import java.io.Serializable

open class ListIllust : ListShow<IllustsBean>, Serializable {

    var next_url: String? = null
    var illusts: List<IllustsBean> = emptyList()

    override val list: List<IllustsBean>
        get() = illusts

    override val nextUrl: String?
        get() = next_url
}
