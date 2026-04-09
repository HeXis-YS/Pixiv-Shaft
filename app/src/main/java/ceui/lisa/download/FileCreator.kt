package ceui.lisa.download

import android.text.TextUtils
import com.google.gson.reflect.TypeToken
import ceui.lisa.activities.Shaft
import ceui.lisa.file.FileName
import ceui.lisa.helper.FileStorageHelper
import ceui.lisa.model.CustomFileNameCell
import ceui.lisa.models.IllustsBean
import ceui.lisa.utils.Common
import java.io.File
import java.util.ArrayList

object FileCreator {

    private const val DASH = "_"

    @JvmStatic
    fun isExist(illust: IllustsBean, index: Int): Boolean {
        val fileName = if (illust.isGif()) FileName().gifName(illust) else customFileName(illust, index)
        val file = File(FileStorageHelper.getIllustAbsolutePath(illust), fileName)
        Common.showLog("saasdadw 给是否存在 ${file.path}")
        return file.exists()
    }

    @JvmStatic
    fun deleteSpecialWords(before: String?): String {
        return if (!TextUtils.isEmpty(before)) {
            var result = before!!
            if (result.startsWith(".")) {
                result = result.replaceFirst("\\.".toRegex(), "\u2024")
            }
            result
                .replace("-", DASH)
                .replace("/", DASH)
                .replace(",", DASH)
                .replace(":", DASH)
                .replace("*", DASH)
        } else {
            "untitle_${System.currentTimeMillis()}.png"
        }
    }

    @JvmField
    val ILLUST_TITLE = 1

    @JvmField
    val ILLUST_ID = 2

    @JvmField
    val P_SIZE = 3

    @JvmField
    val USER_ID = 4

    @JvmField
    val USER_NAME = 5

    @JvmField
    val ILLUST_SIZE = 6

    @JvmField
    val CREATE_TIME = 7

    @JvmStatic
    fun customFileName(illustsBean: IllustsBean, index: Int): String {
        val result: List<CustomFileNameCell> = currentFileCells()
        val fileUrl = if (illustsBean.getPage_count() == 1) {
            illustsBean.getMeta_single_page().getOriginal_image_url()
        } else {
            illustsBean.getMeta_pages()[index].getImage_urls().getOriginal()
        }
        return deleteSpecialWords("${illustToFileName(illustsBean, result, index)}.${getMimeTypeFromUrl(fileUrl)}")
    }

    @JvmStatic
    fun customGifFileName(illustsBean: IllustsBean): String {
        val result: List<CustomFileNameCell> = currentFileCells()
        return Common.removeFSReservedChars("${illustToFileName(illustsBean, result, 0)}.gif")
    }

    @JvmStatic
    fun getMimeTypeFromUrl(url: String): String {
        var result = "png"
        if (url.contains(".")) {
            result = url.substring(url.lastIndexOf(".") + 1)
        }
        Common.showLog("getMimeType fileUrl: $url, fileType: $result")
        return result
    }

    @JvmStatic
    fun customFileNameForPreview(
        illustsBean: IllustsBean,
        cells: List<CustomFileNameCell>,
        index: Int
    ): String {
        val fileUrl = if (illustsBean.getPage_count() == 1) {
            illustsBean.getMeta_single_page().getOriginal_image_url()
        } else {
            illustsBean.getMeta_pages()[index].getImage_urls().getOriginal()
        }
        return deleteSpecialWords("${illustToFileName(illustsBean, cells, index)}.${getMimeTypeFromUrl(fileUrl)}")
    }

    private fun currentFileCells(): List<CustomFileNameCell> {
        val settingsFileNameJson = Shaft.sSettings.fileNameJson
        return if (TextUtils.isEmpty(settingsFileNameJson)) {
            defaultFileCells()
        } else {
            val decoded: List<CustomFileNameCell>? = Shaft.sGson.fromJson(
                    settingsFileNameJson,
                    object : TypeToken<List<CustomFileNameCell>>() {}.type
                )
            ArrayList(decoded ?: defaultFileCells())
        }
    }

    private fun illustToFileName(
        illustsBean: IllustsBean,
        result: List<CustomFileNameCell>,
        index: Int
    ): String {
        var fileName = ""
        for (cell in result) {
            if (cell.isChecked) {
                when (cell.code) {
                    ILLUST_ID -> {
                        fileName = if (!TextUtils.isEmpty(fileName)) {
                            "${fileName}_${illustsBean.id}"
                        } else {
                            illustsBean.id.toString()
                        }
                    }

                    ILLUST_TITLE -> {
                        fileName = if (!TextUtils.isEmpty(fileName)) {
                            "${fileName}_${illustsBean.title}"
                        } else {
                            illustsBean.title
                        }
                    }

                    P_SIZE -> {
                        if (Shaft.sSettings.isHasP0) {
                            fileName = if (!TextUtils.isEmpty(fileName)) {
                                "${fileName}_p$index"
                            } else {
                                "p$index"
                            }
                        } else if (illustsBean.getPage_count() != 1) {
                            fileName = if (!TextUtils.isEmpty(fileName)) {
                                "${fileName}_p${index + 1}"
                            } else {
                                "p${index + 1}"
                            }
                        }
                    }

                    USER_ID -> {
                        fileName = if (!TextUtils.isEmpty(fileName)) {
                            "${fileName}_${illustsBean.user.id}"
                        } else {
                            illustsBean.user.id.toString()
                        }
                    }

                    USER_NAME -> {
                        fileName = if (!TextUtils.isEmpty(fileName)) {
                            "${fileName}_${illustsBean.user.name}"
                        } else {
                            illustsBean.user.name
                        }
                    }

                    ILLUST_SIZE -> {
                        val sizeValue = "${illustsBean.width}px*${illustsBean.height}px"
                        fileName = if (!TextUtils.isEmpty(fileName)) {
                            "${fileName}_$sizeValue"
                        } else {
                            sizeValue
                        }
                    }

                    CREATE_TIME -> {
                        val createDate = Common.getLocalYYYYMMDDHHMMSSFileString(illustsBean.create_date)
                        fileName = if (!TextUtils.isEmpty(fileName)) {
                            "${fileName}_$createDate"
                        } else {
                            createDate
                        }
                    }
                }
            }
        }
        return fileName
    }

    @JvmStatic
    fun defaultFileCells(): List<CustomFileNameCell> {
        val cells = ArrayList<CustomFileNameCell>()
        cells.add(CustomFileNameCell("作品标题", "作品标题，可选项", 1, true))
        cells.add(CustomFileNameCell("作品ID", "不选的话可能两个文件名重复，导致下载失败，必选项", 2, true))
        cells.add(CustomFileNameCell("作品P数", "显示当前图片是作品的第几P，必选项", 3, true))
        cells.add(CustomFileNameCell("画师ID", "画师ID，可选项", 4, false))
        cells.add(CustomFileNameCell("画师昵称", "画师昵称，可选项", 5, false))
        cells.add(CustomFileNameCell("作品尺寸", "显示当前图片的尺寸信息，可选项", 6, false))
        cells.add(CustomFileNameCell("创作时间", "创作时间，可选项", 7, false))
        return cells
    }
}
