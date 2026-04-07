package ceui.lisa.helper

import android.content.res.Resources
import androidx.fragment.app.Fragment
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import ceui.lisa.fragments.FragmentCenter
import ceui.lisa.fragments.FragmentLeft
import ceui.lisa.fragments.FragmentRight
import ceui.lisa.fragments.FragmentViewPager
import ceui.lisa.utils.Params
import java.util.LinkedHashMap

class NavigationLocationHelper {
    class NavigationItem(
        private val titleResId: Int,
        private val iconResId: Int,
        private val instanceClass: Class<*>?,
    ) {
        fun getTitleResId(): Int {
            return titleResId
        }

        fun getIconResId(): Int {
            return iconResId
        }

        fun getInstanceClass(): Class<*>? {
            return instanceClass
        }

        fun getFragment(): Fragment {
            return when (instanceClass) {
                null -> Fragment()
                FragmentLeft::class.java -> FragmentLeft()
                FragmentCenter::class.java -> FragmentCenter()
                FragmentRight::class.java -> FragmentRight()
                FragmentViewPager::class.java -> FragmentViewPager.newInstance(Params.VIEW_PAGER_R18)
                else -> Fragment()
            }
        }
    }

    companion object {
        private val resources: Resources = Shaft.getContext().resources

        const val LATEST = "LATEST"
        const val TUIJIAN = "TUIJIAN"
        const val FAXIAN = "FAXIAN"
        const val DONGTAI = "DONGTAI"
        const val R18 = "R18"

        @JvmField
        val NAVIGATION_MAP: Map<String, NavigationItem> = LinkedHashMap<String, NavigationItem>().apply {
            put(TUIJIAN, NavigationItem(R.string.recommend, R.drawable.ic_tuijian, FragmentLeft::class.java))
            put(FAXIAN, NavigationItem(R.string.discover, R.drawable.ic_discover, FragmentCenter::class.java))
            put(DONGTAI, NavigationItem(R.string.whats_new, R.drawable.ic_dongtai, FragmentRight::class.java))
            put(R18, NavigationItem(R.string.string_r, R.drawable.ic_xiongbu, FragmentViewPager::class.java))
        }

        @JvmField
        val SETTING_NAME_MAP: Map<String, String> = LinkedHashMap<String, String>().apply {
            put(LATEST, resources.getString(R.string.string_427))
            put(TUIJIAN, resources.getString(R.string.recommend))
            put(FAXIAN, resources.getString(R.string.discover))
            put(DONGTAI, resources.getString(R.string.whats_new))
            put(R18, resources.getString(R.string.string_r))
        }
    }
}
