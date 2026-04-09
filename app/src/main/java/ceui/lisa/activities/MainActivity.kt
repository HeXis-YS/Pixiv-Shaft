package ceui.lisa.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import ceui.lisa.R
import ceui.lisa.core.Manager
import ceui.lisa.databinding.ActivityCoverBinding
import ceui.lisa.fragments.FragmentCenter
import ceui.lisa.fragments.FragmentLeft
import ceui.lisa.fragments.FragmentRight
import ceui.lisa.fragments.FragmentViewPager
import ceui.lisa.helper.DrawerLayoutHelper
import ceui.lisa.helper.NavigationLocationHelper
import ceui.lisa.utils.Common
import ceui.lisa.utils.GlideUtil
import ceui.lisa.utils.Params
import ceui.lisa.view.DrawerLayoutViewPager
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

/**
 * 主页
 */
class MainActivity : BaseActivity<ActivityCoverBinding>(), NavigationView.OnNavigationItemSelectedListener {

    private var userHead: ImageView? = null
    private var username: TextView? = null
    private var userEmail: TextView? = null
    private var mExitTime = 0L
    private lateinit var baseFragments: Array<Fragment>

    override fun initLayout(): Int = R.layout.activity_cover

    override fun hideStatusBar(): Boolean = true

    override fun initView() {
        baseBind.drawerLayout.setScrimColor(Color.TRANSPARENT)
        baseBind.navView.setNavigationItemSelectedListener(this)
        userHead = baseBind.navView.getHeaderView(0).findViewById(R.id.user_head)
        username = baseBind.navView.getHeaderView(0).findViewById(R.id.user_name)
        userEmail = baseBind.navView.getHeaderView(0).findViewById(R.id.user_email)
        initDrawerHeader()
        userHead?.setOnClickListener {
            Common.showUser(mContext, Shaft.sUserModel)
            baseBind.drawerLayout.closeDrawer(GravityCompat.START)
        }
        userHead?.setOnLongClickListener {
            val filterEnable = Shaft.sSettings.isR18FilterTempEnable()
            Shaft.sSettings.setR18FilterTempEnable(!filterEnable)
            Common.showToast(if (filterEnable) "ԅ(♡﹃♡ԅ)" else "X﹏X")
            true
        }
        baseBind.navigationView.setOnNavigationItemSelectedListener { item ->
            navigateToPage(item.itemId)
        }
        baseBind.navigationView.setOnNavigationItemReselectedListener { item ->
            forceRefreshCurrentPage(item.itemId)
        }
        baseBind.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int,
            ) {
            }

            override fun onPageSelected(position: Int) {
                syncBottomNavigation(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
        baseBind.viewPager.setTouchEventForwarder(object : DrawerLayoutViewPager.IForwardTouchEvent {
            override fun forwardTouchEvent(ev: MotionEvent) {
                getDrawer().onTouchEvent(ev)
            }
        })
        DrawerLayoutHelper.setCustomLeftEdgeSize(getDrawer(), 1.0f)
    }

    private fun initFragment() {
        if (Shaft.sSettings.isMainViewR18()) {
            baseBind.navigationView.inflateMenu(R.menu.main_activity0_with_r18)
            baseFragments = arrayOf(
                FragmentLeft(),
                FragmentCenter(),
                FragmentRight(),
                FragmentViewPager.newInstance(Params.VIEW_PAGER_R18),
            )
        } else {
            baseBind.navigationView.inflateMenu(R.menu.main_activity0)
            baseFragments = arrayOf(
                FragmentLeft(),
                FragmentCenter(),
                FragmentRight(),
            )
        }
        baseBind.viewPager.adapter = object : FragmentPagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment = baseFragments[position]

            override fun getCount(): Int = baseFragments.size
        }
        baseBind.viewPager.offscreenPageLimit = baseFragments.size - 1
        baseBind.viewPager.currentItem = getNavigationInitPosition()
        Manager.get().restoreAsync()
    }

    override fun initData() {
        if (!hasLoggedInUser()) {
            redirectToLogin()
            return
        }
        continueStartup()
    }

    private fun hasLoggedInUser(): Boolean {
        val user = Shaft.sUserModel.getUser()
        return user != null && user.isIs_login()
    }

    private fun redirectToLogin() {
        TemplateActivity.startLogin(mContext)
        finish()
    }

    private fun continueStartup() {
        if (shouldSkipStoragePermission()) {
            completeStartup()
            return
        }
        ensureStoragePermission()
    }

