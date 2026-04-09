package ceui.lisa.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import ceui.lisa.R
import ceui.lisa.databinding.ActivityFragmentBinding
import ceui.lisa.fragments.FragmentAboutApp
import ceui.lisa.fragments.FragmentBookedTag
import ceui.lisa.fragments.FragmentCollection
import ceui.lisa.fragments.FragmentColors
import ceui.lisa.fragments.FragmentComment
import ceui.lisa.fragments.FragmentDownload
import ceui.lisa.fragments.FragmentEditAccount
import ceui.lisa.fragments.FragmentFeature
import ceui.lisa.fragments.FragmentFileName
import ceui.lisa.fragments.FragmentFollowUser
import ceui.lisa.fragments.FragmentHistory
import ceui.lisa.fragments.FragmentImageDetail
import ceui.lisa.fragments.FragmentLikeIllust
import ceui.lisa.fragments.FragmentLikeNovel
import ceui.lisa.fragments.FragmentListSimpleUser
import ceui.lisa.fragments.FragmentLocalUsers
import ceui.lisa.fragments.FragmentLogin
import ceui.lisa.fragments.FragmentMangaSeries
import ceui.lisa.fragments.FragmentMangaSeriesDetail
import ceui.lisa.fragments.FragmentMultiDownload
import ceui.lisa.fragments.FragmentNew
import ceui.lisa.fragments.FragmentNewNovel
import ceui.lisa.fragments.FragmentNewNovels
import ceui.lisa.fragments.FragmentNiceFriend
import ceui.lisa.fragments.FragmentNovelHolder
import ceui.lisa.fragments.FragmentNovelMarkers
import ceui.lisa.fragments.FragmentNovelSeries
import ceui.lisa.fragments.FragmentNovelSeriesDetail
import ceui.lisa.fragments.FragmentPv
import ceui.lisa.fragments.FragmentRecmdIllust
import ceui.lisa.fragments.FragmentRecmdUser
import ceui.lisa.fragments.FragmentRelatedIllust
import ceui.lisa.fragments.FragmentRelatedUser
import ceui.lisa.fragments.FragmentSB
import ceui.lisa.fragments.FragmentSearch
import ceui.lisa.fragments.FragmentSettings
import ceui.lisa.fragments.FragmentUserIllust
import ceui.lisa.fragments.FragmentUserInfo
import ceui.lisa.fragments.FragmentUserManga
import ceui.lisa.fragments.FragmentUserNovel
import ceui.lisa.fragments.FragmentViewPager
import ceui.lisa.fragments.FragmentWebView
import ceui.lisa.fragments.FragmentWhoFollowThisUser
import ceui.lisa.helper.BackHandlerHelper
import ceui.lisa.models.IllustsBean
import ceui.lisa.models.NovelBean
import ceui.lisa.models.UserDetailResponse
import ceui.lisa.models.UserPreviewsBean
import ceui.lisa.utils.Local
import ceui.lisa.utils.Params
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener

class TemplateActivity : BaseActivity<ActivityFragmentBinding>(), ColorPickerDialogListener {
    protected var childFragment: Fragment? = null
    private var dataType: String? = null

    override fun initBundle(bundle: Bundle) {
        dataType = bundle.getString(EXTRA_FRAGMENT)
    }

    protected fun createNewFragment(): Fragment? {
        val intent = intent
        if (!TextUtils.isEmpty(dataType)) {
            createWebFragment(intent)?.let { return it }
            createUserFragment(intent)?.let { return it }
            createCollectionFragment(intent)?.let { return it }
            createDetailFragment(intent)?.let { return it }
            createSettingsFragment(intent)?.let { return it }
            return Fragment()
        }
        return null
    }

    private fun createWebFragment(intent: Intent): Fragment? =
        when (dataType) {
            FRAGMENT_WEB -> {
                val url = intent.getStringExtra(Params.URL)
                val title = intent.getStringExtra(Params.TITLE)
                val preferPreserve = intent.getBooleanExtra(Params.PREFER_PRESERVE, false)
                FragmentWebView.newInstance(title, url, preferPreserve)
            }

            FRAGMENT_IMAGE_DETAIL -> FragmentImageDetail.newInstance(intent.getStringExtra(Params.URL))
            else -> null
        }

