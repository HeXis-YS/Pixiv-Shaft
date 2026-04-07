package ceui.lisa.file

import ceui.lisa.download.FileCreator
import ceui.lisa.models.IllustsBean
import ceui.lisa.utils.Common

class FileName : FileNameProxy {

    companion object {
        private const val DASH = "_"
    }

    override fun zipName(illust: IllustsBean): String {
        return Common.removeFSReservedChars(illust.title) + DASH + illust.id + ".zip"
    }

    override fun unzipName(illust: IllustsBean): String {
        return Common.removeFSReservedChars(illust.title) + DASH + illust.id + DASH + "unzip"
    }

    override fun gifName(illust: IllustsBean): String {
        return FileCreator.customGifFileName(illust)
    }
}
