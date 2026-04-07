package ceui.lisa.helper

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import ceui.lisa.interfaces.FragmentBackHandler

object BackHandlerHelper {

    @JvmStatic
    fun handleBackPress(fragmentManager: FragmentManager): Boolean {
        val fragments = fragmentManager.fragments
        for (i in fragments.size - 1 downTo 0) {
            val child = fragments[i]
            if (isFragmentBackHandled(child)) {
                return true
            }
        }

        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
            return true
        }
        return false
    }

    @JvmStatic
    fun handleBackPress(fragment: Fragment): Boolean {
        return handleBackPress(fragment.childFragmentManager)
    }

    @JvmStatic
    fun handleBackPress(fragmentActivity: FragmentActivity): Boolean {
        return handleBackPress(fragmentActivity.supportFragmentManager)
    }

    @JvmStatic
    fun isFragmentBackHandled(fragment: Fragment?): Boolean {
        return fragment != null &&
            fragment.isVisible &&
            fragment.userVisibleHint &&
            fragment is FragmentBackHandler &&
            fragment.onBackPressed()
    }
}