    private fun createUserFragment(intent: Intent): Fragment? =
        when (dataType) {
            FRAGMENT_RECMD_USER -> {
                val bundleExtra = intent.getBundleExtra(Params.USER_MODEL)
                if (bundleExtra == null) {
                    FragmentRecmdUser()
                } else {
                    val userPreviewsBeans =
                        bundleExtra.getSerializable(Params.USER_MODEL) as ArrayList<UserPreviewsBean>?
                    val nextUrl = intent.getStringExtra(Params.URL)
                    FragmentRecmdUser(userPreviewsBeans, nextUrl)
                }
            }

            FRAGMENT_LOCAL_USERS -> FragmentLocalUsers()
            FRAGMENT_FOLLOWING -> FragmentFollowUser.newInstance(readUserId(intent), Params.TYPE_PUBLIC, true)
            FRAGMENT_NICE_FRIEND -> FragmentNiceFriend()
            FRAGMENT_USER_INFO -> FragmentUserInfo()
            FRAGMENT_FANS -> FragmentWhoFollowThisUser.newInstance(readUserId(intent))
            FRAGMENT_LIKE_USER_LIST ->
                FragmentListSimpleUser.newInstance(intent.getSerializableExtra(Params.CONTENT) as IllustsBean)

            FRAGMENT_ILLUST_WORKS -> FragmentUserIllust.newInstance(readUserId(intent), true)
            FRAGMENT_MANGA_WORKS -> FragmentUserManga.newInstance(readUserId(intent), true)
            FRAGMENT_PUBLIC_ILLUST_STAR -> FragmentLikeIllust.newInstance(readUserId(intent), Params.TYPE_PUBLIC, true)
            FRAGMENT_PUBLIC_NOVEL_STAR -> FragmentLikeNovel.newInstance(readUserId(intent), Params.TYPE_PUBLIC, true)
            FRAGMENT_NOVEL_WORKS -> FragmentUserNovel.newInstance(readUserId(intent))
            FRAGMENT_RELATED_USER -> FragmentRelatedUser.newInstance(readUserId(intent))
            else -> null
        }

    private fun createCollectionFragment(intent: Intent): Fragment? =
        when (dataType) {
            FRAGMENT_MY_ILLUST_STAR -> createCollectionFragment(COLLECTION_ILLUST_BOOKMARKS)
            FRAGMENT_MY_NOVEL_STAR -> createCollectionFragment(COLLECTION_NOVEL_BOOKMARKS)
            FRAGMENT_WATCHLIST -> createCollectionFragment(COLLECTION_WATCHLIST)
            FRAGMENT_MY_FOLLOWING -> createCollectionFragment(COLLECTION_FOLLOWING)
            FRAGMENT_RECMD_MANGA -> FragmentRecmdIllust.newInstance("漫画")
            FRAGMENT_RECMD_NOVEL -> FragmentNewNovel()
            FRAGMENT_NEW_NOVELS -> FragmentNewNovels()
            FRAGMENT_FEATURE -> FragmentFeature()
            FRAGMENT_NOVEL_MARKERS -> FragmentNovelMarkers()
            else -> null
        }

    private fun createDetailFragment(intent: Intent): Fragment? =
        when (dataType) {
            FRAGMENT_LOGIN -> FragmentLogin()
            FRAGMENT_RELATED_ILLUST -> {
                val id = readIllustId(intent)
                val title = intent.getStringExtra(Params.ILLUST_TITLE)
                FragmentRelatedIllust.newInstance(id, title)
            }

            FRAGMENT_HISTORY -> FragmentHistory()
            FRAGMENT_PV -> FragmentPv()
            FRAGMENT_COMMENT -> {
                val title = intent.getStringExtra(Params.ILLUST_TITLE)
                var workId = readIllustId(intent)
                if (workId == 0) {
                    workId = readNovelId(intent)
                    FragmentComment.newNovelInstance(workId, title!!)
                } else {
                    FragmentComment.newIllustInstance(workId, title!!)
                }
            }

            FRAGMENT_ABOUT -> FragmentAboutApp()
            FRAGMENT_MULTI_DOWNLOAD -> FragmentMultiDownload()
            FRAGMENT_SEARCH -> FragmentSearch()
            FRAGMENT_LATEST -> FragmentNew()
            FRAGMENT_NOVEL_SERIES_DETAIL -> FragmentNovelSeriesDetail.newInstance(readId(intent))
            FRAGMENT_DOWNLOAD -> FragmentDownload()
            FRAGMENT_NOVEL_DETAIL ->
                FragmentNovelHolder.newInstance(intent.getSerializableExtra(Params.CONTENT) as NovelBean?)

            FRAGMENT_MANGA_SERIES -> FragmentMangaSeries.newInstance(readUserId(intent))
            FRAGMENT_MANGA_SERIES_DETAIL -> FragmentMangaSeriesDetail.newInstance(readMangaSeriesId(intent))
            FRAGMENT_NOVEL_SERIES -> FragmentNovelSeries()
            else -> null
        }

