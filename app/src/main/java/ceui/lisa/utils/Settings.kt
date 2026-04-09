package ceui.lisa.utils

import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.PathUtils
import ceui.lisa.helper.NavigationLocationHelper
import ceui.lisa.helper.ThemeHelper

/**
 * A class about all the application settings.
 */
class Settings {
    var themeIndex = 0
    var lineCount = 2
    private var mainViewR18 = false
    private var currentProgress = 0L
    private var trendsForPrivate = false
    private var viewHistoryAnimate = true
    private var settingsAnimate = true
    private var deleteStarIllust = false
    private var relatedIllustNoLimit = true
    private var showLargeThumbnailImage = false
    private var showOriginalPreviewImage = false
    private var showPixivDialog = true
    private var privateStar = false
    private var showLikeButton = true
    private var directDownloadAllImage = true
    private var saveViewHistory = true
    private var r18DivideSave = false
    private var AIDivideSave = false
    private var hideStarButtonAtMyCollection = false
    private var starWithTagSelectAll = false
    var isHasP0 = false
    private var illustPath = ""
    private var novelPath = ""
    private var gifResultPath = ""
    private var gifZipPath = ""
    private var gifUnzipPath = ""
    private var webDownloadPath = ""
    private var novelHolderColor = 0
    private var novelHolderTextColor = 0
    private var bottomBarOrder = 0
    private var reverseDialogNeverShowAgain = false
    var appLanguage = ""
        get() = if (!TextUtils.isEmpty(field)) field else ALL_LANGUAGE[0]
    var fileNameJson = ""
    var rootPathUri = ""
    private var downloadWay = 0
    private var filterComment = false
    private var transformerType = 5
    private var showRelatedWhenStar = true
    private var globalSwipeBack = true
    private var illustLongPressDownload = false
    private var saveForSeparateAuthorStatus = 0
    private var autoPostLikeWhenDownload = false
    private var r18FilterDefaultEnable = false
    private var toastDownloadResult = true
    @Transient
    private var r18FilterTempEnableInitialed = false
    @Transient
    private var r18FilterTempEnable = false
    var searchDefaultSortType = ""
        get() = if (TextUtils.isEmpty(field)) PixivSearchParamUtil.POPULAR_SORT_VALUE else field
    private var navigationInitPosition = NavigationLocationHelper.TUIJIAN
    private var downloadLimitType = 0
    private var illustDetailKeepScreenOn = false
    private var themeType = ""
    private var searchFilter = ""

    fun getCurrentProgress(): Long = currentProgress

    fun setCurrentProgress(currentProgress: Long) {
        this.currentProgress = currentProgress
    }

    fun isToastDownloadResult(): Boolean = toastDownloadResult

    fun setToastDownloadResult(toastDownloadResult: Boolean) {
        this.toastDownloadResult = toastDownloadResult
    }

    fun getDownloadWay(): Int = downloadWay

    fun setDownloadWay(downloadWay: Int) {
        this.downloadWay = downloadWay
    }

    fun isR18DivideSave(): Boolean = r18DivideSave

    fun setR18DivideSave(r18DivideSave: Boolean) {
        this.r18DivideSave = r18DivideSave
    }

    fun isAIDivideSave(): Boolean = AIDivideSave

    fun setAIDivideSave(AIDivideSave: Boolean) {
        this.AIDivideSave = AIDivideSave
    }

    fun getNovelPath(): String = if (TextUtils.isEmpty(novelPath)) FILE_LOG_PATH else novelPath

    fun isPrivateStar(): Boolean = privateStar

    fun setPrivateStar(privateStar: Boolean) {
        this.privateStar = privateStar
    }

    fun setNovelPath(novelPath: String) {
        this.novelPath = novelPath
    }

    fun getThemeType(): ThemeHelper.ThemeType {
        return try {
            ThemeHelper.ThemeType.valueOf(themeType)
        } catch (_: Exception) {
            ThemeHelper.ThemeType.DEFAULT_MODE
        }
    }

    fun setThemeType(activity: AppCompatActivity, themeType: ThemeHelper.ThemeType) {
        this.themeType = themeType.name
        ThemeHelper.applyTheme(activity, themeType)
    }

    fun isDeleteStarIllust(): Boolean = deleteStarIllust

    fun setDeleteStarIllust(pDeleteStarIllust: Boolean) {
        deleteStarIllust = pDeleteStarIllust
    }

    fun isSaveViewHistory(): Boolean = saveViewHistory

    fun setSaveViewHistory(saveViewHistory: Boolean) {
        this.saveViewHistory = saveViewHistory
    }

    fun getSearchFilter(): String = if (TextUtils.isEmpty(searchFilter)) "" else searchFilter

