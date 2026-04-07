package ceui.lisa.viewmodel

import ceui.lisa.helper.DeduplicateArrayList
import ceui.lisa.models.IllustsBean

class DynamicIllustModel : BaseModel<IllustsBean>() {
    override fun getContent(): MutableList<IllustsBean> {
        if (contentList == null) {
            contentList = DeduplicateArrayList()
        }
        return contentList!!
    }
}