    private fun createSettingsFragment(intent: Intent): Fragment? =
        when (dataType) {
            FRAGMENT_SETTINGS -> FragmentSettings()
            FRAGMENT_TAG_FILTER -> FragmentBookedTag.newInstance(readDataType(intent), intent.getStringExtra(EXTRA_KEYWORD))
            FRAGMENT_TAG_STAR -> {
                val id = readIllustId(intent)
                val type = intent.getStringExtra(Params.DATA_TYPE)
                val tagNames = intent.getStringArrayExtra(Params.TAG_NAMES)
                FragmentSB.newInstance(id, type, tagNames)
            }

            FRAGMENT_BIND_EMAIL -> FragmentEditAccount()
            FRAGMENT_MUTED_TAGS -> FragmentViewPager.newInstance(Params.VIEW_PAGER_MUTED)
            FRAGMENT_FILENAME -> FragmentFileName.newInstance()
            FRAGMENT_THEME -> FragmentColors()
            else -> null
        }

    private fun createCollectionFragment(collectionType: Int): Fragment {
        return FragmentCollection.newInstance(collectionType)
    }

    private fun readUserId(intent: Intent): Int = intent.getIntExtra(Params.USER_ID, 0)

    private fun readIllustId(intent: Intent): Int = intent.getIntExtra(Params.ILLUST_ID, 0)

    private fun readNovelId(intent: Intent): Int = intent.getIntExtra(Params.NOVEL_ID, 0)

    private fun readId(intent: Intent): Int = intent.getIntExtra(Params.ID, 0)

    private fun readMangaSeriesId(intent: Intent): Int = intent.getIntExtra(Params.MANGA_SERIES_ID, 0)