    fun setSearchFilter(searchFilter: String) {
        this.searchFilter = searchFilter
    }

    fun isRelatedIllustNoLimit(): Boolean = relatedIllustNoLimit

    fun setRelatedIllustNoLimit(relatedIllustNoLimit: Boolean) {
        this.relatedIllustNoLimit = relatedIllustNoLimit
    }

    fun isMainViewR18(): Boolean = mainViewR18

    fun setMainViewR18(mainViewR18: Boolean) {
        this.mainViewR18 = mainViewR18
    }

    fun isViewHistoryAnimate(): Boolean = viewHistoryAnimate

    fun setViewHistoryAnimate(viewHistoryAnimate: Boolean) {
        this.viewHistoryAnimate = viewHistoryAnimate
    }

    fun isSettingsAnimate(): Boolean = settingsAnimate

    fun setSettingsAnimate(settingsAnimate: Boolean) {
        this.settingsAnimate = settingsAnimate
    }

    fun isDirectDownloadAllImage(): Boolean = directDownloadAllImage

    fun setDirectDownloadAllImage(directDownloadAllImage: Boolean) {
        this.directDownloadAllImage = directDownloadAllImage
    }

    fun getIllustPath(): String = if (TextUtils.isEmpty(illustPath)) FILE_PATH_SINGLE else illustPath

    fun setIllustPath(illustPath: String) {
        this.illustPath = illustPath
    }

    fun getGifResultPath(): String = if (TextUtils.isEmpty(gifResultPath)) FILE_GIF_RESULT_PATH else gifResultPath

    fun setGifResultPath(gifResultPath: String) {
        this.gifResultPath = gifResultPath
    }

    fun getGifZipPath(): String = if (TextUtils.isEmpty(gifZipPath)) FILE_GIF_PATH else gifZipPath

    fun setGifZipPath(gifZipPath: String) {
        this.gifZipPath = gifZipPath
    }

    fun getGifUnzipPath(): String = if (TextUtils.isEmpty(gifUnzipPath)) FILE_GIF_CHILD_PATH else gifUnzipPath

    fun setGifUnzipPath(gifUnzipPath: String) {
        this.gifUnzipPath = gifUnzipPath
    }

    fun getWebDownloadPath(): String = if (TextUtils.isEmpty(webDownloadPath)) WEB_DOWNLOAD_PATH else "webDownloadPath"

    fun setWebDownloadPath(webDownloadPath: String) {
        this.webDownloadPath = webDownloadPath
    }

    fun isTrendsForPrivate(): Boolean = trendsForPrivate

    fun setTrendsForPrivate(trendsForPrivate: Boolean) {
        this.trendsForPrivate = trendsForPrivate
    }

    fun isShowPixivDialog(): Boolean = showPixivDialog

    fun setShowPixivDialog(showPixivDialog: Boolean) {
        this.showPixivDialog = showPixivDialog
    }

    fun isReverseDialogNeverShowAgain(): Boolean = reverseDialogNeverShowAgain

    fun setReverseDialogNeverShowAgain(reverseDialogNeverShowAgain: Boolean) {
        this.reverseDialogNeverShowAgain = reverseDialogNeverShowAgain
    }

    fun isShowLikeButton(): Boolean = showLikeButton

    fun setShowLikeButton(pShowLikeButton: Boolean) {
        showLikeButton = pShowLikeButton
    }

    fun getNovelHolderColor(): Int = novelHolderColor

    fun setNovelHolderColor(novelHolderColor: Int) {
        this.novelHolderColor = novelHolderColor
    }

    fun getNovelHolderTextColor(): Int = novelHolderTextColor

    fun setNovelHolderTextColor(novelHolderTextColor: Int) {
        this.novelHolderTextColor = novelHolderTextColor
    }

    fun getBottomBarOrder(): Int = bottomBarOrder

    fun setBottomBarOrder(bottomBarOrder: Int) {
        this.bottomBarOrder = bottomBarOrder
    }

    fun isHideStarButtonAtMyCollection(): Boolean = hideStarButtonAtMyCollection

    fun setHideStarButtonAtMyCollection(hideStarButtonAtMyCollection: Boolean) {
        this.hideStarButtonAtMyCollection = hideStarButtonAtMyCollection
    }

    fun isStarWithTagSelectAll(): Boolean = starWithTagSelectAll

    fun setStarWithTagSelectAll(starWithTagSelectAll: Boolean) {
        this.starWithTagSelectAll = starWithTagSelectAll
    }

    fun isFilterComment(): Boolean = filterComment

    fun setFilterComment(filterComment: Boolean) {
        this.filterComment = filterComment
    }

