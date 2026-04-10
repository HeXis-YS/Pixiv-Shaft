package ceui.lisa.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import ceui.lisa.R
import ceui.lisa.databinding.GlareLayoutBinding
import ceui.lisa.utils.Common

class GlareLayout : RelativeLayout {
    private var mContext: Context
    private var baseBind: GlareLayoutBinding
    private var currentState = 0
    private var mListener: OnCheckChangeListener? = null

    constructor(context: Context) : super(context) {
        mContext = context
        baseBind = init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        mContext = context
        baseBind = init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        mContext = context
        baseBind = init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        mContext = context
        baseBind = init()
    }

    private fun init(): GlareLayoutBinding {
        val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = GlareLayoutBinding.inflate(inflater, this, true)
        currentState = 0
        binding.left.setOnClickListener { v ->
            if (currentState != 0) {
                currentState = 0
                check(0)
                mListener?.onSelect(0, v)
            } else {
                mListener?.onReselect(0, v)
            }
        }
        binding.center.setOnClickListener { v ->
            if (currentState != 1) {
                currentState = 1
                check(1)
                mListener?.onSelect(1, v)
            } else {
                mListener?.onReselect(1, v)
            }
        }
        binding.right.setOnClickListener { v ->
            if (currentState != 2) {
                currentState = 2
                check(2)
                mListener?.onSelect(2, v)
            } else {
                mListener?.onReselect(2, v)
            }
        }
        return binding
    }

    private fun check(index: Int) {
        val currentColor = Common.resolveThemeAttribute(mContext, androidx.appcompat.R.attr.colorPrimary)
        if (index == 0) {
            baseBind.left.setTextColor(currentColor)
            baseBind.left.setBackgroundResource(R.drawable.glare_selected)
            unCheck(1)
            unCheck(2)
        } else if (index == 1) {
            baseBind.center.setTextColor(currentColor)
            baseBind.center.setBackgroundResource(R.drawable.glare_selected)
            unCheck(0)
            unCheck(2)
        } else if (index == 2) {
            baseBind.right.setTextColor(currentColor)
            baseBind.right.setBackgroundResource(R.drawable.glare_selected)
            unCheck(0)
            unCheck(1)
        }
    }

    private fun unCheck(index: Int) {
        if (index == 0) {
            baseBind.left.setTextColor(resources.getColor(R.color.glare_unselected_text))
            baseBind.left.background = null
        } else if (index == 1) {
            baseBind.center.setTextColor(resources.getColor(R.color.glare_unselected_text))
            baseBind.center.background = null
        } else if (index == 2) {
            baseBind.right.setTextColor(resources.getColor(R.color.glare_unselected_text))
            baseBind.right.background = null
        }
    }

    fun getListener(): OnCheckChangeListener? = mListener

    fun setListener(listener: OnCheckChangeListener?) {
        mListener = listener
    }
}
