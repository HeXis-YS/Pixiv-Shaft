package ceui.lisa.file

import ceui.lisa.download.FileCreator
import ceui.lisa.models.IllustsBean
import ceui.lisa.utils.Common

class FileName {

    companion object {
        private const val DASH = "_"
    }

    fun zipName(illust: IllustsBean): String {
        return Common.removeFSReservedChars(illust.title) + DASH + illust.id + ".zip"
    }

    fun unzipName(illust: IllustsBean): String {
        return Common.removeFSReservedChars(illust.title) + DASH + illust.id + DASH + "unzip"
    }

    fun gifName(illust: IllustsBean): String {
        return FileCreator.customGifFileName(illust)
    }
}