    fun getTransformerType(): Int = transformerType

    fun setTransformerType(transformerType: Int) {
        this.transformerType = transformerType
    }

    fun isShowRelatedWhenStar(): Boolean = showRelatedWhenStar

    fun setShowRelatedWhenStar(showRelatedWhenStar: Boolean) {
        this.showRelatedWhenStar = showRelatedWhenStar
    }

    fun isGlobalSwipeBack(): Boolean = globalSwipeBack

    fun setGlobalSwipeBack(globalSwipeBack: Boolean) {
        this.globalSwipeBack = globalSwipeBack
    }

    fun isIllustLongPressDownload(): Boolean = illustLongPressDownload

    fun setIllustLongPressDownload(illustLongPressDownload: Boolean) {
        this.illustLongPressDownload = illustLongPressDownload
    }

    fun isAutoPostLikeWhenDownload(): Boolean = autoPostLikeWhenDownload

    fun setAutoPostLikeWhenDownload(autoPostLikeWhenDownload: Boolean) {
        this.autoPostLikeWhenDownload = autoPostLikeWhenDownload
    }

    fun isShowOriginalPreviewImage(): Boolean = showOriginalPreviewImage

    fun setShowOriginalPreviewImage(showOriginalPreviewImage: Boolean) {
        this.showOriginalPreviewImage = showOriginalPreviewImage
    }

    fun isR18FilterDefaultEnable(): Boolean = r18FilterDefaultEnable

    fun setR18FilterDefaultEnable(r18FilterDefaultEnable: Boolean) {
        this.r18FilterDefaultEnable = r18FilterDefaultEnable
    }

    fun isR18FilterTempEnable(): Boolean {
        if (!r18FilterTempEnableInitialed) {
            r18FilterTempEnable = r18FilterDefaultEnable
            r18FilterTempEnableInitialed = true
        }
        return r18FilterTempEnable
    }

    fun setR18FilterTempEnable(r18FilterTempEnable: Boolean) {
        this.r18FilterTempEnable = r18FilterTempEnable
    }

    fun getNavigationInitPosition(): String = navigationInitPosition

    fun setNavigationInitPosition(navigationInitPosition: String) {
        this.navigationInitPosition = navigationInitPosition
    }

    fun getSaveForSeparateAuthorStatus(): Int = saveForSeparateAuthorStatus

    fun setSaveForSeparateAuthorStatus(saveForSeparateAuthorStatus: Int) {
        this.saveForSeparateAuthorStatus = saveForSeparateAuthorStatus
    }

    fun getDownloadLimitType(): Int = downloadLimitType

    fun setDownloadLimitType(downloadLimitType: Int) {
        this.downloadLimitType = downloadLimitType
    }

    fun isShowLargeThumbnailImage(): Boolean = showLargeThumbnailImage

    fun setShowLargeThumbnailImage(showLargeThumbnailImage: Boolean) {
        this.showLargeThumbnailImage = showLargeThumbnailImage
    }

    fun isIllustDetailKeepScreenOn(): Boolean = illustDetailKeepScreenOn

    fun setIllustDetailKeepScreenOn(illustDetailKeepScreenOn: Boolean) {
        this.illustDetailKeepScreenOn = illustDetailKeepScreenOn
    }

    companion object {
        @JvmField
        val ALL_LANGUAGE = arrayOf("简体中文", "日本語", "English", "繁體中文", "русский", "한국어")

        @JvmField
        val FILE_PATH_SINGLE: String = PathUtils.getExternalPicturesPath() + "/ShaftImages"

        @JvmField
        val FILE_PATH_NOVEL: String = PathUtils.getExternalDownloadsPath() + "/ShaftNovels"

        @JvmField
        val FILE_PATH_SINGLE_R18: String = PathUtils.getExternalPicturesPath() + "/ShaftImages-R18"

        @JvmField
        val FILE_GIF_PATH: String = PathUtils.getExternalDownloadsPath()

        @JvmField
        val FILE_LOG_PATH: String = PathUtils.getExternalDownloadsPath() + "/ShaftFiles"

        @JvmField
        val FILE_GIF_CHILD_PATH: String = PathUtils.getExternalAppCachePath()

        @JvmField
        val FILE_GIF_RESULT_PATH: String = PathUtils.getExternalPicturesPath() + "/ShaftGIFs"

        @JvmField
        val WEB_DOWNLOAD_PATH: String = PathUtils.getExternalPicturesPath() + "/ShaftWeb"

        @JvmField
        val FILE_PATH_BACKUP: String = PathUtils.getExternalDownloadsPath() + "/ShaftBackups"
    }
}
