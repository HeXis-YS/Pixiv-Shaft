package ceui.lisa.file

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.text.TextUtils
import androidx.documentfile.provider.DocumentFile
import ceui.lisa.activities.Shaft
import ceui.lisa.download.FileCreator
import ceui.lisa.helper.FileStorageHelper
import ceui.lisa.models.IllustsBean
import ceui.lisa.utils.Common

object SAFile {
    @JvmStatic
    fun getDocument(context: Context, illust: IllustsBean, index: Int, deleteOld: Boolean): DocumentFile? {
        val root = rootFolder(context) ?: return null
        val displayName = FileCreator.customFileName(illust, index)
        var id = DocumentsContract.getTreeDocumentId(root.uri)
        val subDirectoryName = FileStorageHelper.getShaftIllustR18DirNameWithInnerR18Folder(illust)
        val authorDirectoryName = FileStorageHelper.getAuthorDirectoryName(illust.user)
        if (!TextUtils.isEmpty(subDirectoryName)) {
            id += "/$subDirectoryName"
        }
        val saveForSeparateAuthor = authorDirectoryName.isNotEmpty()
        if (saveForSeparateAuthor) {
            id += "/$authorDirectoryName"
        }
        id += "/$displayName"
        val childrenUri = DocumentsContract.buildDocumentUriUsingTree(root.uri, id)
        val realFile = DocumentFile.fromSingleUri(context, childrenUri)
        if (realFile != null && realFile.exists()) {
            if (deleteOld) {
                try {
                    DocumentsContract.deleteDocument(context.contentResolver, realFile.uri)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                return realFile
            }
        }

        var subDirectory = root
        if (!TextUtils.isEmpty(subDirectoryName)) {
            val createdSubDirectory = root.findFile(subDirectoryName) ?: root.createDirectory(subDirectoryName)
            if (createdSubDirectory == null) {
                return null
            }
            subDirectory = createdSubDirectory
        }
        var finalDirectory = subDirectory

        if (saveForSeparateAuthor) {
            val authorDirectory = subDirectory.findFile(authorDirectoryName) ?: subDirectory.createDirectory(authorDirectoryName)
            finalDirectory = authorDirectory ?: return null
        }
        return finalDirectory.createFile(getMimeTypeFromIllust(illust, index), displayName)
    }

    @JvmStatic
    fun rootFolder(context: Context): DocumentFile? {
        val rootUriString = Shaft.sSettings.rootPathUri
        val uri = Uri.parse(rootUriString)
        return try {
            DocumentFile.fromTreeUri(context, uri)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @JvmStatic
    fun isFileExists(context: Context, illust: IllustsBean): Boolean {
        return isFileExists(context, illust, 0)
    }

    @JvmStatic
    fun isFileExists(context: Context, illust: IllustsBean, index: Int): Boolean {
        val root = rootFolder(context)
        return if (root != null) {
            var id = DocumentsContract.getTreeDocumentId(root.uri)
            val displayName = if (illust.isGif) {
                FileName().gifName(illust)
            } else {
                FileCreator.customFileName(illust, index)
            }
            id = FileStorageHelper.getIllustFileSAFFullName(id, illust, displayName)
            val childrenUri = DocumentsContract.buildDocumentUriUsingTree(root.uri, id)
            val realFile = DocumentFile.fromSingleUri(context, childrenUri)
            realFile != null && realFile.exists()
        } else {
            false
        }
    }

    @JvmStatic
    fun getMimeTypeFromIllust(illust: IllustsBean, index: Int): String {
        val url = if (illust.page_count == 1) {
            illust.meta_single_page.original_image_url
        } else {
            illust.meta_pages[index].image_urls.original
        }

        var result = "png"
        if (url.contains(".")) {
            result = url.substring(url.lastIndexOf(".") + 1)
        }
        Common.showLog("getMimeTypeFromIllust fileUrl: $url, fileType: $result")
        return "image/$result"
    }
}
