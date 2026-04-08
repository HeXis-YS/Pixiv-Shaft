package ceui.lisa.page

class ReadSettingInfo {
    @JvmField
    var lightType: Int = ConstantPageInfo.lightType

    @JvmField
    var lightValue: Int = 0

    @JvmField
    var frontSize: Float = ConstantPageInfo.textSize

    @JvmField
    var frontColor: Int = ConstantPageInfo.textColor

    @JvmField
    var lineSpacingExtra: Int = UtilityMeasure.getLineSpacingExtra(frontSize)

    @JvmField
    var pageAnimType: PageMode = PageMode.SIMULATION
}
