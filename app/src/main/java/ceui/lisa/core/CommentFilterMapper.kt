package ceui.lisa.core

import ceui.lisa.activities.Shaft
import ceui.lisa.helper.CommentFilter
import ceui.lisa.model.ListComment

class CommentFilterMapper : Mapper<ListComment>() {

    override fun apply(listComment: ListComment): ListComment {
        if (Shaft.sSettings.isFilterComment) {
            listComment.comments.removeIf(CommentFilter::judge)
        }

        return listComment
    }
}
