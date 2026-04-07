package ceui.lisa.model

import ceui.lisa.interfaces.ListShow
import ceui.lisa.models.Live

class ListLive : ListShow<Live> {

    var live_info: Any? = null
    var next_url: String? = null
    var lives: List<Live> = emptyList()

    override val list: List<Live>
        get() = lives

    override val nextUrl: String?
        get() = next_url
}
