package ceui.lisa.viewmodel

import ceui.lisa.models.IllustsBean

class RecmdModel : BaseModel<IllustsBean>() {
    private val rankList: List<IllustsBean> = ArrayList()

    fun getRankList(): List<IllustsBean> {
        return rankList
    }
}