    private fun readDataType(intent: Intent): Int = intent.getIntExtra(Params.DATA_TYPE, 0)

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (childFragment is FragmentWebView) {
            return (childFragment as FragmentWebView).getAgentWeb().handleKeyEvent(keyCode, event) ||
                super.onKeyDown(keyCode, event)
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun initLayout(): Int = R.layout.activity_fragment

    override fun initView() {
    }

    override fun initData() {
        val fragmentManager: FragmentManager = supportFragmentManager
        var fragment = fragmentManager.findFragmentById(R.id.fragment_container)
        if (fragment == null) {
            fragment = createNewFragment()
            if (fragment != null) {
                fragmentManager.beginTransaction().add(R.id.fragment_container, fragment).commit()
                childFragment = fragment
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        childFragment?.onActivityResult(requestCode, resultCode, data)
    }

    override fun hideStatusBar(): Boolean {
        return if (FRAGMENT_COMMENT == dataType) {
            false
        } else {
            intent.getBooleanExtra(EXTRA_HIDE_STATUS_BAR, true)
        }
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        if (childFragment is FragmentNovelHolder) {
            if (dialogId == Params.DIALOG_NOVEL_BG_COLOR) {
                Shaft.sSettings.setNovelHolderColor(color)
                (childFragment as FragmentNovelHolder).setBackgroundColor(color)
            } else if (dialogId == Params.DIALOG_NOVEL_TEXT_COLOR) {
                Shaft.sSettings.setNovelHolderTextColor(color)
                (childFragment as FragmentNovelHolder).setTextColor(color)
            }

            Local.setSettings(Shaft.sSettings)
        }
    }

    override fun onDialogDismissed(dialogId: Int) {
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (!BackHandlerHelper.handleBackPress(this)) {
            super.onBackPressed()
        }
    }

    companion object {
        @JvmField
        val EXTRA_FRAGMENT = "dataType"

        @JvmField
        val EXTRA_KEYWORD = "keyword"

        private const val EXTRA_HIDE_STATUS_BAR = "hideStatusBar"

        @JvmField
        val FRAGMENT_LOGIN = "登录注册"

        @JvmField
        val FRAGMENT_RELATED_ILLUST = "相关作品"

        private const val FRAGMENT_HISTORY = "浏览记录"

        @JvmField
        val FRAGMENT_WEB = "网页链接"

        @JvmField
        val FRAGMENT_SETTINGS = "设置"

        private const val FRAGMENT_RECMD_USER = "推荐用户"
        private const val FRAGMENT_PV = "特辑"

        @JvmField
        val FRAGMENT_COMMENT = "相关评论"

        @JvmField
        val FRAGMENT_TAG_FILTER = "按标签筛选"

        @JvmField
        val FRAGMENT_TAG_STAR = "按标签收藏"

        private const val FRAGMENT_ABOUT = "关于软件"
        private const val FRAGMENT_MULTI_DOWNLOAD = "批量下载"

        @JvmField
        val FRAGMENT_SEARCH = "搜索"

        private const val FRAGMENT_LATEST = "最新作品"

        @JvmField
        val FRAGMENT_DOWNLOAD = "下载管理"

        private const val FRAGMENT_BIND_EMAIL = "绑定邮箱"
        private const val FRAGMENT_MUTED_TAGS = "标签屏蔽记录"
        private const val FRAGMENT_FILENAME = "修改命名方式"
        private const val FRAGMENT_THEME = "主题颜色"

        @JvmField
        val FRAGMENT_LOCAL_USERS = "账号管理"

        private const val FRAGMENT_FOLLOWING = "正在关注"
        private const val FRAGMENT_NICE_FRIEND = "好P友"
        private const val FRAGMENT_USER_INFO = "详细信息"
        private const val FRAGMENT_FANS = "粉丝"

        @JvmField
        val FRAGMENT_LIKE_USER_LIST = "喜欢这个作品的用户"

        private const val FRAGMENT_RELATED_USER = "相关用户"
        private const val FRAGMENT_ILLUST_WORKS = "插画作品"
        private const val FRAGMENT_MANGA_WORKS = "漫画作品"
        private const val FRAGMENT_PUBLIC_ILLUST_STAR = "插画/漫画收藏"
        private const val FRAGMENT_PUBLIC_NOVEL_STAR = "小说收藏"
        private const val FRAGMENT_NOVEL_WORKS = "小说作品"
        private const val FRAGMENT_MY_ILLUST_STAR = "我的插画收藏"
        private const val FRAGMENT_MY_NOVEL_STAR = "我的小说收藏"
        private const val FRAGMENT_WATCHLIST = "追更列表"
        private const val FRAGMENT_MY_FOLLOWING = "我的关注"
        private const val FRAGMENT_NOVEL_MARKERS = "小说书签"
        private const val FRAGMENT_RECMD_MANGA = "推荐漫画"
        private const val FRAGMENT_RECMD_NOVEL = "推荐小说"
        private const val FRAGMENT_NEW_NOVELS = "关注者的小说"

        @JvmField
        val FRAGMENT_NOVEL_DETAIL = "小说详情"

        @JvmField
        val FRAGMENT_IMAGE_DETAIL = "图片详情"

        @JvmField
        val FRAGMENT_NOVEL_SERIES_DETAIL = "小说系列详情"

        private const val FRAGMENT_MANGA_SERIES = "漫画系列作品"

        @JvmField
        val FRAGMENT_MANGA_SERIES_DETAIL = "漫画系列详情"

        private const val FRAGMENT_NOVEL_SERIES = "小说系列作品"
        private const val FRAGMENT_FEATURE = "精华列"
        private const val COLLECTION_ILLUST_BOOKMARKS = 0
        private const val COLLECTION_NOVEL_BOOKMARKS = 1
        private const val COLLECTION_FOLLOWING = 2
        private const val COLLECTION_WATCHLIST = 3

        @JvmStatic
        fun newSettingsIntent(context: Context): Intent = createIntent(context, FRAGMENT_SETTINGS)

        @JvmStatic
        fun startSettings(context: Context) {
            context.startActivity(newSettingsIntent(context))
        }

        @JvmStatic
        fun newLoginIntent(context: Context): Intent = createIntent(context, FRAGMENT_LOGIN)

        @JvmStatic
        fun startLogin(context: Context) {
            context.startActivity(newLoginIntent(context))
        }

        @JvmStatic
        fun newDownloadManagerIntent(context: Context): Intent = createIntent(context, FRAGMENT_DOWNLOAD)

        @JvmStatic
        fun startDownloadManager(context: Context) {
            context.startActivity(newDownloadManagerIntent(context))
        }

        @JvmStatic
        fun newHistoryIntent(context: Context): Intent = createIntent(context, FRAGMENT_HISTORY)

        @JvmStatic
        fun startHistory(context: Context) {
            context.startActivity(newHistoryIntent(context))
        }

        @JvmStatic
        fun newAboutIntent(context: Context): Intent = createIntent(context, FRAGMENT_ABOUT)

        @JvmStatic
        fun startAbout(context: Context) {
            context.startActivity(newAboutIntent(context))
        }

        @JvmStatic
        fun newMutedTagsIntent(context: Context): Intent = createIntent(context, FRAGMENT_MUTED_TAGS)

        @JvmStatic
        fun startMutedTags(context: Context) {
            context.startActivity(newMutedTagsIntent(context))
        }

        @JvmStatic
        fun newFeatureIntent(context: Context): Intent = createIntent(context, FRAGMENT_FEATURE)

        @JvmStatic
        fun startFeature(context: Context) {
            context.startActivity(newFeatureIntent(context))
        }

        @JvmStatic
        fun newMultiDownloadIntent(context: Context): Intent = createIntent(context, FRAGMENT_MULTI_DOWNLOAD)

        @JvmStatic
        fun startMultiDownload(context: Context) {
            context.startActivity(newMultiDownloadIntent(context))
        }

        @JvmStatic
        fun newSearchIntent(context: Context): Intent = createIntent(context, FRAGMENT_SEARCH)

        @JvmStatic
        fun startSearch(context: Context) {
            context.startActivity(newSearchIntent(context))
        }

        @JvmStatic
        fun newLocalUsersIntent(context: Context): Intent = createIntent(context, FRAGMENT_LOCAL_USERS)

        @JvmStatic
        fun startLocalUsers(context: Context) {
            context.startActivity(newLocalUsersIntent(context))
        }

        @JvmStatic
        fun newWebIntent(context: Context, title: String?, url: String?): Intent =
            newWebIntent(context, title, url, false)

        @JvmStatic
        fun newWebIntent(context: Context, title: String?, url: String?, preferPreserve: Boolean): Intent {
            val intent = createIntent(context, FRAGMENT_WEB)
            intent.putExtra(Params.TITLE, title)
            intent.putExtra(Params.URL, url)
            intent.putExtra(Params.PREFER_PRESERVE, preferPreserve)
            return intent
        }

        @JvmStatic
        fun startWeb(context: Context, title: String?, url: String?) {
            context.startActivity(newWebIntent(context, title, url))
        }

        @JvmStatic
        fun startWeb(context: Context, title: String?, url: String?, preferPreserve: Boolean) {
            context.startActivity(newWebIntent(context, title, url, preferPreserve))
        }

        @JvmStatic
        fun newBindEmailIntent(context: Context): Intent = createIntent(context, FRAGMENT_BIND_EMAIL)

        @JvmStatic
        fun startBindEmail(context: Context) {
            context.startActivity(newBindEmailIntent(context))
        }

        @JvmStatic
        fun newThemeColorsIntent(context: Context): Intent = createIntent(context, FRAGMENT_THEME)

        @JvmStatic
        fun startThemeColors(context: Context) {
            context.startActivity(newThemeColorsIntent(context))
        }

        @JvmStatic
        fun newFileNameSettingsIntent(context: Context): Intent = createIntent(context, FRAGMENT_FILENAME)

        @JvmStatic
        fun startFileNameSettings(context: Context) {
            context.startActivity(newFileNameSettingsIntent(context))
        }

        @JvmStatic
        fun newLatestIntent(context: Context): Intent = createIntent(context, FRAGMENT_LATEST, false)

        @JvmStatic
        fun startLatest(context: Context) {
            context.startActivity(newLatestIntent(context))
        }

        @JvmStatic
        fun newMyIllustBookmarksIntent(context: Context): Intent = createIntent(context, FRAGMENT_MY_ILLUST_STAR, false)

        @JvmStatic
        fun startMyIllustBookmarks(context: Context) {
            context.startActivity(newMyIllustBookmarksIntent(context))
        }

        @JvmStatic
        fun newMyNovelBookmarksIntent(context: Context): Intent = createIntent(context, FRAGMENT_MY_NOVEL_STAR, false)

        @JvmStatic
        fun startMyNovelBookmarks(context: Context) {
            context.startActivity(newMyNovelBookmarksIntent(context))
        }

        @JvmStatic
        fun newWatchlistIntent(context: Context): Intent = createIntent(context, FRAGMENT_WATCHLIST, false)

        @JvmStatic
        fun startWatchlist(context: Context) {
            context.startActivity(newWatchlistIntent(context))
        }

        @JvmStatic
        fun newNovelMarkersIntent(context: Context): Intent = createIntent(context, FRAGMENT_NOVEL_MARKERS, false)

        @JvmStatic
        fun startNovelMarkers(context: Context) {
            context.startActivity(newNovelMarkersIntent(context))
        }

        @JvmStatic
        fun newMyFollowingIntent(context: Context): Intent = createIntent(context, FRAGMENT_MY_FOLLOWING, false)

        @JvmStatic
        fun startMyFollowing(context: Context) {
            context.startActivity(newMyFollowingIntent(context))
        }

        @JvmStatic
        fun newFollowingIntent(context: Context, userId: Int): Intent =
            createIntent(context, FRAGMENT_FOLLOWING).apply { putExtra(Params.USER_ID, userId) }

        @JvmStatic
        fun startFollowing(context: Context, userId: Int) {
            context.startActivity(newFollowingIntent(context, userId))
        }

        @JvmStatic
        fun newNiceFriendIntent(context: Context, userId: Int): Intent =
            createIntent(context, FRAGMENT_NICE_FRIEND).apply { putExtra(Params.USER_ID, userId) }

        @JvmStatic
        fun startNiceFriend(context: Context, userId: Int) {
            context.startActivity(newNiceFriendIntent(context, userId))
        }

        @JvmStatic
        fun newUserIllustIntent(context: Context, userId: Int): Intent =
            createIntent(context, FRAGMENT_ILLUST_WORKS).apply { putExtra(Params.USER_ID, userId) }

        @JvmStatic
        fun startUserIllust(context: Context, userId: Int) {
            context.startActivity(newUserIllustIntent(context, userId))
        }

        @JvmStatic
        fun newUserMangaIntent(context: Context, userId: Int): Intent =
            createIntent(context, FRAGMENT_MANGA_WORKS).apply { putExtra(Params.USER_ID, userId) }

        @JvmStatic
        fun startUserManga(context: Context, userId: Int) {
            context.startActivity(newUserMangaIntent(context, userId))
        }

        @JvmStatic
        fun newMangaSeriesIntent(context: Context, userId: Int): Intent =
            createIntent(context, FRAGMENT_MANGA_SERIES).apply { putExtra(Params.USER_ID, userId) }

        @JvmStatic
        fun startMangaSeries(context: Context, userId: Int) {
            context.startActivity(newMangaSeriesIntent(context, userId))
        }

        @JvmStatic
        fun newUserNovelIntent(context: Context, userId: Int): Intent =
            createIntent(context, FRAGMENT_NOVEL_WORKS).apply { putExtra(Params.USER_ID, userId) }

        @JvmStatic
        fun startUserNovel(context: Context, userId: Int) {
            context.startActivity(newUserNovelIntent(context, userId))
        }

        @JvmStatic
        fun newNovelSeriesIntent(context: Context, userId: Int): Intent =
            createIntent(context, FRAGMENT_NOVEL_SERIES).apply { putExtra(Params.USER_ID, userId) }

        @JvmStatic
        fun startNovelSeries(context: Context, userId: Int) {
            context.startActivity(newNovelSeriesIntent(context, userId))
        }

        @JvmStatic
        fun newPublicIllustBookmarksIntent(context: Context, userId: Int): Intent =
            createIntent(context, FRAGMENT_PUBLIC_ILLUST_STAR).apply { putExtra(Params.USER_ID, userId) }

        @JvmStatic
        fun startPublicIllustBookmarks(context: Context, userId: Int) {
            context.startActivity(newPublicIllustBookmarksIntent(context, userId))
        }

        @JvmStatic
        fun newPublicNovelBookmarksIntent(context: Context, userId: Int): Intent =
            createIntent(context, FRAGMENT_PUBLIC_NOVEL_STAR).apply { putExtra(Params.USER_ID, userId) }

        @JvmStatic
        fun startPublicNovelBookmarks(context: Context, userId: Int) {
            context.startActivity(newPublicNovelBookmarksIntent(context, userId))
        }

        @JvmStatic
        fun newRecmdMangaIntent(context: Context): Intent = createIntent(context, FRAGMENT_RECMD_MANGA)

        @JvmStatic
        fun startRecmdManga(context: Context) {
            context.startActivity(newRecmdMangaIntent(context))
        }

        @JvmStatic
        fun newRecmdNovelIntent(context: Context): Intent = createIntent(context, FRAGMENT_RECMD_NOVEL, false)

        @JvmStatic
        fun startRecmdNovel(context: Context) {
            context.startActivity(newRecmdNovelIntent(context))
        }

        @JvmStatic
        fun newNewNovelsIntent(context: Context): Intent = createIntent(context, FRAGMENT_NEW_NOVELS)

        @JvmStatic
        fun startNewNovels(context: Context) {
            context.startActivity(newNewNovelsIntent(context))
        }

        @JvmStatic
        fun newPvIntent(context: Context): Intent = createIntent(context, FRAGMENT_PV, false)

        @JvmStatic
        fun startPv(context: Context) {
            context.startActivity(newPvIntent(context))
        }

        @JvmStatic
        fun newRecmdUserIntent(context: Context): Intent = createIntent(context, FRAGMENT_RECMD_USER)

        @JvmStatic
        fun newRecmdUserIntent(
            context: Context,
            items: List<UserPreviewsBean>?,
            nextUrl: String?,
        ): Intent {
            val intent = createIntent(context, FRAGMENT_RECMD_USER)
            if (!items.isNullOrEmpty()) {
                val bundle = Bundle()
                bundle.putSerializable(Params.USER_MODEL, ArrayList(items))
                intent.putExtra(Params.USER_MODEL, bundle)
            }
            intent.putExtra(Params.URL, nextUrl)
            return intent
        }

        @JvmStatic
        fun startRecmdUser(context: Context) {
            context.startActivity(newRecmdUserIntent(context))
        }

        @JvmStatic
        fun startRecmdUser(context: Context, items: List<UserPreviewsBean>?, nextUrl: String?) {
            context.startActivity(newRecmdUserIntent(context, items, nextUrl))
        }

        @JvmStatic
        fun newIllustCommentsIntent(context: Context, illustId: Int, title: String?): Intent =
            createIntent(context, FRAGMENT_COMMENT).apply {
                putExtra(Params.ILLUST_ID, illustId)
                putExtra(Params.ILLUST_TITLE, title)
            }

        @JvmStatic
        fun startIllustComments(context: Context, illustId: Int, title: String?) {
            context.startActivity(newIllustCommentsIntent(context, illustId, title))
        }

        @JvmStatic
        fun newNovelCommentsIntent(context: Context, novelId: Int, title: String?): Intent =
            createIntent(context, FRAGMENT_COMMENT).apply {
                putExtra(Params.NOVEL_ID, novelId)
                putExtra(Params.ILLUST_TITLE, title)
            }

        @JvmStatic
        fun startNovelComments(context: Context, novelId: Int, title: String?) {
            context.startActivity(newNovelCommentsIntent(context, novelId, title))
        }

        @JvmStatic
        fun newRelatedIllustIntent(context: Context, illustId: Int, title: String?): Intent =
            createIntent(context, FRAGMENT_RELATED_ILLUST).apply {
                putExtra(Params.ILLUST_ID, illustId)
                putExtra(Params.ILLUST_TITLE, title)
            }

        @JvmStatic
        fun startRelatedIllust(context: Context, illustId: Int, title: String?) {
            context.startActivity(newRelatedIllustIntent(context, illustId, title))
        }

        @JvmStatic
        fun newNovelSeriesDetailIntent(context: Context, seriesId: Int): Intent =
            createIntent(context, FRAGMENT_NOVEL_SERIES_DETAIL).apply {
                putExtra(Params.ID, seriesId)
            }

        @JvmStatic
        fun startNovelSeriesDetail(context: Context, seriesId: Int) {
            context.startActivity(newNovelSeriesDetailIntent(context, seriesId))
        }

        @JvmStatic
        fun newMangaSeriesDetailIntent(context: Context, seriesId: Int): Intent =
            createIntent(context, FRAGMENT_MANGA_SERIES_DETAIL).apply {
                putExtra(Params.MANGA_SERIES_ID, seriesId)
            }

        @JvmStatic
        fun startMangaSeriesDetail(context: Context, seriesId: Int) {
            context.startActivity(newMangaSeriesDetailIntent(context, seriesId))
        }

        @JvmStatic
        fun newNovelDetailIntent(context: Context, novel: NovelBean?): Intent =
            createIntent(context, FRAGMENT_NOVEL_DETAIL).apply {
                putExtra(Params.CONTENT, novel)
            }

        @JvmStatic
        fun startNovelDetail(context: Context, novel: NovelBean?) {
            context.startActivity(newNovelDetailIntent(context, novel))
        }

        @JvmStatic
        fun newImageDetailIntent(context: Context, url: String?): Intent =
            createIntent(context, FRAGMENT_IMAGE_DETAIL).apply {
                putExtra(Params.URL, url)
            }

        @JvmStatic
        fun startImageDetail(context: Context, url: String?) {
            context.startActivity(newImageDetailIntent(context, url))
        }

        @JvmStatic
        fun newTagFilterIntent(context: Context, dataType: Int, keyword: String?): Intent =
            createIntent(context, FRAGMENT_TAG_FILTER).apply {
                putExtra(Params.DATA_TYPE, dataType)
                putExtra(EXTRA_KEYWORD, keyword)
            }

        @JvmStatic
        fun startTagFilter(context: Context, dataType: Int, keyword: String?) {
            context.startActivity(newTagFilterIntent(context, dataType, keyword))
        }

        @JvmStatic
        fun newTagStarIntent(context: Context, illustId: Int, type: String?, tagNames: Array<String>?): Intent =
            newTagStarIntent(context, illustId, type, tagNames, null)

        @JvmStatic
        fun newTagStarIntent(
            context: Context,
            illustId: Int,
            type: String?,
            tagNames: Array<String>?,
            lastClass: String?,
        ): Intent {
            val intent = createIntent(context, FRAGMENT_TAG_STAR)
            intent.putExtra(Params.ILLUST_ID, illustId)
            intent.putExtra(Params.DATA_TYPE, type)
            intent.putExtra(Params.TAG_NAMES, tagNames)
            if (!TextUtils.isEmpty(lastClass)) {
                intent.putExtra(Params.LAST_CLASS, lastClass)
            }
            return intent
        }

        @JvmStatic
        fun startTagStar(context: Context, illustId: Int, type: String?, tagNames: Array<String>?) {
            context.startActivity(newTagStarIntent(context, illustId, type, tagNames))
        }

        @JvmStatic
        fun startTagStar(
            context: Context,
            illustId: Int,
            type: String?,
            tagNames: Array<String>?,
            lastClass: String?,
        ) {
            context.startActivity(newTagStarIntent(context, illustId, type, tagNames, lastClass))
        }

        @JvmStatic
        fun newRelatedUserIntent(context: Context, userId: Int): Intent =
            createIntent(context, FRAGMENT_RELATED_USER).apply {
                putExtra(Params.USER_ID, userId)
            }

        @JvmStatic
        fun startRelatedUser(context: Context, userId: Int) {
            context.startActivity(newRelatedUserIntent(context, userId))
        }

        @JvmStatic
        fun newFansIntent(context: Context, userId: Int): Intent =
            createIntent(context, FRAGMENT_FANS).apply {
                putExtra(Params.USER_ID, userId)
            }

        @JvmStatic
        fun startFans(context: Context, userId: Int) {
            context.startActivity(newFansIntent(context, userId))
        }

        @JvmStatic
        fun newUserInfoIntent(context: Context, userDetailResponse: UserDetailResponse?): Intent =
            createIntent(context, FRAGMENT_USER_INFO).apply {
                putExtra(Params.CONTENT, userDetailResponse)
            }

        @JvmStatic
        fun startUserInfo(context: Context, userDetailResponse: UserDetailResponse?) {
            context.startActivity(newUserInfoIntent(context, userDetailResponse))
        }

        private fun createIntent(context: Context, fragment: String): Intent =
            createIntent(context, fragment, true)

        private fun createIntent(context: Context, fragment: String, hideStatusBar: Boolean): Intent =
            Intent(context, TemplateActivity::class.java).apply {
                putExtra(EXTRA_FRAGMENT, fragment)
                putExtra(EXTRA_HIDE_STATUS_BAR, hideStatusBar)
            }
    }
}
