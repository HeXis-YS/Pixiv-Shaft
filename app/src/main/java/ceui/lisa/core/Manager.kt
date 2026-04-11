package ceui.lisa.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import ceui.lisa.database.AppDatabase
import ceui.lisa.database.DownloadEntity
import ceui.lisa.database.DownloadingEntity
import ceui.lisa.download.ImageSaver
import ceui.lisa.helper.Android10DownloadFactory22
import ceui.lisa.helper.SAFactory
import ceui.lisa.interfaces.Callback
import ceui.lisa.model.Holder
import ceui.lisa.utils.Common
import ceui.lisa.utils.DownloadLimitTypeUtil
import ceui.lisa.utils.Params
import ceui.lisa.utils.PixivOperate
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Action
import io.reactivex.rxjava3.functions.Consumer
import rxhttp.RxHttp
import rxhttp.wrapper.callback.UriFactory
import rxhttp.wrapper.entity.Progress
import java.io.File
import java.io.FileNotFoundException

class Manager private constructor() {
    private val mContext: Context = Shaft.getContext()
    var content: MutableList<DownloadItem> = ArrayList()
        private set
    private var handle: Disposable? = null
    private var isRunning = false
    var uuid = ""
        private set
    var currentIllustID = 0
        private set
    private val mCallback: MutableMap<String, Callback<Progress<Uri?>>?> = HashMap()

    fun restore() {
        applyRestoredItems(loadRestoredItems())
    }

    fun restoreAsync() {
        RxRun.runOn(
            object : RxRunnable<List<DownloadItem>>() {
                override fun execute(): List<DownloadItem> {
                    return loadRestoredItems()
                }
            },
            object : TryCatchObserverImpl<List<DownloadItem>>() {
                override fun next(items: List<DownloadItem>) {
                    applyRestoredItems(items)
                }
            },
        )
    }

