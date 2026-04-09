package ceui.lisa.utils

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import ceui.lisa.R
import ceui.lisa.activities.OutWakeActivity
import ceui.lisa.activities.Shaft
import ceui.lisa.activities.TemplateActivity
import ceui.lisa.activities.VActivity
import ceui.lisa.cache.Cache
import ceui.lisa.core.Container
import ceui.lisa.core.PageData
import ceui.lisa.core.RxRun
import ceui.lisa.core.RxRunnable
import ceui.lisa.core.TryCatchObserverImpl
import ceui.lisa.database.AppDatabase
import ceui.lisa.database.DownloadEntity
import ceui.lisa.database.IllustHistoryEntity
import ceui.lisa.database.MuteEntity
import ceui.lisa.database.SearchEntity
import ceui.lisa.database.UserEntity
import ceui.lisa.feature.FeatureEntity
import ceui.lisa.file.LegacyFile
import ceui.lisa.file.OutPut
import ceui.lisa.fragments.FragmentLogin
import ceui.lisa.helper.IllustNovelFilter
import ceui.lisa.http.ErrorCtrl
import ceui.lisa.http.NullCtrl
import ceui.lisa.http.Retro
import ceui.lisa.interfaces.Back
import ceui.lisa.interfaces.Callback
import ceui.lisa.model.ListIllust
import ceui.lisa.models.FramesBean
import ceui.lisa.models.GifResponse
import ceui.lisa.models.IllustSearchResponse
import ceui.lisa.models.IllustsBean
import ceui.lisa.models.MarkedNovelItem
import ceui.lisa.models.NovelBean
import ceui.lisa.models.NovelDetail
import ceui.lisa.models.NovelSearchResponse
import ceui.lisa.models.NullResponse
import ceui.lisa.models.TagsBean
import ceui.lisa.models.UserBean
import ceui.lisa.models.UserModel
import ceui.lisa.viewmodel.AppLevelViewModel
import ceui.loxia.ObjectPool
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.ZipUtils
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import com.waynejo.androidndkgif.GifEncoder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Callback as RetrofitCallback
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.Comparator
import java.util.HashMap
import kotlin.collections.set
import com.blankj.utilcode.util.ColorUtils.getColor
import com.blankj.utilcode.util.StringUtils.getString

object PixivOperate {
    private val sBack: MutableMap<Int, Back> = HashMap()
    private val gifEncodingWorkSet: MutableMap<Int, Long> = HashMap()
    private const val reEncodeTimeThresholdMillis: Long = 60 * 1000

    @JvmStatic
    fun refreshUserData(userModel: UserModel, callback: RetrofitCallback<UserModel>) {
        val call =
            Retro.getAccountTokenApi().newRefreshToken(
                FragmentLogin.CLIENT_ID,
                FragmentLogin.CLIENT_SECRET,
                FragmentLogin.REFRESH_TOKEN,
                userModel.getRefresh_token(),
                true,
            )
        call.enqueue(callback)
    }

