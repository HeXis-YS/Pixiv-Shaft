package ceui.lisa.refresh.header

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import ceui.lisa.refresh.layout.api.RefreshHeader

open class FalsifyHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs), RefreshHeader
