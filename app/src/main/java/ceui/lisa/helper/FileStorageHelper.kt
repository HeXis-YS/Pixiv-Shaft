package ceui.lisa.helper

import android.os.Environment
import com.blankj.utilcode.util.PathUtils
import ceui.lisa.activities.Shaft
import ceui.lisa.core.DownloadItem
import ceui.lisa.models.IllustsBean
import ceui.lisa.models.UserBean
import ceui.lisa.utils.Common
import ceui.lisa.utils.UserFolderNameUtil
import java.io.File

object FileStorageHelper {
    private val sep = File.separatorChar

    @JvmStatic
    fun getIllustFileFullNameUnderQ(downloadItem: DownloadItem): String {
        return getIllustAbsolutePath(downloadItem.illust) + sep + downloadItem.name
    }

    @JvmStatic
    fun getIllustFileRelativeNameQ(downloadItem: DownloadItem): String {
        return getIllustRelativePathQ(downloadItem.illust) + sep + downloadItem.name
    }

    @JvmStatic
    fun getIllustFileSAFFullName(id: String, illustsBean: IllustsBean, fileName: String): String {
        var fullName = id
        if (isSaveToAIDir(illustsBean)) {
            fullName += getShaftIllustPathPartWithInnerAIFolder(illustsBean)
        } else if (isSaveToR18Dir(illustsBean)) {
            fullName += getShaftIllustPathPartWithInnerR18Folder(illustsBean)
        }
        fullName += getAuthorPathPart(illustsBean) + File.separator + fileName
        return fullName
    }

    @JvmStatic
    fun getIllustAbsolutePath(illustsBean: IllustsBean): String {
        var absolutePath = PathUtils.getExternalPicturesPath() + sep
        if (isSaveToAIDir(illustsBean)) {
            absolutePath += getShaftIllustDirWithInnerAIFolder(true)
        } else if (isSaveToR18Dir(illustsBean)) {
            absolutePath += getShaftIllustDirWithInnerR18Folder(true)
        }
        absolutePath += getAuthorPathPart(illustsBean)
        return absolutePath
    }

    @JvmStatic
    fun getIllustRelativePathQ(illustsBean: IllustsBean): String {
        var relativePath = Environment.DIRECTORY_PICTURES + sep
        if (isSaveToAIDir(illustsBean)) {
            relativePath += getShaftIllustDirWithInnerAIFolder(true)
        } else if (isSaveToR18Dir(illustsBean)) {
            relativePath += getShaftIllustDirWithInnerR18Folder(true)
        }
        relativePath += getAuthorPathPart(illustsBean)
        return relativePath
    }

    @JvmStatic
    fun getNovelRelativePathQ(): String {
        return Environment.DIRECTORY_DOWNLOADS + sep + "ShaftNovels"
    }

    @JvmStatic
    fun getShaftIllustR18DirNameWithInnerR18Folder(illustsBean: IllustsBean): String {
        return if (isSaveToR18Dir(illustsBean)) "ShaftImages-R18" else ""
    }

    @JvmStatic
    fun getShaftIllustPathPartWithInnerR18Folder(illustsBean: IllustsBean): String {
        return getShaftIllustPathPartWithInnerR18Folder(isSaveToR18Dir(illustsBean))
    }

    @JvmStatic
    fun getShaftIllustPathPartWithInnerR18Folder(isR18: Boolean): String {
        return if (isR18) sep + "ShaftImages-R18" else ""
    }

    @JvmStatic
    fun getShaftIllustDirWithInnerR18Folder(isR18: Boolean): String {
        return "ShaftImages" + getShaftIllustPathPartWithInnerR18Folder(isR18)
    }

    private fun isSaveToR18Dir(illustsBean: IllustsBean): Boolean {
        return illustsBean.isR18File && Shaft.sSettings.isR18DivideSave
    }

    @JvmStatic
    fun getShaftIllustAIDirNameWithInnerAIFolder(illustsBean: IllustsBean): String {
        return if (isSaveToAIDir(illustsBean)) "ShaftImages-AI" else ""
    }

    @JvmStatic
    fun getShaftIllustPathPartWithInnerAIFolder(illustsBean: IllustsBean): String {
        return getShaftIllustPathPartWithInnerAIFolder(isSaveToAIDir(illustsBean))
    }

    @JvmStatic
    fun getShaftIllustPathPartWithInnerAIFolder(isAI: Boolean): String {
        return if (isAI) sep + "ShaftImages-AI" else ""
    }

    @JvmStatic
    fun getShaftIllustDirWithInnerAIFolder(isAI: Boolean): String {
        return "ShaftImages" + getShaftIllustPathPartWithInnerAIFolder(isAI)
    }

    private fun isSaveToAIDir(illustsBean: IllustsBean): Boolean {
        return illustsBean.isCreatedByAI && Shaft.sSettings.isAIDivideSave
    }

    private fun getAuthorPathPart(illustsBean: IllustsBean): String {
        val name = getAuthorDirectoryName(illustsBean.user)
        return if (name.isNotEmpty()) "$sep$name" else name
    }

    @JvmStatic
    fun getAuthorDirectoryName(userBean: UserBean): String {
        return Common.removeFSReservedChars(UserFolderNameUtil.getFolderNameForUser(userBean))
    }
}
