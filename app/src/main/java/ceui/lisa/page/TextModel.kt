package ceui.lisa.page

class TextModel {

    /**
     * 内容
     */
    @JvmField
    var text: String? = null

    /**
     * 字体大小(sp)
     */
    @JvmField
    var textSize: Float = 0f

    /**
     * 字体是否加粗
     */
    @JvmField
    var fakeBoldText: Boolean = false

    /**
     * 内容长度
     */
    @JvmField
    var textLength: Int = 0

    /**
     * 内容高度
     */
    @JvmField
    var height: Int = 0

    /**
     * 是否顶部标题
     */
    @JvmField
    var isChapter: Boolean = false

    /**
     * 是否标题
     */
    @JvmField
    var isTitle: Boolean = false

    /**
     * 段落首行
     */
    @JvmField
    var partFirstLine: Boolean = false
}
