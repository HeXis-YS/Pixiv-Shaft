package ceui.lisa.download

import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import ceui.lisa.activities.Shaft
import ceui.lisa.utils.Common
import java.io.File

abstract class ImageSaver {

    abstract fun whichFile(): File?

    fun execute() {
        val file = whichFile() ?: return

        val path = arrayOf(file.path)
        val mime = arrayOf(
            when {
                file.path.endsWith(".gif") -> "image/gif"
                file.path.endsWith(".jpeg") || file.path.endsWith(".jpg") -> "image/jpeg"
                else -> "image/png"
            }
        )
        Common.showLog("ImageSaver before ${file.path}")
        MediaScannerConnection.scanFile(
            Shaft.getContext(),
            path,
            mime
        ) { path1, uri ->
            Common.showLog("ImageSaver path1 $path1 uri $uri")
        }

        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file))
        Shaft.getContext().sendBroadcast(intent)
    }
}
