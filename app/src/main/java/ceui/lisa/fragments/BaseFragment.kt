package ceui.lisa.fragments

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding
import ceui.refactor.ViewBindingCompat
import java.util.UUID

abstract class BaseFragment<Layout : ViewBinding> : Fragment() {

    protected var rootView: View? = null
    protected lateinit var baseBind: Layout
    protected var className: String = javaClass.simpleName + " "
    protected var mLayoutID: Int = -1
    protected lateinit var mActivity: FragmentActivity
    protected lateinit var mContext: Context
    private var isVertical = false
    @JvmField
    protected var isInit = false
    protected var uuid: String = UUID.randomUUID().toString()

    init {
        Log.d(className, " newInstance $uuid")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            mActivity = requireActivity()
            mContext = requireContext()

            val fragmentBundle = arguments
            if (fragmentBundle != null) {
                initBundle(fragmentBundle)
            }

            val activityIntent: Intent? = mActivity.intent
            if (activityIntent != null) {
                val activityBundle = activityIntent.extras
                if (activityBundle != null) {
                    initActivityBundle(activityBundle)
                }
            }

            initModel()

            val resources = resources
            if (resources != null) {
                when (resources.configuration.orientation) {
                    Configuration.ORIENTATION_LANDSCAPE -> isVertical = false
                    Configuration.ORIENTATION_PORTRAIT -> isVertical = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        try {
            isInit = true
            rootView?.let { cachedRoot ->
                if (!this::baseBind.isInitialized) {
                    baseBind = ViewBindingCompat.bind(
                        javaClass,
                        BaseFragment::class.java,
                        0,
                        cachedRoot,
                    )
                }
                return cachedRoot
            }

            initLayout()

            if (mLayoutID != -1) {
                baseBind = ViewBindingCompat.inflate(
                    javaClass,
                    BaseFragment::class.java,
                    0,
                    inflater,
                    container,
                    false,
                )
                rootView = baseBind.root
                initView()
                initData()
                return rootView
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        try {
            rootView?.tag = uuid
            if (isVertical) {
                vertical()
            } else {
                horizon()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected abstract fun initLayout()

    protected open fun initBundle(bundle: Bundle) {
    }

    protected open fun initActivityBundle(bundle: Bundle) {
    }

    protected open fun initView() {
    }

    protected open fun initData() {
    }

    open fun horizon() {
    }

    open fun vertical() {
    }

    fun finish() {
        if (this::mActivity.isInitialized) {
            mActivity.finish()
        }
    }

    open fun initModel() {
    }

    protected fun tryParseId(str: String): Long {
        return try {
            str.toLong()
        } catch (ex: Exception) {
            ex.printStackTrace()
            0
        }
    }
}
