package ceui.lisa.download

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.qmuiteam.qmui.skin.QMUISkinManager
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction
import ceui.lisa.R
import ceui.lisa.activities.BaseActivity
import ceui.lisa.activities.Shaft
import ceui.lisa.cache.Cache
import ceui.lisa.core.DownloadItem
import ceui.lisa.core.Manager
import ceui.lisa.file.LegacyFile
import ceui.lisa.file.OutPut
import ceui.lisa.file.SAFile
import ceui.lisa.http.ErrorCtrl
import ceui.lisa.interfaces.Callback
import ceui.lisa.interfaces.FeedBack
import ceui.lisa.models.GifResponse
import ceui.lisa.models.IllustsBean
import ceui.lisa.models.ImageUrlsBean
import ceui.lisa.models.NovelBean
import ceui.lisa.models.NovelDetail
import ceui.lisa.models.NovelSeriesItem
import ceui.lisa.utils.Common
import ceui.lisa.utils.Params
import ceui.lisa.utils.PixivOperate
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object IllustDownload {
    private fun buildDownloadItem(illust: IllustsBean, index: Int): DownloadItem? {
        return buildDownloadItem(illust, index, Params.IMAGE_RESOLUTION_ORIGINAL)
    }

    private fun buildDownloadItem(
        illust: IllustsBean,
        index: Int,
        imageResolution: String,
    ): DownloadItem? {
        return if (illust.isGif) {
            null
        } else if (illust.page_count == 1) {
            val item = DownloadItem(illust, 0)
            item.url = getUrl(illust, 0, imageResolution)
            item.showUrl = getShowUrl(illust, 0)
            item
        } else {
            val item = DownloadItem(illust, index)
            item.url = getUrl(illust, index, imageResolution)
            item.showUrl = getShowUrl(illust, index)
            item
        }
    }

    @JvmStatic
    fun downloadIllustFirstPage(illust: IllustsBean, activity: BaseActivity<*>) {
        check(activity) { downloadIllustFirstPage(illust) }
    }

    @JvmStatic
    fun downloadIllustFirstPageWithResolution(
        illust: IllustsBean,
        imageResolution: String,
        activity: BaseActivity<*>,
    ) {
        check(activity) {
            if (illust.page_count == 1) {
                val item = buildDownloadItem(illust, 0, imageResolution)!!
                Common.showToast('1' + Shaft.getContext().getString(R.string.has_been_added))
                Manager.get().addTask(item)
            }
        }
    }

    @JvmStatic
    fun downloadIllustFirstPage(illust: IllustsBean) {
        downloadIllustFirstPageWithResolution(illust, Params.IMAGE_RESOLUTION_ORIGINAL)
    }

    @JvmStatic
    fun downloadIllustFirstPageWithResolution(
        illust: IllustsBean,
        imageResolution: String,
    ) {
        if (illust.page_count == 1) {
            val item = buildDownloadItem(illust, 0, imageResolution)!!
            Common.showToast('1' + Shaft.getContext().getString(R.string.has_been_added))
            Manager.get().addTask(item)
        }
    }

    @JvmStatic
    fun downloadIllustCertainPage(illust: IllustsBean, index: Int, activity: BaseActivity<*>) {
        check(activity) {
            if (illust.page_count == 1) {
                downloadIllustFirstPage(illust)
            } else {
                val item = buildDownloadItem(illust, index)!!
                Common.showToast('1' + Shaft.getContext().getString(R.string.has_been_added))
                Manager.get().addTask(item)
            }
        }
    }

    @JvmStatic
    fun downloadIllustAllPages(illust: IllustsBean, activity: BaseActivity<*>) {
        check(activity) { downloadIllustAllPages(illust) }
    }

    @JvmStatic
    fun downloadIllustAllPagesWithResolution(
        illust: IllustsBean,
        imageResolution: String,
        activity: BaseActivity<*>,
    ) {
        check(activity) {
            if (illust.page_count == 1) {
                downloadIllustFirstPage(illust, activity)
            } else {
                val tempList = ArrayList<DownloadItem>()
                for (i in 0 until illust.page_count) {
                    val item = buildDownloadItem(illust, i, imageResolution)!!
                    tempList.add(item)
                }
                Common.showToast(tempList.size.toString() + Shaft.getContext().getString(R.string.has_been_added))
                Manager.get().addTasks(tempList)
            }
        }
    }

    @JvmStatic
    fun downloadIllustAllPages(illust: IllustsBean) {
        if (illust.isGif) {
            downloadGif(illust)
        } else if (illust.page_count == 1) {
            downloadIllustFirstPage(illust)
        } else {
            val tempList = ArrayList<DownloadItem>()
            for (i in 0 until illust.page_count) {
                val item = buildDownloadItem(illust, i)!!
                tempList.add(item)
            }
            Common.showToast(tempList.size.toString() + Shaft.getContext().getString(R.string.has_been_added))
            Manager.get().addTasks(tempList)
        }
    }

    @JvmStatic
    fun downloadCheckedIllustAllPages(beans: List<IllustsBean>, activity: BaseActivity<*>) {
        check(activity) {
            val tempList = ArrayList<DownloadItem>()
            var taskCount = 0
            for (illust in beans) {
                if (illust.isChecked) {
                    if (illust.isGif) {
                        downloadGif(illust)
                        taskCount++
                    } else if (illust.page_count == 1) {
                        val item = DownloadItem(illust, 0)
                        item.url = getUrl(illust, 0)
                        item.showUrl = getShowUrl(illust, 0)
                        tempList.add(item)
                        taskCount++
                    } else {
                        for (j in 0 until illust.page_count) {
                            val item = DownloadItem(illust, j)
                            item.url = getUrl(illust, j)
                            item.showUrl = getShowUrl(illust, j)
                            tempList.add(item)
                            taskCount++
                        }
                    }
                }
            }
            Common.showToast(taskCount.toString() + Shaft.getContext().getString(R.string.has_been_added))
            Manager.get().addTasks(tempList)
        }
    }

    @JvmStatic
    fun downloadGif(response: GifResponse, illust: IllustsBean): DownloadItem {
        return downloadGif(response, illust, false)
    }

    @JvmStatic
    fun downloadGif(response: GifResponse, illust: IllustsBean, autoSave: Boolean): DownloadItem {
        val item = DownloadItem(illust, 0)
        item.autoSave = autoSave
        item.url = response.ugoira_metadata.zip_urls.medium
        item.showUrl = illust.image_urls.medium
        Manager.get().addTask(item)
        return item
    }

    @JvmStatic
    fun downloadGif(illustsBean: IllustsBean) {
        if (!illustsBean.isGif) {
            return
        }
        PixivOperate.getGifInfo(illustsBean, object : ErrorCtrl<GifResponse>() {
            override fun next(gifResponse: GifResponse) {
                Cache.get().saveModel(Params.ILLUST_ID + "_" + illustsBean.id, gifResponse)
                downloadGif(gifResponse, illustsBean, true)
            }
        })
    }

    @JvmStatic
    fun downloadNovel(
        activity: BaseActivity<*>,
        novelSeriesItem: NovelSeriesItem,
        content: String,
        targetCallback: Callback<Uri>?,
    ) {
        val displayName =
            FileCreator.deleteSpecialWords(
                "NovelSeries_" + novelSeriesItem.id + "_Chapter_1~" +
                    novelSeriesItem.content_count + "_" + novelSeriesItem.title + ".txt",
            )
        downloadNovel(activity, displayName, content, targetCallback)
    }

    @JvmStatic
    fun downloadNovel(
        activity: BaseActivity<*>,
        novelBean: NovelBean,
        novelDetail: NovelDetail,
        targetCallback: Callback<Uri>?,
    ) {
        val displayName = FileCreator.deleteSpecialWords("Novel_" + novelBean.id + "_" + novelBean.title + ".txt")
        val content = novelDetail.novel_text
        downloadNovel(activity, displayName, content, targetCallback)
    }

    @JvmStatic
    fun downloadNovel(
        activity: BaseActivity<*>,
        displayName: String,
        content: String,
        targetCallback: Callback<Uri>?,
    ) {
        check(activity, object : FeedBack {
            override fun doSomething() {
                val textFile = LegacyFile.textFile(activity, displayName)
                try {
                    val outStream: OutputStream = FileOutputStream(textFile)
                    outStream.write(content.toByteArray())
                    outStream.close()
                    Common.showLog("downloadNovel displayName $displayName")
                    OutPut.outPutNovel(activity, textFile, displayName)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val fileURI =
                    FileProvider.getUriForFile(
                        activity,
                        activity.applicationContext.packageName + ".provider",
                        textFile,
                    )
                targetCallback?.doSomething(fileURI)
            }
        })
    }

    @JvmStatic
    fun downloadFile(
        activity: BaseActivity<*>,
        displayName: String,
        content: String,
        targetCallback: Callback<Uri>?,
    ) {
        check(activity, object : FeedBack {
            override fun doSomething() {
                val textFile = LegacyFile.textFile(activity, displayName)
                try {
                    val outStream: OutputStream = FileOutputStream(textFile)
                    outStream.write(content.toByteArray())
                    outStream.close()
                    Common.showLog("downloadFile displayName $displayName")
                    OutPut.outPutFile(activity, textFile, displayName)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val fileURI =
                    FileProvider.getUriForFile(
                        activity,
                        activity.applicationContext.packageName + ".provider",
                        textFile,
                    )
                targetCallback?.doSomething(fileURI)
            }
        })
    }

    @JvmStatic
    fun downloadBackupFile(
        activity: BaseActivity<*>,
        displayName: String,
        content: String,
        targetCallback: Callback<Uri>?,
    ) {
        check(activity, object : FeedBack {
            override fun doSomething() {
                val textFile = LegacyFile.textFile(activity, displayName)
                try {
                    val outStream: OutputStream = FileOutputStream(textFile)
                    outStream.write(content.toByteArray())
                    outStream.close()
                    Common.showLog("downloadBackupFile displayName $displayName")
                    OutPut.outPutBackupFile(activity, textFile, displayName)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val fileURI =
                    FileProvider.getUriForFile(
                        activity,
                        activity.applicationContext.packageName + ".provider",
                        textFile,
                    )
                targetCallback?.doSomething(fileURI)
            }
        })
    }

    @JvmStatic
    fun getUrl(illust: IllustsBean, index: Int): String {
        return getUrl(illust, index, Params.IMAGE_RESOLUTION_ORIGINAL)
    }

    @JvmStatic
    fun getUrl(illust: IllustsBean, index: Int, imageResolution: String): String {
        return getImageUrlByResolution(illust, index, imageResolution)
    }

    private fun getImageUrlByResolution(
        illust: IllustsBean,
        index: Int,
        imageResolution: String,
    ): String {
        val imageUrlsBean = getImageUrlsBean(illust, index, imageResolution)
        return when (imageResolution) {
            Params.IMAGE_RESOLUTION_ORIGINAL -> imageUrlsBean.original
            Params.IMAGE_RESOLUTION_LARGE -> imageUrlsBean.large
            Params.IMAGE_RESOLUTION_MEDIUM -> imageUrlsBean.medium
            Params.IMAGE_RESOLUTION_SQUARE_MEDIUM -> imageUrlsBean.square_medium
            else -> imageUrlsBean.maxImage
        }
    }

    private fun getImageUrlsBean(
        illust: IllustsBean,
        index: Int,
        imageResolution: String,
    ): ImageUrlsBean {
        return if (illust.page_count == 1) {
            if (imageResolution == Params.IMAGE_RESOLUTION_ORIGINAL) {
                illust.meta_single_page
            } else {
                illust.image_urls
            }
        } else {
            illust.meta_pages[index].image_urls
        }
    }

    @JvmStatic
    fun getShowUrl(illust: IllustsBean, index: Int): String {
        return if (illust.page_count == 1) {
            illust.image_urls.medium
        } else {
            illust.meta_pages[index].image_urls.medium
        }
    }

    @JvmStatic
    fun check(activity: BaseActivity<*>, feedBack: FeedBack?) {
        if (Shaft.sSettings.getDownloadWay() == 1) {
            if (TextUtils.isEmpty(Shaft.sSettings.rootPathUri)) {
                if (feedBack != null) {
                    activity.setFeedBack(feedBack)
                }
                QMUIDialog.MessageDialogBuilder(activity)
                    .setTitle(activity.resources.getString(R.string.string_143))
                    .setMessage(activity.resources.getString(R.string.string_313))
                    .setSkinManager(QMUISkinManager.defaultInstance(activity))
                    .addAction(
                        0,
                        activity.resources.getString(R.string.string_142),
                        QMUIDialogAction.ACTION_PROP_NEGATIVE,
                    ) { dialog, _ -> dialog.dismiss() }
                    .addAction(0, activity.resources.getString(R.string.string_312)) { dialog, _ ->
                        try {
                            Thread {
                                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                                if (!TextUtils.isEmpty(Shaft.sSettings.rootPathUri) &&
                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                                ) {
                                    val start = Uri.parse(Shaft.sSettings.rootPathUri)
                                    intent.putExtra(android.provider.DocumentsContract.EXTRA_INITIAL_URI, start)
                                }
                                activity.startActivityForResult(intent, BaseActivity.ASK_URI)
                            }.start()
                        } catch (e: Exception) {
                            Common.showToast(e.toString())
                            e.printStackTrace()
                        }
                        dialog.dismiss()
                    }.show()
            } else {
                val root: DocumentFile? = SAFile.rootFolder(activity)
                if (root == null || !root.exists() || !root.isDirectory) {
                    if (feedBack != null) {
                        activity.setFeedBack(feedBack)
                    }
                    QMUIDialog.MessageDialogBuilder(activity)
                        .setTitle(activity.resources.getString(R.string.string_143))
                        .setMessage(activity.resources.getString(R.string.string_365))
                        .setSkinManager(QMUISkinManager.defaultInstance(activity))
                        .addAction(
                            0,
                            activity.resources.getString(R.string.string_142),
                            QMUIDialogAction.ACTION_PROP_NEGATIVE,
                        ) { dialog, _ -> dialog.dismiss() }
                        .addAction(0, activity.resources.getString(R.string.string_366)) { dialog, _ ->
                            try {
                                Thread {
                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                                    if (!TextUtils.isEmpty(Shaft.sSettings.rootPathUri) &&
                                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                                    ) {
                                        val start = Uri.parse(Shaft.sSettings.rootPathUri)
                                        intent.putExtra(android.provider.DocumentsContract.EXTRA_INITIAL_URI, start)
                                    }
                                    activity.startActivityForResult(intent, BaseActivity.ASK_URI)
                                }.start()
                            } catch (e: Exception) {
                                Common.showToast(e.toString())
                                e.printStackTrace()
                            }
                            dialog.dismiss()
                        }.show()
                } else {
                    if (feedBack != null) {
                        try {
                            feedBack.doSomething()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        } else {
            if (feedBack != null) {
                try {
                    feedBack.doSomething()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private inline fun check(activity: BaseActivity<*>, crossinline block: () -> Unit) {
        check(
            activity,
            object : FeedBack {
                override fun doSomething() {
                    block()
                }
            },
        )
    }
}
