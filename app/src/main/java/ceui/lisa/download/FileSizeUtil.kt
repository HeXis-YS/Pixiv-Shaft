package ceui.lisa.download

import java.io.File
import java.io.FileInputStream
import java.text.DecimalFormat

object FileSizeUtil {

    @JvmField
    val SIZETYPE_B = 1

    @JvmField
    val SIZETYPE_KB = 2

    @JvmField
    val SIZETYPE_MB = 3

    @JvmField
    val SIZETYPE_GB = 4

    @JvmStatic
    fun getFileOrFilesSize(filePath: String, sizeType: Int): Double {
        val file = File(filePath)
        var blockSize = 0L
        try {
            blockSize = if (file.isDirectory) getFileSizes(file) else getFileSize(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return formatFileSize(blockSize, sizeType)
    }

    @JvmStatic
    fun getFileOrFilesSize(file: File, sizeType: Int): Double {
        var blockSize = 0L
        try {
            blockSize = if (file.isDirectory) getFileSizes(file) else getFileSize(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return formatFileSize(blockSize, sizeType)
    }

    @JvmStatic
    fun getAutoFileOrFilesSize(filePath: String): String {
        val file = File(filePath)
        var blockSize = 0L
        try {
            blockSize = if (file.isDirectory) getFileSizes(file) else getFileSize(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return formatFileSize(blockSize)
    }

    private fun getFileSize(file: File): Long {
        var size = 0L
        if (file.exists()) {
            val fis = FileInputStream(file)
            size = fis.available().toLong()
        } else {
            file.createNewFile()
        }
        return size
    }

    private fun getFileSizes(file: File): Long {
        var size = 0L
        val fileList = file.listFiles() ?: return size
        for (child in fileList) {
            size += if (child.isDirectory) getFileSizes(child) else getFileSize(child)
        }
        return size
    }

    @JvmStatic
    fun formatFileSize(fileS: Long): String {
        val df = DecimalFormat("#.00")
        if (fileS <= 0) {
            return "0B"
        }
        return when {
            fileS < 1024 -> df.format(fileS.toDouble()) + "B"
            fileS < 1048576 -> df.format(fileS.toDouble() / 1024) + "KB"
            fileS < 1073741824 -> df.format(fileS.toDouble() / 1048576) + "MB"
            else -> df.format(fileS.toDouble() / 1073741824) + "GB"
        }
    }

    private fun formatFileSize(fileS: Long, sizeType: Int): Double {
        val df = DecimalFormat("#.00")
        return when (sizeType) {
            SIZETYPE_B -> df.format(fileS.toDouble()).toDouble()
            SIZETYPE_KB -> df.format(fileS.toDouble() / 1024).toDouble()
            SIZETYPE_MB -> df.format(fileS.toDouble() / 1048576).toDouble()
            SIZETYPE_GB -> df.format(fileS.toDouble() / 1073741824).toDouble()
            else -> 0.0
        }
    }
}