    @JvmStatic
    fun postFollowUser(userID: Int, followType: String) {
        val pendingFollowType = if (Shaft.sSettings.isPrivateStar) Params.TYPE_PRIVATE else followType
        Retro.getAppApi().postFollow(
            Shaft.sUserModel.getAccess_token(),
            userID,
            pendingFollowType,
        )
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : ErrorCtrl<NullResponse>() {
                override fun next(t: NullResponse) {
                    val intent = Intent(Params.LIKED_USER)
                    intent.putExtra(Params.ID, userID)
                    intent.putExtra(Params.IS_LIKED, true)
                    LocalBroadcastManager.getInstance(Shaft.getContext()).sendBroadcast(intent)

                    ObjectPool.followUser(userID.toLong())
                    if (pendingFollowType == Params.TYPE_PUBLIC) {
                        Shaft.appViewModel.updateFollowUserStatus(
                            userID,
                            AppLevelViewModel.FollowUserStatus.FOLLOWED_PUBLIC,
                        )
                        Common.showToast(getString(R.string.like_success_public))
                    } else {
                        Shaft.appViewModel.updateFollowUserStatus(
                            userID,
                            AppLevelViewModel.FollowUserStatus.FOLLOWED_PRIVATE,
                        )
                        Common.showToast(getString(R.string.like_success_private))
                    }
                }
            })
    }

    @JvmStatic
    fun postUnFollowUser(userID: Int) {
        Retro.getAppApi().postUnFollow(Shaft.sUserModel.getAccess_token(), userID)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : ErrorCtrl<NullResponse>() {
                override fun next(t: NullResponse) {
                    val intent = Intent(Params.LIKED_USER)
                    intent.putExtra(Params.ID, userID)
                    intent.putExtra(Params.IS_LIKED, false)
                    LocalBroadcastManager.getInstance(Shaft.getContext()).sendBroadcast(intent)
                    Shaft.appViewModel.updateFollowUserStatus(
                        userID,
                        AppLevelViewModel.FollowUserStatus.NOT_FOLLOW,
                    )
                    ObjectPool.unFollowUser(userID.toLong())
                    Common.showToast(getString(R.string.cancel_like))
                }
            })
    }

    @JvmStatic
    fun postLikeDefaultStarType(illustsBean: IllustsBean?) {
        if (Shaft.sSettings.isPrivateStar) {
            postLike(illustsBean, Params.TYPE_PRIVATE, false, 0)
        } else {
            postLike(illustsBean, Params.TYPE_PUBLIC, false, 0)
        }
    }

    @JvmStatic
    fun postLike(illustsBean: IllustsBean?, starType: String) {
        postLike(illustsBean, starType, false, 0)
    }

    @JvmStatic
    fun postLike(illustsBean: IllustsBean?, starType: String, showRelated: Boolean, index: Int) {
        if (illustsBean == null) {
            return
        }

        if (illustsBean.isIs_bookmarked()) {
            illustsBean.setIs_bookmarked(false)
            Retro.getAppApi().postDislikeIllust(Shaft.sUserModel.getAccess_token(), illustsBean.getId())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : ErrorCtrl<NullResponse>() {
                    override fun next(t: NullResponse) {
                        val intent = Intent(Params.LIKED_ILLUST)
                        intent.putExtra(Params.ID, illustsBean.getId())
                        intent.putExtra(Params.IS_LIKED, false)
                        LocalBroadcastManager.getInstance(Shaft.getContext()).sendBroadcast(intent)

                        Common.showToast(getString(R.string.cancel_like_illust))
                    }
                })
        } else {
            illustsBean.setIs_bookmarked(true)
            Retro.getAppApi().postLikeIllust(
                Shaft.sUserModel.getAccess_token(),
                illustsBean.getId(),
                starType,
            )
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : ErrorCtrl<NullResponse>() {
                    override fun next(t: NullResponse) {
                        val intent = Intent(Params.LIKED_ILLUST)
                        intent.putExtra(Params.ID, illustsBean.getId())
                        intent.putExtra(Params.IS_LIKED, true)
                        LocalBroadcastManager.getInstance(Shaft.getContext()).sendBroadcast(intent)

                        if (Params.TYPE_PUBLIC == starType) {
                            Common.showToast(getString(R.string.like_novel_success_public))
                        } else {
                            Common.showToast(getString(R.string.like_novel_success_private))
                        }
                    }
                })

            if (showRelated) {
                Retro.getAppApi().relatedIllust(Shaft.sUserModel.getAccess_token(), illustsBean.getId())
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : NullCtrl<ListIllust>() {
                        override fun success(t: ListIllust) {
                            val intent = Intent(Params.FRAGMENT_ADD_RELATED_DATA)
                            intent.putExtra(Params.CONTENT, t)
                            intent.putExtra(Params.INDEX, index)
                            LocalBroadcastManager.getInstance(Shaft.getContext()).sendBroadcast(intent)
                        }
                    })
            }
        }
        insertIllustViewHistory(illustsBean)
    }

    @JvmStatic
    fun postLikeNovel(novelBean: NovelBean?, userModel: UserModel, starType: String, view: View?) {
        if (novelBean == null) {
            return
        }

        if (novelBean.isIs_bookmarked()) {
            novelBean.setIs_bookmarked(false)
            Retro.getAppApi().postDislikeNovel(userModel.getAccess_token(), novelBean.getId())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : ErrorCtrl<NullResponse>() {
                    override fun next(t: NullResponse) {
                        val intent = Intent(Params.LIKED_NOVEL)
                        intent.putExtra(Params.ID, novelBean.getId())
                        intent.putExtra(Params.IS_LIKED, false)
                        LocalBroadcastManager.getInstance(Shaft.getContext()).sendBroadcast(intent)

                        if (view is Button) {
                            view.text = getString(R.string.string_180)
                        }
                        Common.showToast(getString(R.string.cancel_like_illust))
                    }
                })
        } else {
            novelBean.setIs_bookmarked(true)
            val pendingType = if (Shaft.sSettings.isPrivateStar) Params.TYPE_PRIVATE else starType
            Retro.getAppApi().postLikeNovel(userModel.getAccess_token(), novelBean.getId(), pendingType)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : ErrorCtrl<NullResponse>() {
                    override fun next(t: NullResponse) {
                        val intent = Intent(Params.LIKED_NOVEL)
                        intent.putExtra(Params.ID, novelBean.getId())
                        intent.putExtra(Params.IS_LIKED, true)
                        LocalBroadcastManager.getInstance(Shaft.getContext()).sendBroadcast(intent)

                        if (view is Button) {
                            view.text = getString(R.string.string_179)
                        }
                        if (Params.TYPE_PUBLIC == pendingType) {
                            Common.showToast(getString(R.string.like_novel_success_public))
                        } else {
                            Common.showToast(getString(R.string.like_novel_success_private))
                        }
                    }
                })
        }
    }

    @JvmStatic
    fun getIllustByID(userModel: UserModel, illustID: Long, context: Context) {
        val tipDialog =
            QMUITipDialog.Builder(context)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(getString(R.string.string_429))
                .create()
        tipDialog.show()
        Retro.getAppApi().getIllustByID(userModel.getAccess_token(), illustID)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : NullCtrl<IllustSearchResponse>() {
                override fun success(t: IllustSearchResponse) {
                    val illust = t.getIllust() ?: return
                    ObjectPool.updateIllust(illust)
                    if (illust.getId() == 0 || !illust.isVisible()) {
                        Common.showToast(R.string.string_206)
                        return
                    }
                    val user = illust.getUser()
                    if (user != null) {
                        Shaft.appViewModel.updateFollowUserStatus(
                            user.getId(),
                            if (user.isIs_followed()) {
                                AppLevelViewModel.FollowUserStatus.FOLLOWED
                            } else {
                                AppLevelViewModel.FollowUserStatus.NOT_FOLLOW
                            },
                        )
                    }

                    val pageData = PageData(listOf(illust))
                    Container.get().addPageToMap(pageData)

                    val intent = Intent(context, VActivity::class.java)
                    intent.putExtra(Params.POSITION, 0)
                    intent.putExtra(Params.PAGE_UUID, pageData.getUUID())
                    context.startActivity(intent)
                }

                override fun must() {
                    super.must()
                    try {
                        tipDialog.dismiss()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
    }

    @JvmStatic
    fun getIllustByID(
        userModel: UserModel,
        illustID: Long,
        context: Context,
        success: Callback<Void>?,
        fail: Callback<Void>?,
    ) {
        Retro.getAppApi().getIllustByID(userModel.getAccess_token(), illustID)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : NullCtrl<IllustSearchResponse>() {
                override fun success(t: IllustSearchResponse) {
                    val illust = t.getIllust()
                    if (illust != null) {
                        val user = illust.getUser()
                        if (user != null) {
                            Shaft.appViewModel.updateFollowUserStatus(
                                user.getId(),
                                if (user.isIs_followed()) {
                                    AppLevelViewModel.FollowUserStatus.FOLLOWED
                                } else {
                                    AppLevelViewModel.FollowUserStatus.NOT_FOLLOW
                                },
                            )
                        }

                        val pageData = PageData(listOf(illust))
                        Container.get().addPageToMap(pageData)

                        val intent = Intent(context, VActivity::class.java)
                        intent.putExtra(Params.POSITION, 0)
                        intent.putExtra(Params.PAGE_UUID, pageData.getUUID())
                        context.startActivity(intent)

                        if (success != null) {
                            @Suppress("NULL_FOR_NONNULL_TYPE")
                            success.doSomething(null as Void)
                        }
                    }
                }

                override fun must(isSuccess: Boolean) {
                    if (!isSuccess && fail != null) {
                        @Suppress("NULL_FOR_NONNULL_TYPE")
                        fail.doSomething(null as Void)
                    }
                }

                override fun must() {
                    super.must()
                    OutWakeActivity.isNetWorking = false
                }
            })
    }

    @JvmStatic
    fun getNovelByID(
        userModel: UserModel,
        novel: Long,
        context: Context,
        callback: Callback<Void>?,
    ) {
        Retro.getAppApi().getNovelByID(userModel.getAccess_token(), novel)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : NullCtrl<NovelSearchResponse>() {
                override fun success(t: NovelSearchResponse) {
                    if (t.getNovel() != null) {
                        TemplateActivity.startNovelDetail(context, t.getNovel())

                        if (callback != null) {
                            @Suppress("NULL_FOR_NONNULL_TYPE")
                            callback.doSomething(null as Void)
                        }
                    } else {
                        Common.showToast("NovelSearchResponse 为空")
                    }
                }

                override fun must() {
                    super.must()
                    OutWakeActivity.isNetWorking = false
                }
            })
    }

    @JvmStatic
    fun getGifInfo(illust: IllustsBean, errorCtrl: ErrorCtrl<GifResponse>) {
        Retro.getAppApi().getGifPackage(Shaft.sUserModel.getAccess_token(), illust.getId())
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(errorCtrl)
    }

    @JvmStatic
    fun muteTag(tagsBean: TagsBean) {
        val muteEntity = MuteEntity()
        val tagName = tagsBean.getName()
        muteEntity.type = Params.MUTE_TAG
        muteEntity.id = tagName.hashCode()
        muteEntity.tagJson = Shaft.sGson.toJson(tagsBean)
        muteEntity.searchTime = System.currentTimeMillis()
        AppDatabase.searchDao(Shaft.getContext()).insertMuteTag(muteEntity)
        IllustNovelFilter.invalidateMutedTags()
    }

    @JvmStatic
    fun updateTag(tagsBean: TagsBean) {
        val muteEntity = MuteEntity()
        val tagName = tagsBean.getName()
        muteEntity.type = Params.MUTE_TAG
        muteEntity.id = tagName.hashCode()
        muteEntity.tagJson = Shaft.sGson.toJson(tagsBean)
        muteEntity.searchTime = System.currentTimeMillis()
        if (tagsBean.isEffective()) {
            Shaft.getContext().resources.getString(R.string.string_356)
        } else {
            Shaft.getContext().resources.getString(R.string.string_357)
        }
        AppDatabase.searchDao(Shaft.getContext()).updateMuteTag(muteEntity)
        IllustNovelFilter.invalidateMutedTags()
    }

    @JvmStatic
    fun muteUser(userBean: UserBean) {
        val muteEntity = MuteEntity()
        muteEntity.type = Params.MUTE_USER
        muteEntity.id = userBean.getId()
        muteEntity.tagJson = Shaft.sGson.toJson(userBean)
        muteEntity.searchTime = System.currentTimeMillis()
        AppDatabase.searchDao(Shaft.getContext()).insertMuteTag(muteEntity)
        IllustNovelFilter.invalidateMutedUsers()
        Common.showToast(Shaft.getContext().getString(R.string.string_382))
    }

    @JvmStatic
    fun unMuteUser(userBean: UserBean) {
        val muteEntity = MuteEntity()
        muteEntity.type = Params.MUTE_USER
        muteEntity.id = userBean.getId()
        muteEntity.tagJson = Shaft.sGson.toJson(userBean)
        muteEntity.searchTime = System.currentTimeMillis()
        AppDatabase.searchDao(Shaft.getContext()).unMuteTag(muteEntity)
        IllustNovelFilter.invalidateMutedUsers()
        Common.showToast(Shaft.getContext().getString(R.string.string_383))
    }

    @JvmStatic
    fun blockUser(userBean: UserBean) {
        val muteEntity = MuteEntity()
        muteEntity.type = Params.BLOCK_USER
        muteEntity.id = userBean.getId()
        muteEntity.tagJson = Shaft.sGson.toJson(userBean)
        muteEntity.searchTime = System.currentTimeMillis()
        AppDatabase.searchDao(Shaft.getContext()).insertMuteTag(muteEntity)
        Common.showToast(Shaft.getContext().getString(R.string.string_382))
    }

    @JvmStatic
    fun unBlockUser(userBean: UserBean) {
        val muteEntity = MuteEntity()
        muteEntity.type = Params.BLOCK_USER
        muteEntity.id = userBean.getId()
        muteEntity.tagJson = Shaft.sGson.toJson(userBean)
        muteEntity.searchTime = System.currentTimeMillis()
        AppDatabase.searchDao(Shaft.getContext()).unMuteTag(muteEntity)
        Common.showToast(Shaft.getContext().getString(R.string.string_383))
    }

    @JvmStatic
    fun muteIllust(illust: IllustsBean) {
        val muteEntity = MuteEntity()
        muteEntity.type = Params.MUTE_ILLUST
        muteEntity.id = illust.getId()
        muteEntity.tagJson = Shaft.sGson.toJson(illust)
        muteEntity.searchTime = System.currentTimeMillis()
        AppDatabase.searchDao(Shaft.getContext()).insertMuteTag(muteEntity)
        IllustNovelFilter.invalidateMutedWorks()
        Common.showToast(Shaft.getContext().getString(R.string.string_384))
    }

    @JvmStatic
    fun muteNovel(novelBean: NovelBean) {
        val muteEntity = MuteEntity()
        muteEntity.type = Params.MUTE_NOVEL
        muteEntity.id = novelBean.getId()
        muteEntity.tagJson = Shaft.sGson.toJson(novelBean)
        muteEntity.searchTime = System.currentTimeMillis()
        AppDatabase.searchDao(Shaft.getContext()).insertMuteTag(muteEntity)
        IllustNovelFilter.invalidateMutedWorks()
        Common.showToast(Shaft.getContext().getString(R.string.string_384))
    }

    @JvmStatic
    fun muteTags(tagsBeans: List<TagsBean>?) {
        if (tagsBeans == null || tagsBeans.isEmpty()) {
            return
        }
        for (tagsBean in tagsBeans) {
            muteTag(tagsBean)
        }
    }

    @JvmStatic
    fun unMuteTag(tagsBean: TagsBean) {
        val muteEntity = MuteEntity()
        val tagName = tagsBean.getName()
        muteEntity.type = Params.MUTE_TAG
        muteEntity.id = tagName.hashCode()
        muteEntity.tagJson = Shaft.sGson.toJson(tagsBean)
        muteEntity.searchTime = System.currentTimeMillis()
        AppDatabase.searchDao(Shaft.getContext()).unMuteTag(muteEntity)
        IllustNovelFilter.invalidateMutedTags()
        Common.showToast(Shaft.getContext().getString(R.string.string_135))
    }

    @JvmStatic
    fun insertIllustViewHistory(illust: IllustsBean?) {
        if (illust == null) {
            return
        }

        if (illust.getId() > 0) {
            val entity = IllustHistoryEntity()
            entity.type = 0
            entity.illustID = illust.getId()
            entity.illustJson = Shaft.sGson.toJson(illust)
            entity.time = System.currentTimeMillis()
            insertViewHistory(entity)
        }
    }

    @JvmStatic
    fun insertNovelViewHistory(novelBean: NovelBean?) {
        if (novelBean == null) {
            return
        }

        if (novelBean.getId() > 0) {
            val entity = IllustHistoryEntity()
            entity.illustID = novelBean.getId()
            entity.type = 1
            entity.illustJson = Shaft.sGson.toJson(novelBean)
            entity.time = System.currentTimeMillis()
            insertViewHistory(entity)
        }
    }

    private fun insertViewHistory(illustHistoryEntity: IllustHistoryEntity) {
        RxRun.runOn(object : RxRunnable<Void>() {
            override fun execute(): Void {
                Common.showLog("插入了 ${illustHistoryEntity.illustID} time ${illustHistoryEntity.time}")
                AppDatabase.downloadDao(Shaft.getContext()).insert(illustHistoryEntity)
                @Suppress("NULL_FOR_NONNULL_TYPE")
                return null as Void
            }
        }, TryCatchObserverImpl())
    }

    @JvmStatic
    fun insertFeature(entity: FeatureEntity?) {
        if (entity == null) {
            return
        }

        RxRun.runOn(object : RxRunnable<Void>() {
            override fun execute(): Void {
                AppDatabase.downloadDao(Shaft.getContext()).insertFeature(entity)
                @Suppress("NULL_FOR_NONNULL_TYPE")
                return null as Void
            }
        }, TryCatchObserverImpl())
    }

    @JvmStatic
    fun insertUser(userEntity: UserEntity?) {
        if (userEntity == null) {
            return
        }

        RxRun.runOn(object : RxRunnable<Void>() {
            override fun execute(): Void {
                AppDatabase.downloadDao(Shaft.getContext()).insertUser(userEntity)
                @Suppress("NULL_FOR_NONNULL_TYPE")
                return null as Void
            }
        }, TryCatchObserverImpl())
    }

    @JvmStatic
    fun insertDownload(downloadEntity: DownloadEntity?) {
        if (downloadEntity == null) {
            return
        }

        RxRun.runOn(object : RxRunnable<Void>() {
            override fun execute(): Void {
                AppDatabase.downloadDao(Shaft.getContext()).insert(downloadEntity)
                @Suppress("NULL_FOR_NONNULL_TYPE")
                return null as Void
            }
        }, TryCatchObserverImpl())
    }

    @JvmStatic
    fun deleteUser(userEntity: UserEntity?) {
        if (userEntity == null) {
            return
        }

        RxRun.runOn(object : RxRunnable<Void>() {
            override fun execute(): Void {
                AppDatabase.downloadDao(Shaft.getContext()).deleteUser(userEntity)
                @Suppress("NULL_FOR_NONNULL_TYPE")
                return null as Void
            }
        }, TryCatchObserverImpl())
    }

    @JvmStatic
    fun insertSearchHistory(key: String?, searchType: Int) {
        if (TextUtils.isEmpty(key)) {
            return
        }
        val searchEntity = createSearchEntity(key!!, searchType, false)
        RxRun.runOn(object : RxRunnable<Void>() {
            override fun execute(): Void {
                writeSearchHistory(searchEntity, true)
                @Suppress("NULL_FOR_NONNULL_TYPE")
                return null as Void
            }
        }, TryCatchObserverImpl())
    }

    @JvmStatic
    fun insertPinnedSearchHistory(key: String?, searchType: Int, pinned: Boolean) {
        if (TextUtils.isEmpty(key)) {
            return
        }
        val searchEntity = createSearchEntity(key!!, searchType, pinned)
        RxRun.runOn(object : RxRunnable<Void>() {
            override fun execute(): Void {
                writeSearchHistory(searchEntity, false)
                @Suppress("NULL_FOR_NONNULL_TYPE")
                return null as Void
            }
        }, TryCatchObserverImpl())
    }

    @JvmStatic
    fun getSearchHistory(key: String, searchType: Int): SearchEntity? {
        val id = key.hashCode() + searchType
        return AppDatabase.searchDao(Shaft.getContext()).getSearchEntity(id)
    }

    private fun createSearchEntity(key: String, searchType: Int, pinned: Boolean): SearchEntity {
        val searchEntity = SearchEntity()
        searchEntity.keyword = key
        searchEntity.searchType = searchType
        searchEntity.searchTime = System.currentTimeMillis()
        searchEntity.id = searchEntity.keyword.hashCode() + searchEntity.searchType
        searchEntity.isPinned = pinned
        Common.showLog("insertSearchHistory $searchType ${searchEntity.id}")
        return searchEntity
    }

    private fun writeSearchHistory(searchEntity: SearchEntity, preservePinnedState: Boolean) {
        if (preservePinnedState) {
            val existEntity = AppDatabase.searchDao(Shaft.getContext()).getSearchEntity(searchEntity.id)
            if (existEntity != null) {
                searchEntity.isPinned = existEntity.isPinned
            }
        }
        AppDatabase.searchDao(Shaft.getContext()).insert(searchEntity)
    }

    @JvmStatic
    fun getListWithoutBooked(response: ListIllust?): List<IllustsBean> {
        val result: MutableList<IllustsBean> = ArrayList()
        if (response == null) {
            return result
        }
        if (response.list.isEmpty()) {
            return result
        }

        for (illustsBean in response.list) {
            if (!illustsBean.isIs_bookmarked()) {
                result.add(illustsBean)
            }
        }
        return result
    }

    @JvmStatic
    fun getListWithStarSize(response: ListIllust?, starSize: Int): List<IllustsBean> {
        val result: MutableList<IllustsBean> = ArrayList()
        if (response == null || response.list.isEmpty()) {
            return result
        }

        for (illustsBean in response.list) {
            if (illustsBean.getTotal_bookmarks() >= starSize) {
                result.add(illustsBean)
            }
        }
        return result
    }

    @JvmStatic
    fun justUnzipFile(fromZipFile: File, toFolder: File) {
        try {
            ZipUtils.unzipFile(fromZipFile, toFolder)
            Common.showLog("justUnzipFile 解压成功")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun encodeGif(context: Context, parentFile: File, illustsBean: IllustsBean) {
        RxRun.runOn(object : RxRunnable<Void>() {
            override fun execute(): Void {
                Common.showLog("encodeGif 开始生成gif图")
                val listfile = parentFile.listFiles()!!

                val allFiles: MutableList<File> = Arrays.asList(*listfile)
                Collections.sort(allFiles, object : Comparator<File> {
                    override fun compare(o1: File, o2: File): Int {
                        return if (
                            Integer.parseInt(o1.name.substring(0, o1.name.length - 4)) >
                            Integer.parseInt(o2.name.substring(0, o2.name.length - 4))
                        ) {
                            1
                        } else {
                            -1
                        }
                    }
                })

                val gifFile = LegacyFile.gifResultFile(context, illustsBean)
                Common.showLog("gifFile ${gifFile.path}")

                val gifEncoder = GifEncoder()

                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeFile(allFiles[0].path, options)
                val outHeight = options.outHeight
                val outWidth = options.outWidth
                Common.showLog("通过Options获取到的图片大小width:$outWidth height: $outHeight")

                gifEncoder.init(
                    outWidth,
                    outHeight,
                    gifFile.path,
                    GifEncoder.EncodingType.ENCODING_TYPE_NORMAL_LOW_MEMORY,
                )

                val gifResponse =
                    Cache.get().getModel(
                        Params.ILLUST_ID + "_" + illustsBean.getId(),
                        GifResponse::class.java,
                    )
                var delayMs = 60
                if (gifResponse != null) {
                    if (allFiles.size == gifResponse.getUgoira_metadata().getFrames().size) {
                        Common.showLog("使用返回的delay 00")
                        val back = sBack[illustsBean.getId()]
                        for (i in allFiles.indices) {
                            Common.showLog("编码中 00 ${allFiles.size} ${i + 1}")
                            gifEncoder.encodeFrame(
                                BitmapFactory.decodeFile(allFiles[i].path),
                                gifResponse.getUgoira_metadata().getFrames()[i].getDelay(),
                            )
                            if (back != null) {
                                val proc = i / (allFiles.size - 1).toFloat()
                                back.invoke(proc)
                            }
                        }
                        sBack.remove(illustsBean.getId())
                    } else {
                        delayMs = gifResponse.getDelay()
                        Common.showLog("使用返回的delay 11")
                        for (i in allFiles.indices) {
                            Common.showLog("编码中 00 ${allFiles.size}")
                            gifEncoder.encodeFrame(
                                BitmapFactory.decodeFile(allFiles[i].path),
                                delayMs,
                            )
                        }
                    }
                } else {
                    Common.showLog("使用返回的delay 22")
                    for (i in allFiles.indices) {
                        Common.showLog("编码中 00 ${allFiles.size}")
                        gifEncoder.encodeFrame(
                            BitmapFactory.decodeFile(allFiles[i].path),
                            delayMs,
                        )
                    }
                }

                Common.showLog("allFiles size ${allFiles.size}")
                gifEncoder.close()
                Common.showLog("gifFile gifFile " + FileUtils.getSize(gifFile))

                val intent = Intent(Params.PLAY_GIF)
                intent.putExtra(Params.ID, illustsBean.getId())
                LocalBroadcastManager.getInstance(Shaft.getContext()).sendBroadcast(intent)
                @Suppress("NULL_FOR_NONNULL_TYPE")
                return null as Void
            }
        }, TryCatchObserverImpl())
    }

    @JvmStatic
    fun encodeGifV2(context: Context, parentFile: File, illustsBean: IllustsBean, autoSave: Boolean) {
        RxRun.runOn(object : RxRunnable<Void>() {
            override fun execute(): Void {
                val currentTimeMillis = System.currentTimeMillis()
                if (gifEncodingWorkSet.containsKey(illustsBean.getId()) &&
                    currentTimeMillis - gifEncodingWorkSet[illustsBean.getId()]!! < reEncodeTimeThresholdMillis
                ) {
                    @Suppress("NULL_FOR_NONNULL_TYPE")
                    return null as Void
                }
                gifEncodingWorkSet[illustsBean.getId()] = currentTimeMillis
                Common.showLog("encodeGif 开始生成gif图")
                val listfile = parentFile.listFiles()!!

                val allFiles: MutableList<File> = Arrays.asList(*listfile)
                Collections.sort(allFiles, object : Comparator<File> {
                    override fun compare(o1: File, o2: File): Int {
                        return if (
                            Integer.parseInt(o1.name.substring(0, o1.name.length - 4)) >
                            Integer.parseInt(o2.name.substring(0, o2.name.length - 4))
                        ) {
                            1
                        } else {
                            -1
                        }
                    }
                })

                val gifFile = LegacyFile.gifResultFile(context, illustsBean)
                Common.showLog("gifFile ${gifFile.path}")

                val animatedGifEncoder = AnimatedGifEncoder()
                val bos = ByteArrayOutputStream()
                animatedGifEncoder.start(bos)
                animatedGifEncoder.setRepeat(0)

                val frameCount = allFiles.size
                val gifResponse =
                    Cache.get().getModel(
                        Params.ILLUST_ID + "_" + illustsBean.getId(),
                        GifResponse::class.java,
                    )
                var delayMs = 60
                if (gifResponse != null) {
                    val framesBeans: List<FramesBean> = gifResponse.getUgoira_metadata().getFrames()
                    if (frameCount == framesBeans.size) {
                        Common.showLog("使用返回的delay 00")

                        for (i in 0 until frameCount) {
                            val bitmap: Bitmap = BitmapFactory.decodeFile(allFiles[i].path)
                            Common.showLog("编码中 00 $frameCount ${i + 1}")
                            animatedGifEncoder.setDelay(framesBeans[i].getDelay())
                            animatedGifEncoder.addFrame(bitmap)

                            val back = sBack[illustsBean.getId()]
                            if (back != null) {
                                val proc = i / (frameCount - 1).toFloat()
                                back.invoke(proc)
                            }
                        }
                        sBack.remove(illustsBean.getId())
                    } else {
                        delayMs = gifResponse.getDelay()
                        Common.showLog("使用返回的delay 11")
                        for (i in 0 until frameCount) {
                            val bitmap: Bitmap = BitmapFactory.decodeFile(allFiles[i].path)
                            Common.showLog("编码中 00 $frameCount")
                            animatedGifEncoder.setDelay(delayMs)
                            animatedGifEncoder.addFrame(bitmap)
                        }
                    }
                } else {
                    Common.showLog("使用返回的delay 22")
                    for (i in 0 until frameCount) {
                        Common.showLog("编码中 00 $frameCount")
                        val bitmap: Bitmap = BitmapFactory.decodeFile(allFiles[i].path)
                        animatedGifEncoder.setDelay(delayMs)
                        animatedGifEncoder.addFrame(bitmap)
                    }
                }

                Common.showLog("allFiles size $frameCount")

                animatedGifEncoder.finish()

                val outStream = FileOutputStream(gifFile.path)
                outStream.write(bos.toByteArray())
                outStream.close()

                if (autoSave) {
                    OutPut.outPutGif(context, gifFile, illustsBean)
                }

                Common.showLog("gifFile gifFile " + FileUtils.getSize(gifFile))
                gifEncodingWorkSet.remove(illustsBean.getId())

                val intent = Intent(Params.PLAY_GIF)
                intent.putExtra(Params.ID, illustsBean.getId())
                LocalBroadcastManager.getInstance(Shaft.getContext()).sendBroadcast(intent)
                @Suppress("NULL_FOR_NONNULL_TYPE")
                return null as Void
            }
        }, TryCatchObserverImpl())
    }

    @JvmStatic
    fun unzipAndPlay(context: Context, illustsBean: IllustsBean) {
        unzipAndPlay(context, illustsBean, false)
    }

    @JvmStatic
    fun unzipAndPlay(context: Context, illustsBean: IllustsBean, autoSave: Boolean) {
        try {
            val fromZip = LegacyFile.gifZipFile(context, illustsBean)
            val toFolder = LegacyFile.gifUnzipFolder(context, illustsBean)
            justUnzipFile(fromZip, toFolder)
            encodeGifV2(context, toFolder, illustsBean, autoSave)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun setBack(illustId: Int, back: Back) {
        sBack[illustId] = back
    }

    @JvmStatic
    fun clearBack() {
        sBack.clear()
    }

    @JvmStatic
    fun postNovelMarker(
        novelMarkerBean: NovelDetail.NovelMarkerBean,
        novelId: Int,
        page: Int,
        view: View?,
    ) {
        val currentMarkPage = novelMarkerBean.getPage()
        if (currentMarkPage == 0 || currentMarkPage > 0 && currentMarkPage != page) {
            novelMarkerBean.setPage(page)
            Retro.getAppApi().postAddNovelMarker(Shaft.sUserModel.getAccess_token(), novelId, page)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : ErrorCtrl<NullResponse>() {
                    override fun next(t: NullResponse) {
                        if (view is ImageView) {
                            view.imageTintList = ColorStateList.valueOf(getColor(R.color.novel_marker_add))
                        }
                        Common.showToast(getString(R.string.string_368, page))
                    }
                })
        } else {
            novelMarkerBean.setPage(0)
            Retro.getAppApi().postDeleteNovelMarker(Shaft.sUserModel.getAccess_token(), novelId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : ErrorCtrl<NullResponse>() {
                    override fun next(t: NullResponse) {
                        if (view is ImageView) {
                            view.imageTintList = ColorStateList.valueOf(getColor(R.color.novel_marker_none))
                        }
                        Common.showToast(getString(R.string.string_369))
                    }
                })
        }
    }

    @JvmStatic
    fun postNovelMarker(marker: MarkedNovelItem.NovelMarker, novelId: Int, view: View?) {
        val page = marker.page
        if (marker.isCancelled) {
            marker.isCancelled = false
            Retro.getAppApi().postAddNovelMarker(Shaft.sUserModel.getAccess_token(), novelId, page)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : ErrorCtrl<NullResponse>() {
                    override fun next(t: NullResponse) {
                        if (view is ImageView) {
                            view.imageTintList = ColorStateList.valueOf(getColor(R.color.novel_marker_add))
                        }
                        Common.showToast(getString(R.string.string_368, page))
                    }
                })
        } else {
            marker.isCancelled = true
            Retro.getAppApi().postDeleteNovelMarker(Shaft.sUserModel.getAccess_token(), novelId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : ErrorCtrl<NullResponse>() {
                    override fun next(t: NullResponse) {
                        if (view is ImageView) {
                            view.imageTintList = ColorStateList.valueOf(getColor(R.color.novel_marker_none))
                        }
                        Common.showToast(getString(R.string.string_369))
                    }
                })
        }
    }
}
