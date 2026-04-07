package ceui.lisa.core

import ceui.lisa.activities.Shaft
import ceui.lisa.model.ListIllust
import ceui.lisa.utils.PixivOperate

class FilterMapper : Mapper<ListIllust>() {

    private var filterStarSize = false
    private var starSizeLimit = 0

    override fun apply(listIllust: ListIllust): ListIllust {
        super.apply(listIllust)
        if (Shaft.sSettings.isDeleteStarIllust) {
            listIllust.illusts = PixivOperate.getListWithoutBooked(listIllust)
        }

        if (filterStarSize && starSizeLimit > 0) {
            listIllust.illusts = PixivOperate.getListWithStarSize(listIllust, starSizeLimit)
        }

        return listIllust
    }

    fun enableFilterStarSize(): FilterMapper {
        filterStarSize = true
        return this
    }

    fun updateStarSizeLimit(starSizeLimit: Int) {
        this.starSizeLimit = starSizeLimit
    }
}
