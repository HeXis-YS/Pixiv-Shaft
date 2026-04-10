package ceui.lisa.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.view.Gravity
import androidx.annotation.NonNull
import ceui.lisa.R
import ceui.lisa.feature.ToastStyle
import ceui.lisa.helper.ShortcutHelper
import ceui.lisa.helper.ThemeHelper
import ceui.lisa.models.UserModel
import ceui.lisa.notification.NetWorkStateReceiver
import ceui.lisa.utils.DensityUtil
import ceui.lisa.utils.Local
import ceui.lisa.utils.Settings
import ceui.lisa.view.MyDeliveryHeader
import ceui.lisa.viewmodel.AppLevelViewModel
import com.billy.android.swipe.SmartSwipeBack
import com.google.gson.Gson
import com.hjq.toast.ToastUtils
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.tencent.mmkv.MMKV
import me.jessyan.progressmanager.ProgressManager
import okhttp3.OkHttpClient

class Shaft : Application() {

    protected var netWorkStateReceiver: NetWorkStateReceiver? = null
    private var okHttpClient: OkHttpClient? = null

    override fun onCreate() {
        super.onCreate()

        sContext = this
        sGson = Gson()

        sPreferences = getSharedPreferences(Local.LOCAL_DATA, Context.MODE_PRIVATE)

        MMKV.initialize(this)

        sUserModel = Local.getUser()
        sSettings = Local.getSettings()

        updateTheme()
        ThemeHelper.applyTheme(null, sSettings.getThemeType())

        okHttpClient = ProgressManager.getInstance().with(OkHttpClient.Builder()).build()

        statusHeight = 0
        val resourceId = sContext.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusHeight = sContext.resources.getDimensionPixelSize(resourceId)
        }
        toolbarHeight = DensityUtil.dp2px(56.0f)

        if (netWorkStateReceiver == null) {
            netWorkStateReceiver = NetWorkStateReceiver()
        }

        ToastUtils.init(this)
        ToastUtils.setGravity(Gravity.BOTTOM, 0, 0)
        ToastUtils.initStyle(ToastStyle(this))

        val filter = IntentFilter()
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(netWorkStateReceiver, filter)

        if (sSettings.isGlobalSwipeBack()) {
            SmartSwipeBack.activitySlidingBack(this) { activity: Activity ->
                activity !is MainActivity
            }
        }

        ShortcutHelper.addAppShortcuts()

        appViewModel = AppLevelViewModel(this)
    }

    fun getOkHttpClient(): OkHttpClient {
        return okHttpClient!!
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

    override fun unbindService(conn: ServiceConnection) {
        try {
            super.unbindService(conn)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onConfigurationChanged(@NonNull newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        when (newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO,
            Configuration.UI_MODE_NIGHT_YES -> MyDeliveryHeader.changeCloudColor(getContext())
        }
    }

    companion object {
        init {
            SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, _ ->
                ClassicsHeader(context)
            }

            SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ ->
                ClassicsFooter(context).setDrawableSize(20f)
            }
        }

        @JvmField
        var sUserModel: UserModel = UserModel()

        @JvmField
        var sSettings: Settings = Settings()

        @JvmField
        var sGson: Gson = Gson()

        lateinit var sPreferences: SharedPreferences

        lateinit var appViewModel: AppLevelViewModel

        @JvmField
        var statusHeight = 0

        @JvmField
        var toolbarHeight = 0

        private var mmkv: MMKV? = null

        @SuppressLint("StaticFieldLeak")
        private lateinit var sContext: Context

        @JvmStatic
        fun getContext(): Context {
            return sContext
        }

        @JvmStatic
        fun getMMKV(): MMKV {
            if (mmkv == null) {
                mmkv = MMKV.defaultMMKV()
            }
            return mmkv!!
        }

        @JvmStatic
        fun hasLoginUser(): Boolean {
            val user = sUserModel.user
            return user != null && user.isIs_login
        }
    }
}
