package com.scwang.smart.refresh.header

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.scwang.smart.refresh.layout.api.RefreshHeader

open class MaterialHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs), RefreshHeader
