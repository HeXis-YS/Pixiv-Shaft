package ceui.lisa.helper

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import ceui.lisa.R

class ThemeHelper {
    enum class ThemeType(
        @JvmField val themeTypeIndex: Int,
        private val themeTypeNameResId: Int,
    ) {
        DEFAULT_MODE(0, R.string.string_298),
        LIGHT_MODE(1, R.string.string_299),
        DARK_MODE(2, R.string.string_300);

        override fun toString(): String {
            return themeTypeIndex.toString()
        }

        fun toDisplayString(context: Context): String {
            return context.getString(themeTypeNameResId)
        }
    }

    companion object {
        @JvmStatic
        fun applyTheme(activity: AppCompatActivity?, themePref: ThemeType) {
            when (themePref) {
                ThemeType.LIGHT_MODE -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    activity?.delegate?.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }

                ThemeType.DARK_MODE -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    activity?.delegate?.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }

                ThemeType.DEFAULT_MODE -> {
                    val nightMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    } else {
                        AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                    }
                    AppCompatDelegate.setDefaultNightMode(nightMode)
                    activity?.delegate?.setLocalNightMode(nightMode)
                }
            }
        }
    }
}
