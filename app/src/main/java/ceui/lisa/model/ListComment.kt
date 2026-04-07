package ceui.lisa.model

import ceui.lisa.interfaces.ListShow
import ceui.lisa.models.ReplyCommentBean

class ListComment : ListShow<ReplyCommentBean> {

    var comments: MutableList<ReplyCommentBean> = mutableListOf()
    var next_url: String? = null
    var total_comments: Int = 0

    override val list: List<ReplyCommentBean>
        get() = comments

    override val nextUrl: String?
        get() = next_url
}
