package ceui.lisa.utils

import androidx.fragment.app.Fragment
import ceui.lisa.fragments.ListFragment
import com.google.android.material.tabs.TabLayout

class MyOnTabSelectedListener(
    private val fragments: Array<Fragment>
) : TabLayout.OnTabSelectedListener {

    override fun onTabSelected(tab: TabLayout.Tab) {
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
    }

    override fun onTabReselected(tab: TabLayout.Tab) {
        val position = tab.position
        if (position < fragments.size) {
            val fragment = fragments[position]
            if (fragment is ListFragment<*, *>) {
                fragment.scrollToTop()
            }
        }
    }
}
