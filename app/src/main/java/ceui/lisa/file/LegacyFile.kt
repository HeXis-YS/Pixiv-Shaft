package ceui.lisa.file

import android.content.Context
import ceui.lisa.models.IllustsBean
import ceui.lisa.utils.Common
import java.io.File

object LegacyFile {
    private const val GIF_CACHE = "/gif cache"
    private const val IMAGE_CACHE = "/image_manager_disk_cache"

    @JvmStatic
    fun imageCacheFolder(context: Context): File {
        val cacheDir = File(context.cacheDir.path + IMAGE_CACHE)
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        Common.showLog("LegacyFile imageCacheFolder ${cacheDir.path}")
        return cacheDir
    }

    @JvmStatic
    fun gifCacheFolder(context: Context): File {
        val cacheDir = File(context.externalCacheDir!!.path + GIF_CACHE)
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        Common.showLog("LegacyFile gifCacheFolder ${cacheDir.path}")
        return cacheDir
    }

    @JvmStatic
    fun gifZipFile(context: Context, illust: IllustsBean): File {
        val gifCacheFolder = gifCacheFolder(context)
        val zipName = FileName().zipName(illust)
        val zipFile = File(gifCacheFolder, zipName)
        if (!zipFile.exists()) {
            try {
                zipFile.createNewFile()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        Common.showLog("LegacyFile gifZipFile ${zipFile.path}")
        return zipFile
    }

    @JvmStatic
    fun gifUnzipFolder(context: Context, illust: IllustsBean): File {
        val folderName = FileName().unzipName(illust)
        val unzipDirFile = File(gifCacheFolder(context).path + "/" + folderName)
        if (!unzipDirFile.exists()) {
            unzipDirFile.mkdirs()
        }
        Common.showLog("LegacyFile gifUnzipFolder ${unzipDirFile.path}")
        return unzipDirFile
    }

    @JvmStatic
    fun gifResultFile(context: Context, illust: IllustsBean): File {
        val gifCacheFolder = gifCacheFolder(context)
        val gifResultName = FileName().gifName(illust)
        val gifResult = File(gifCacheFolder, gifResultName)
        if (!gifResult.exists()) {
            try {
                gifResult.createNewFile()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        Common.showLog("LegacyFile gifResultFile ${gifResult.path}")
        return gifResult
    }

    @JvmStatic
    fun textFile(context: Context, name: String): File {
        val gifCacheFolder = gifCacheFolder(context)
        val textFile = File(gifCacheFolder, name)
        if (!textFile.exists()) {
            try {
                textFile.createNewFile()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return textFile
    }
}
