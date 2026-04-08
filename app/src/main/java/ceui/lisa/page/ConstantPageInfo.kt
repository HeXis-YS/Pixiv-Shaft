package ceui.lisa.page

import ceui.lisa.R

class ConstantPageInfo {
    companion object {
        @JvmField
        var lightType: Int = ConstantSetting.LIGHTTYPE_2

        @JvmField
        var textSize: Float = 18f

        @JvmField
        var tipTextSize: Float = 13f

        @JvmField
        var timeTextSize: Float = 13f

        @JvmField
        var processTextSize: Float = 13f

        @JvmField
        var textColor: Int = R.color.black_to_grey
    }
}
