package ceui.lisa.file

import android.content.Context
import ceui.lisa.models.IllustsBean
import java.io.File

interface FileProxy {

    fun imageCacheFolder(context: Context): File

    fun gifCacheFolder(context: Context): File

    fun gifZipFile(context: Context, illust: IllustsBean): File

    fun gifUnzipFolder(context: Context, illust: IllustsBean): File

    fun gifResultFile(context: Context, illust: IllustsBean): File

    fun textFile(context: Context, name: String): File
}
