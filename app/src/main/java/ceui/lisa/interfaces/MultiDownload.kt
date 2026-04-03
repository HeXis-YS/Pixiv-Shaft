package ceui.lisa.interfaces

import android.content.Context
import ceui.lisa.activities.TemplateActivity
import ceui.lisa.models.IllustsBean
import ceui.lisa.utils.DataChannel

interface MultiDownload {

    fun getContext(): Context

    fun getIllustList(): List<IllustsBean>

    fun startDownload()

    companion object {
        @JvmStatic
        fun startMultiDownload(target: MultiDownload) {
            val dataChannel = DataChannel.get()
            val list = target.getIllustList()
            dataChannel.downloadList = list
            TemplateActivity.startMultiDownload(target.getContext())
        }
    }
}
