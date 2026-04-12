package com.scwang.smart.refresh.header

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.FrameLayout
import com.scwang.smart.refresh.layout.api.RefreshFooter

open class ClassicsFooter @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs), RefreshFooter {

    fun setDrawableSize(size: Float): ClassicsFooter = this

    fun setPrimaryColor(color: Int): ClassicsFooter = this

    fun setPrimaryColors(vararg colors: Int): ClassicsFooter = this
}
