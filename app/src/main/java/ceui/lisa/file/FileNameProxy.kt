package ceui.lisa.file

import ceui.lisa.models.IllustsBean

interface FileNameProxy {

    fun zipName(illust: IllustsBean): String

    fun unzipName(illust: IllustsBean): String

    fun gifName(illust: IllustsBean): String
}
