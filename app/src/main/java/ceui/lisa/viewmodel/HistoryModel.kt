package ceui.lisa.viewmodel

import ceui.lisa.database.IllustHistoryEntity
import ceui.lisa.models.IllustsBean

class HistoryModel : BaseModel<IllustHistoryEntity>() {
    private var all: List<IllustsBean> = ArrayList()

    fun getAll(): List<IllustsBean> {
        return all
    }

    fun setAll(all: List<IllustsBean>) {
        this.all = all
    }
}
