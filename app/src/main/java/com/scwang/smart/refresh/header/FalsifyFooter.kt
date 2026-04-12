package com.scwang.smart.refresh.header

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.scwang.smart.refresh.layout.api.RefreshFooter

open class FalsifyFooter @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs), RefreshFooter