    private fun loadRestoredItems(): List<DownloadItem> {
        val downloadingEntities: List<DownloadingEntity> =
            AppDatabase.downloadDao(mContext).getAllDownloading()
        val restoredItems: MutableList<DownloadItem> = ArrayList()
        if (Common.isEmpty(downloadingEntities)) {
            return restoredItems
        }
        for (entity in downloadingEntities) {
            try {
                val downloadItem =
                    Shaft.sGson.fromJson(entity.taskGson, DownloadItem::class.java)
                restoredItems.add(downloadItem)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        return restoredItems
    }

    private fun applyRestoredItems(restoredItems: List<DownloadItem>?) {
        if (Common.isEmpty(restoredItems)) {
            return
        }
        Common.showLog("downloadingEntities " + restoredItems!!.size)
        content = ArrayList(restoredItems)
        Common.showToast("下载记录恢复成功")
    }

    fun addTask(bean: DownloadItem) {
        synchronized(this) {
            var isTaskExist = false
            for (item in content) {
                if (item.isSame(bean)) {
                    isTaskExist = true
                }
            }
            if (!isTaskExist) {
                safeAdd(bean)
            }
            if (DownloadLimitTypeUtil.startTaskWhenCreate()) {
                startAll()
            }
        }
    }

    private fun safeAdd(item: DownloadItem) {
        Common.showLog("Manager safeAdd " + item.uuid)
        content.add(item)
        val entity = DownloadingEntity()
        entity.fileName = item.name
        entity.uuid = item.uuid
        entity.taskGson = Shaft.sGson.toJson(item)
        AppDatabase.downloadDao(Shaft.getContext()).insertDownloading(entity)
    }

    private fun complete(item: DownloadItem, isDownloadSuccess: Boolean) {
        if (isDownloadSuccess) {
            item.setState(DownloadItem.DownloadState.SUCCESS)
            setCallback(uuid, null)
            content.remove(item)

            val entity = DownloadingEntity()
            entity.fileName = item.name
            entity.uuid = item.uuid
            entity.taskGson = Shaft.sGson.toJson(item)
            AppDatabase.downloadDao(mContext).deleteDownloading(entity)
            if (Shaft.sSettings.isToastDownloadResult()) {
                Common.showToast(item.name + mContext.getString(R.string.has_been_downloaded))
            }
        } else {
            item.nonius = 0
            item.setState(DownloadItem.DownloadState.FAILED)
        }
    }

    fun addTasks(list: List<DownloadItem>?) {
        if (!Common.isEmpty(list)) {
            for (item in list!!) {
                addTask(item)
            }
        }
    }

    fun startAll() {
        if (!Common.isEmpty(content)) {
            for (item in content) {
                item.setPaused(false)
                if (item.getState() == DownloadItem.DownloadState.FAILED) {
                    item.setState(DownloadItem.DownloadState.INIT)
                }
            }
        }
        if (isRunning) {
            Common.showLog("Manager 正在下载中，不用多次start")
            return
        }
        isRunning = true
        loop()
    }

    fun startOne(uuid: String) {
        for (downloadItem in content) {
            if (downloadItem.uuid == uuid) {
                downloadItem.setPaused(false)
                if (downloadItem.getState() == DownloadItem.DownloadState.FAILED) {
                    downloadItem.setState(DownloadItem.DownloadState.INIT)
                }
                Common.showLog("已开始 $uuid")
                break
            }
        }

        if (isRunning) {
            Common.showLog("Manager 正在下载中，不用多次start")
            return
        }
        isRunning = true
        loop()
    }

    fun stopAll() {
        for (item in content) {
            item.setPaused(true)
        }
        isRunning = false
        handle?.dispose()
        Common.showLog("已经停止")
    }

    fun stopOne(uuid: String) {
        for (item in content) {
            if (item.uuid == uuid) {
                item.setPaused(true)
                Common.showLog("已暂停 $uuid")
                break
            }
        }
        if (this.uuid == uuid && handle != null) {
            handle!!.dispose()
        }
    }

    fun clearAll() {
        stopAll()
        AppDatabase.downloadDao(mContext).deleteAllDownloading()
        content.clear()
    }

    fun clearOne(uuid: String) {
        stopOne(uuid)
        val downloadItem = content.firstOrNull { it.uuid == uuid }
        if (downloadItem != null) {
            val entity = DownloadingEntity()
            entity.fileName = downloadItem.name
            entity.uuid = downloadItem.uuid
            entity.taskGson = Shaft.sGson.toJson(downloadItem)
            AppDatabase.downloadDao(mContext).deleteDownloading(entity)
            content.remove(downloadItem)
        }
    }

    private fun loop() {
        val activeItems = content.filter { !it.isPaused() }
        if (Common.isEmpty(activeItems)) {
            isRunning = false
            Common.showLog("Manager 已经全部下载完成")
            return
        }
        if (!isRunning) {
            return
        }

        val item = getFirstOne()
        if (item != null) {
            downloadOne(mContext, item)
        } else {
            stopAll()
        }
    }

    private fun getFirstOne(): DownloadItem? {
        for (downloadItem in content) {
            if (downloadItem.getState() == DownloadItem.DownloadState.INIT ||
                downloadItem.getState() == DownloadItem.DownloadState.DOWNLOADING
            ) {
                return downloadItem
            }
        }
        return null
    }

    private fun downloadOne(context: Context, downloadItem: DownloadItem) {
        if (!DownloadLimitTypeUtil.canDownloadNow()) {
            stopAll()
            return
        }

        val factory: UriFactory =
            if (Shaft.sSettings.getDownloadWay() == 0 || downloadItem.illust.isGif()) {
                Android10DownloadFactory22(context, downloadItem)
            } else {
                SAFactory(context, downloadItem)
            }
        currentIllustID = downloadItem.illust.id
        Common.showLog("Manager 下载单个 当前进度" + downloadItem.nonius)
        uuid = downloadItem.uuid
        val fileSize = getDownloadedLength(factory.query(), context)
        val passSize: Long =
            if (!downloadItem.shouldStartNewDownload() && fileSize >= 0) {
                fileSize
            } else {
                0L
            }

        val requestUrl = downloadItem.url ?: throw IllegalStateException("Download url is null")
        handle =
            RxHttp
                .get(requestUrl)
                .addHeader(Params.MAP_KEY, Params.IMAGE_REFERER)
                .setRangeHeader(passSize, true)
                .toDownloadObservable(factory, !downloadItem.shouldStartNewDownload())
                .onProgress(
                    AndroidSchedulers.mainThread(),
                    Consumer<Progress<Uri?>> { progress ->
                        val currentProgress = progress.progress
                        downloadItem.nonius = currentProgress
                        downloadItem.setState(DownloadItem.DownloadState.DOWNLOADING)
                        Common.showLog("currentProgress $currentProgress")
                        try {
                            val c = getCallback(uuid)
                            c?.doSomething(progress)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                ).doFinally(
                    Action {
                        currentIllustID = 0
                        loop()
                        Common.showLog("doFinally ")
                    },
                ).subscribe(
                    { s: Uri ->
                        Common.showLog("downloadOne $s")

                        if (downloadItem.illust.isGif()) {
                            Shaft.getMMKV().encode(
                                Params.ILLUST_ID + "_" + downloadItem.illust.id,
                                true,
                            )
                            PixivOperate.unzipAndPlay(
                                context,
                                downloadItem.illust,
                                downloadItem.isAutoSave(),
                            )
                        }

                        run {
                            val intent = Intent(Params.DOWNLOAD_ING)
                            val holder = Holder()
                            holder.code = Params.DOWNLOAD_SUCCESS
                            holder.index = content.indexOf(downloadItem)
                            holder.downloadItem = downloadItem
                            intent.putExtra(Params.CONTENT, holder)
                            LocalBroadcastManager
                                .getInstance(Shaft.getContext())
                                .sendBroadcast(intent)
                        }

                        run {
                            val downloadEntity = DownloadEntity()
                            downloadEntity.illustGson = Shaft.sGson.toJson(downloadItem.illust)
                            downloadEntity.fileName = downloadItem.name
                            downloadEntity.downloadTime = System.currentTimeMillis()
                            downloadEntity.filePath =
                                if (factory is SAFactory) {
                                    factory.getUri().toString()
                                } else {
                                    (factory as Android10DownloadFactory22).fileUri.toString()
                                }
                            AppDatabase.downloadDao(Shaft.getContext()).insert(downloadEntity)

                            val intent = Intent(Params.DOWNLOAD_FINISH)
                            intent.putExtra(Params.CONTENT, downloadEntity)
                            LocalBroadcastManager
                                .getInstance(Shaft.getContext())
                                .sendBroadcast(intent)
                        }

                        object : ImageSaver() {
                            override fun whichFile(): File? {
                                val uri: Uri = factory.query() ?: return null
                                val path = uri.path ?: return null
                                return File(path)
                            }
                        }.execute()

                        complete(downloadItem, true)
                    },
                    { throwable: Throwable ->
                        throwable.printStackTrace()
                        if (Shaft.sSettings.isToastDownloadResult()) {
                            Common.showToast("下载失败，原因：" + throwable.toString())
                        }
                        Common.showLog("下载失败，原因：" + throwable.toString())
                        complete(downloadItem, false)
                        val intent = Intent(Params.DOWNLOAD_ING)
                        val holder = Holder()
                        holder.code = Params.DOWNLOAD_FAILED
                        holder.index = content.indexOf(downloadItem)
                        holder.downloadItem = downloadItem
                        intent.putExtra(Params.CONTENT, holder)
                        LocalBroadcastManager
                            .getInstance(Shaft.getContext())
                            .sendBroadcast(intent)
                    },
                )
    }

    fun getCallback(uuid: String): Callback<Progress<Uri?>>? {
        return mCallback.getOrDefault(uuid, null)
    }

    fun clearCallback() {
        mCallback.clear()
    }

    fun setCallback(callback: Callback<Progress<Uri?>>?) {
        mCallback[""] = callback
    }

    fun setCallback(uuid: String, callback: Callback<Progress<Uri?>>?) {
        mCallback[uuid] = callback
    }

    private fun getDownloadedLength(uri: Uri?, context: Context): Long {
        if (uri == null) {
            return -1L
        }
        return try {
            if ("content" == uri.scheme) {
                context.contentResolver
                    .openAssetFileDescriptor(uri, "r")
                    ?.use { descriptor -> descriptor.length }
                    ?: -1L
            } else {
                val path = uri.path ?: return -1L
                val file = File(path)
                if (file.exists()) file.length() else -1L
            }
        } catch (_: FileNotFoundException) {
            -1L
        }
    }

    companion object {
        @JvmStatic
        fun get(): Manager {
            return SingletonHolder.INSTANCE
        }
    }

    private object SingletonHolder {
        val INSTANCE: Manager = Manager()
    }
}
