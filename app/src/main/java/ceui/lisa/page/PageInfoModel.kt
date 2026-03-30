package ceui.lisa.page

class PageInfoModel {
    @JvmField
    var pages: Int = 0

    @JvmField
    var title: String? = null

    @JvmField
    var lisText: MutableList<TextModel> = ArrayList()
}
