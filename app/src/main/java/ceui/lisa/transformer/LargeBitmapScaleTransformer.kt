package ceui.lisa.transformer

import android.graphics.Bitmap
import com.blankj.utilcode.util.ImageUtils
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

class LargeBitmapScaleTransformer : BitmapTransformation() {

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int,
    ): Bitmap {
        var width = toTransform.width
        var height = toTransform.height
        var bitmap = toTransform
        val byteCount = bitmap.byteCount

        if (byteCount > MAX_BITMAP_SIZE) {
            val scale = Math.sqrt(byteCount.toDouble() / MAX_BITMAP_SIZE)
            width = Math.floor(width / scale).toInt()
            height = Math.floor(height / scale).toInt()
            bitmap = ImageUtils.compressByScale(bitmap, width, height)
        }

        return bitmap
    }

    override fun equals(other: Any?): Boolean {
        return other is LargeBitmapScaleTransformer
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
    }

    companion object {
        private const val ID = "ceui.lisa.transformer.LargeBitmapScaleTransformer"
        private val ID_BYTES = ID.toByteArray()
        private const val MAX_BITMAP_SIZE = 100 * 1024 * 1024
    }
}