    private fun shouldSkipStoragePermission(): Boolean = Common.isAndroidQ()

    private fun completeStartup() {
        initFragment()
    }

    private fun ensureStoragePermission() {
        if (hasStoragePermission()) {
            completeStartup()
            return
        }
        requestStoragePermission()
    }

    private fun hasStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_WRITE_STORAGE,
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_WRITE_STORAGE) {
            handleStoragePermissionResult(grantResults)
        }
    }

    private fun handleStoragePermissionResult(grantResults: IntArray) {
        if (isPermissionGranted(grantResults)) {
            completeStartup()
            return
        }
        handleStoragePermissionDenied()
    }

    private fun isPermissionGranted(grantResults: IntArray): Boolean {
        return grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
    }

    private fun handleStoragePermissionDenied() {
        Common.showToast(getString(R.string.access_denied))
        finish()
    }

    fun getDrawer(): DrawerLayout = baseBind.drawerLayout

    @SuppressLint("NonConstantResourceId")
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        handleDrawerNavigation(item.itemId)
        baseBind.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun handleDrawerNavigation(itemId: Int) {
        if (itemId == R.id.nav_new_work) {
            TemplateActivity.startLatest(this)
            return
        }
        val intent = resolveDrawerDestination(itemId)
        if (intent != null) {
            startActivity(intent)
        }
    }

    private fun navigateToPage(itemId: Int): Boolean {
        val pageIndex = getPageIndex(itemId)
        if (pageIndex == -1) {
            return false
        }
        baseBind.viewPager.currentItem = pageIndex
        return true
    }

    private fun getPageIndex(itemId: Int): Int {
        return when (itemId) {
            R.id.action_1 -> PAGE_LEFT
            R.id.action_2 -> PAGE_CENTER
            R.id.action_3 -> PAGE_RIGHT
            R.id.action_4 -> PAGE_R18
            else -> -1
        }
    }

    private fun forceRefreshCurrentPage(itemId: Int) {
        val pageIndex = getPageIndex(itemId)
        if (pageIndex == -1) {
            return
        }
        forceRefreshPage(pageIndex)
    }

    private fun forceRefreshPage(pageIndex: Int) {
        when (val fragment = getBaseFragment(pageIndex)) {
            is FragmentLeft -> fragment.forceRefresh()
            is FragmentCenter -> fragment.forceRefresh()
            is FragmentRight -> fragment.forceRefresh()
            is FragmentViewPager -> fragment.forceRefresh()
        }
    }

    private fun getBaseFragment(pageIndex: Int): Fragment? {
        if (pageIndex < 0 || pageIndex >= baseFragments.size) {
            return null
        }
        return baseFragments[pageIndex]
    }

    private fun syncBottomNavigation(position: Int) {
        val itemId = getBottomNavigationItemId(position)
        if (itemId != View.NO_ID) {
            baseBind.navigationView.selectedItemId = itemId
        }
    }

    private fun getBottomNavigationItemId(pageIndex: Int): Int {
        return when (pageIndex) {
            PAGE_LEFT -> R.id.action_1
            PAGE_CENTER -> R.id.action_2
            PAGE_RIGHT -> R.id.action_3
            PAGE_R18 -> R.id.action_4
            else -> View.NO_ID
        }
    }

    private fun resolveDrawerDestination(itemId: Int): Intent? {
        return when (itemId) {
            R.id.nav_gallery -> TemplateActivity.newDownloadManagerIntent(this)
            R.id.nav_slideshow -> TemplateActivity.newHistoryIntent(this)
            R.id.nav_manage -> TemplateActivity.newSettingsIntent(this)
            R.id.nav_share -> TemplateActivity.newAboutIntent(this)
            R.id.main_page -> UActivity.newCurrentUserIntent(this)
            R.id.muted_list -> TemplateActivity.newMutedTagsIntent(this)
            R.id.nav_feature -> TemplateActivity.newFeatureIntent(this)
            R.id.nav_fans -> TemplateActivity.newFansIntent(this, Shaft.sUserModel.getUser().getId())
            R.id.illust_star -> TemplateActivity.newMyIllustBookmarksIntent(this)
            R.id.novel_star -> TemplateActivity.newMyNovelBookmarksIntent(this)
            R.id.watchlist -> TemplateActivity.newWatchlistIntent(this)
            R.id.novel_markers -> TemplateActivity.newNovelMarkersIntent(this)
            R.id.follow_user -> TemplateActivity.newMyFollowingIntent(this)
            else -> null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }

    private fun initDrawerHeader() {
        val user = Shaft.sUserModel.getUser() ?: return
        Glide.with(mContext)
            .load(GlideUtil.getHead(user))
            .into(userHead!!)
        username?.text = user.getName()
        userEmail?.text = if (TextUtils.isEmpty(user.getMail_address())) {
            mContext.getString(R.string.no_mail_address)
        } else {
            user.getMail_address()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (handleOpenDrawerOnBack()) {
            return true
        }
        if (shouldInterceptBack(keyCode, event)) {
            exit()
            return true
        }
        return false
    }

    private fun handleOpenDrawerOnBack(): Boolean {
        if (!baseBind.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            return false
        }
        baseBind.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun shouldInterceptBack(keyCode: Int, event: KeyEvent): Boolean {
        return keyCode == KeyEvent.KEYCODE_BACK && event.repeatCount == 0
    }

    fun exit() {
        if (shouldFinishOnBackPress()) {
            finish()
            return
        }
        if (hasPendingDownloadTasks()) {
            showDownloadTaskExitDialog()
            return
        }
        recordPendingExit()
    }

    private fun shouldFinishOnBackPress(): Boolean {
        return System.currentTimeMillis() - mExitTime <= 2000
    }

    private fun hasPendingDownloadTasks(): Boolean {
        return Manager.get().content.size != 0
    }

    private fun showDownloadTaskExitDialog() {
        val builder = AlertDialog.Builder(mContext)
        builder.setTitle(getString(R.string.shaft_hint))
        builder.setMessage(mContext.getString(R.string.you_have_download_plan))
        builder.setPositiveButton(mContext.getString(R.string.sure)) { _: DialogInterface, _: Int ->
            stopDownloadsAndExit()
        }
        builder.setNegativeButton(mContext.getString(R.string.cancel), null)
        builder.setNeutralButton(getString(R.string.see_download_task)) { _, _ ->
            openDownloadManager()
        }
        builder.create().show()
    }

    private fun stopDownloadsAndExit() {
        Manager.get().stopAll()
        finish()
    }

    private fun openDownloadManager() {
        TemplateActivity.startDownloadManager(this)
    }

    private fun recordPendingExit() {
        Common.showToast(getString(R.string.double_click_finish))
        mExitTime = System.currentTimeMillis()
    }

    override fun onResume() {
        super.onResume()
        if (shouldRefreshDrawerHeader()) {
            initDrawerHeader()
            intent.removeExtra(EXTRA_REFRESH_DRAWER_HEADER)
        }
    }

    private fun shouldRefreshDrawerHeader(): Boolean {
        return intent.getBooleanExtra(EXTRA_REFRESH_DRAWER_HEADER, false)
    }

    override fun finish() {
        val currentPosition = baseBind.viewPager.currentItem
        Shaft.getMMKV().putInt(Params.MAIN_ACTIVITY_NAVIGATION_POSITION, currentPosition)
        super.finish()
    }

    private fun getNavigationInitPosition(): Int {
        val defaultPosition = 0
        val settingValue = Shaft.sSettings.getNavigationInitPosition()
        if (settingValue == NavigationLocationHelper.LATEST) {
            val latestPosition = Shaft.getMMKV().getInt(Params.MAIN_ACTIVITY_NAVIGATION_POSITION, 0)
            return if (latestPosition < baseFragments.size) latestPosition else defaultPosition
        }
        val navigationValue = NavigationLocationHelper.NAVIGATION_MAP.getOrDefault(settingValue, null)
            ?: return defaultPosition
        val clazz = navigationValue.getInstanceClass()
        for (i in baseFragments.indices) {
            val fragment = baseFragments[i]
            if (clazz == fragment.javaClass) {
                return i
            }
        }
        return defaultPosition
    }

    companion object {
        private const val REQUEST_WRITE_STORAGE = 1001
        private const val EXTRA_REFRESH_DRAWER_HEADER = "refreshDrawerHeader"
        private const val PAGE_LEFT = 0
        private const val PAGE_CENTER = 1
        private const val PAGE_RIGHT = 2
        private const val PAGE_R18 = 3

        @JvmStatic
        fun newIntent(context: Context, refreshDrawerHeader: Boolean): Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(EXTRA_REFRESH_DRAWER_HEADER, refreshDrawerHeader)
            return intent
        }
    }
}
