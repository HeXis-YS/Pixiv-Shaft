package ceui.lisa.model

import ceui.lisa.interfaces.ListShow
import ceui.lisa.models.UserPreviewsBean

class ListUser : ListShow<UserPreviewsBean> {

    var next_url: String? = null
    var user_previews: List<UserPreviewsBean>? = null

    override val list: List<UserPreviewsBean>
        get() = user_previews ?: emptyList()

    override val nextUrl: String?
        get() = next_url
}
