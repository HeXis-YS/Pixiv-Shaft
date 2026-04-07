package ceui.lisa.model

import ceui.lisa.interfaces.ListShow
import ceui.lisa.models.UserBean

class ListSimpleUser : ListShow<UserBean> {

    var next_url: String? = null
    var users: List<UserBean> = emptyList()

    override val list: List<UserBean>
        get() = users

    override val nextUrl: String?
        get() = next_url
}
