package ceui.lisa.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment

abstract class BaseDialog<Layout : ViewDataBinding> : DialogFragment() {

    @JvmField
    protected var mContext: Context? = null

    @JvmField
    protected var mActivity: Activity? = null

    @JvmField
    protected var baseBind: Layout? = null

    @JvmField
    protected var mLayoutID = -1

    @JvmField
    protected var parentView: View? = null

    @JvmField
    protected var className: String = this.javaClass.simpleName + " "

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mContext = requireContext()
        mActivity = requireActivity()

        val bundle = arguments
        if (bundle != null) {
            initBundle(bundle)
        }
    }

    open fun initBundle(bundle: Bundle) {
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        initLayout()
        baseBind = DataBindingUtil.inflate(inflater, mLayoutID, container, false)
        parentView = if (baseBind != null) {
            baseBind!!.root
        } else {
            inflater.inflate(mLayoutID, container, false)
        }
        return parentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView(view)
        initData()
    }

    override fun onResume() {
        super.onResume()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            val window: Window? = dialog.window
            if (window != null) {
                val lp: WindowManager.LayoutParams = window.attributes
                lp.width = resources.displayMetrics.widthPixels * 6 / 7
                window.attributes = lp
            }
        }
    }

    protected abstract fun initLayout()

    protected abstract fun initView(v: View)

    protected abstract fun initData()
}
