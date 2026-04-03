package ceui.lisa.core

import android.text.TextUtils
import ceui.lisa.download.FileCreator
import ceui.lisa.file.FileName
import ceui.lisa.models.IllustsBean
import ceui.lisa.utils.Common
import java.io.Serializable
import java.util.UUID

class DownloadItem(
    val illust: IllustsBean,
    var index: Int
) : Serializable {

    var name: String
    var url: String? = null
        set(value) {
            Common.showLog("DownloadItem 准备下载：$value")
            field = value
        }
    var showUrl: String? = null
    var uuid: String = UUID.randomUUID().toString()
    var autoSave: Boolean = true
    private var state: Int = DownloadState.INIT
    private var paused: Boolean = false
    var nonius: Int = 0

    init {
        name = if (illust.isGif) {
            FileName().zipName(illust)
        } else {
            FileCreator.customFileName(illust, index)
        }
        Common.showLog("随机生成一个UUID")
    }

    fun isSame(next: DownloadItem?): Boolean {
        return next != null &&
            TextUtils.equals(name, next.name) &&
            TextUtils.equals(url, next.url)
    }

    fun getState(): Int {
        if (paused) {
            return DownloadState.PAUSED
        }
        return state
    }

    fun setState(state: Int) {
        this.state = state
    }

    fun setPaused(paused: Boolean) {
        this.paused = paused
    }

    fun isAutoSave(): Boolean {
        return autoSave
    }

    fun isPaused(): Boolean {
        return paused
    }

    fun shouldStartNewDownload(): Boolean {
        return state == DownloadState.INIT || state == DownloadState.FAILED
    }

    class DownloadState {
        companion object {
            const val INIT = 0
            const val DOWNLOADING = 1
            const val SUCCESS = 2
            const val FAILED = 3
            const val PAUSED = 4
        }
    }
}
