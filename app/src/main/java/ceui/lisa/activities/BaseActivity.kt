package ceui.lisa.activities

import android.content.Context
import android.content.Intent
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import ceui.lisa.R
import ceui.lisa.interfaces.FeedBack
import ceui.lisa.utils.Common
import ceui.lisa.utils.Local
import com.blankj.utilcode.util.BarUtils

abstract class BaseActivity<Layout : ViewDataBinding> : AppCompatActivity() {

    protected lateinit var mContext: Context

    protected lateinit var mActivity: FragmentActivity

    @JvmField
    protected var mLayoutID = 0

    protected lateinit var baseBind: Layout

    @JvmField
    protected var className: String = this.javaClass.simpleName + " "

    private var mFeedBack: FeedBack? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            updateTheme()

            mLayoutID = initLayout()
            mContext = this
            mActivity = this

            val intent = intent
            if (intent != null) {
                val bundle = intent.extras
                if (bundle != null) {
                    initBundle(bundle)
                }
            }

            if (hideStatusBar()) {
                BarUtils.transparentStatusBar(this)
            } else {
                window.statusBarColor = Common.resolveThemeAttribute(
                    mContext,
                    androidx.appcompat.R.attr.colorPrimary,
                )
            }
            try {
                baseBind = DataBindingUtil.setContentView(mActivity, mLayoutID)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            initModel()
            initView()
            initData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    open fun initModel() {
    }

    protected open fun initBundle(bundle: Bundle) {
    }

    protected abstract fun initLayout(): Int

    protected abstract fun initView()

    protected abstract fun initData()

    open fun hideStatusBar(): Boolean = false

    fun gray(gray: Boolean) {
        if (gray) {
            val grayPaint = Paint()
            val colorMatrix = ColorMatrix()
            colorMatrix.setSaturation(0.0f)
            grayPaint.colorFilter = ColorMatrixColorFilter(colorMatrix)
            window.decorView.setLayerType(View.LAYER_TYPE_HARDWARE, grayPaint)
        } else {
            val normalPaint = Paint()
            window.decorView.setLayerType(View.LAYER_TYPE_HARDWARE, normalPaint)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ASK_URI) {
            if (resultCode != RESULT_OK || data == null) {
                return
            }
            val treeUri: Uri? = data.data
            if (treeUri != null) {
                Common.showLog(className + "onActivityResult " + treeUri)
                Shaft.sSettings.rootPathUri = treeUri.toString()
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                mContext.contentResolver.takePersistableUriPermission(treeUri, takeFlags)
                Common.showToast("授权成功！")
                Local.setSettings(Shaft.sSettings)
                doAfterGranted()
            }
        }
    }

    open fun doAfterGranted() {
        mFeedBack?.doSomething()
    }

    fun setFeedBack(feedBack: FeedBack) {
        mFeedBack = feedBack
    }

    protected fun tryParseId(str: String): Long {
        return try {
            str.toLong()
        } catch (ex: Exception) {
            ex.printStackTrace()
            0
        }
    }

    private fun updateTheme() {
        when (Shaft.sSettings.themeIndex) {
            0 -> setTheme(R.style.AppTheme_Index0)
            1 -> setTheme(R.style.AppTheme_Index1)
            2 -> setTheme(R.style.AppTheme_Index2)
            3 -> setTheme(R.style.AppTheme_Index3)
            4 -> setTheme(R.style.AppTheme_Index4)
            5 -> setTheme(R.style.AppTheme_Index5)
            6 -> setTheme(R.style.AppTheme_Index6)
            7 -> setTheme(R.style.AppTheme_Index7)
            8 -> setTheme(R.style.AppTheme_Index8)
            9 -> setTheme(R.style.AppTheme_Index9)
            else -> setTheme(R.style.AppTheme_Default)
        }
    }

    companion object {
        const val ASK_URI = 42

        @JvmStatic
        fun newInstance(intent: Intent, context: Context) {
            context.startActivity(intent)
        }
    }
}
