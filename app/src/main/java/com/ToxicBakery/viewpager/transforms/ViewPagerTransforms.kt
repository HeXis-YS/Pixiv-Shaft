package com.ToxicBakery.viewpager.transforms

import android.graphics.Camera
import android.graphics.Matrix
import android.view.View
import androidx.viewpager.widget.ViewPager
import kotlin.math.abs
import kotlin.math.max

abstract class ABaseTransformer : ViewPager.PageTransformer {
    protected open fun isPagingEnabled(): Boolean = false

    protected abstract fun onTransform(page: View, position: Float)

    override fun transformPage(page: View, position: Float) {
        val clamped = position.coerceIn(-1f, 1f)
        onPreTransform(page, clamped)
        onTransform(page, clamped)
        onPostTransform(page, clamped)
    }

    protected open fun hideOffscreenPages(): Boolean = true

    protected open fun onPreTransform(page: View, position: Float) {
        val width = page.width.toFloat()
        page.rotationX = 0f
        page.rotationY = 0f
        page.rotation = 0f
        page.scaleX = 1f
        page.scaleY = 1f
        page.pivotX = 0f
        page.pivotY = 0f
        page.translationY = 0f
        page.translationX = if (isPagingEnabled()) 0f else -width * position
        if (hideOffscreenPages()) {
            page.alpha = if (position <= -1f || position >= 1f) 0f else 1f
            page.isEnabled = false
        } else {
            page.isEnabled = true
            page.alpha = 1f
        }
    }

    protected open fun onPostTransform(page: View, position: Float) = Unit

    companion object {
        @JvmStatic
        protected fun min(a: Float, b: Float): Float = if (a < b) a else b
    }
}

class AccordionTransformer : ABaseTransformer() {
    override fun onTransform(page: View, position: Float) {
        page.pivotX = if (position < 0f) 0f else page.width.toFloat()
        page.scaleX = if (position < 0f) 1f + position else 1f - position
    }
}

class BackgroundToForegroundTransformer : ABaseTransformer() {
    override fun onTransform(page: View, position: Float) {
        val height = page.height.toFloat()
        val width = page.width.toFloat()
        val scale = min(if (position < 0f) 1f else abs(1f - position), 0.5f)
        page.scaleX = scale
        page.scaleY = scale
        page.pivotX = width * 0.5f
        page.pivotY = height * 0.5f
        page.translationX = if (position < 0f) width * position else (-width * position * 0.25f)
    }
}

class CubeInTransformer : ABaseTransformer() {
    override fun isPagingEnabled() = true

    override fun onTransform(page: View, position: Float) {
        page.pivotX = if (position > 0f) 0f else page.width.toFloat()
        page.pivotY = 0f
        page.rotationY = -90f * position
    }
}

class CubeOutTransformer @JvmOverloads constructor(
    private val distanceMultiplier: Int = 20
) : ABaseTransformer() {
    override fun isPagingEnabled() = true

    override fun onTransform(page: View, position: Float) {
        page.cameraDistance = page.width * distanceMultiplier.toFloat()
        page.pivotX = if (position < 0f) page.width.toFloat() else 0f
        page.pivotY = page.height * 0.5f
        page.rotationY = 90f * position
    }
}

class DefaultTransformer : ABaseTransformer() {
    override fun isPagingEnabled() = true
    override fun onTransform(page: View, position: Float) = Unit
}

class DepthPageTransformer : ABaseTransformer() {
    override fun isPagingEnabled() = true

    override fun onTransform(page: View, position: Float) {
        if (position <= 0f) {
            page.translationX = 0f
            page.scaleX = 1f
            page.scaleY = 1f
            return
        }
        val scale = 0.75f + 0.25f * (1f - abs(position))
        page.alpha = 1f - position
        page.pivotY = page.height * 0.5f
        page.translationX = -page.width * position
        page.scaleX = scale
        page.scaleY = scale
    }
}

class DrawerTransformer : ABaseTransformer() {
    override fun onTransform(page: View, position: Float) {
        if (position <= 0f) {
            page.translationX = 0f
        } else if (position <= 1f) {
            page.translationX = (-page.width / 2f) * position
        }
    }
}

class FlipHorizontalTransformer : ABaseTransformer() {
    override fun onTransform(page: View, position: Float) {
        val rotation = 180f * position
        page.alpha = if (rotation in -90f..90f) 1f else 0f
        page.pivotX = page.width * 0.5f
        page.pivotY = page.height * 0.5f
        page.rotationY = rotation
    }

    override fun onPostTransform(page: View, position: Float) {
        super.onPostTransform(page, position)
        page.visibility = if (position in -0.5f..0.5f) View.VISIBLE else View.INVISIBLE
    }
}

class FlipVerticalTransformer : ABaseTransformer() {
    override fun onTransform(page: View, position: Float) {
        val rotation = -180f * position
        page.alpha = if (rotation in -90f..90f) 1f else 0f
        page.pivotX = page.width * 0.5f
        page.pivotY = page.height * 0.5f
        page.rotationX = rotation
    }

    override fun onPostTransform(page: View, position: Float) {
        super.onPostTransform(page, position)
        page.visibility = if (position in -0.5f..0.5f) View.VISIBLE else View.INVISIBLE
    }
}

class ForegroundToBackgroundTransformer : ABaseTransformer() {
    override fun onTransform(page: View, position: Float) {
        val height = page.height.toFloat()
        val width = page.width.toFloat()
        val scale = min(if (position > 0f) 1f else abs(1f + position), 0.5f)
        page.scaleX = scale
        page.scaleY = scale
        page.pivotX = width * 0.5f
        page.pivotY = height * 0.5f
        page.translationX = if (position > 0f) width * position else (-width * position * 0.25f)
    }
}

class RotateDownTransformer : ABaseTransformer() {
    override fun isPagingEnabled() = true

    override fun onTransform(page: View, position: Float) {
        val width = page.width.toFloat()
        val height = page.height.toFloat()
        page.pivotX = width * 0.5f
        page.pivotY = height
        page.rotation = 18.75f * position
    }
}

class RotateUpTransformer : ABaseTransformer() {
    override fun isPagingEnabled() = true

    override fun onTransform(page: View, position: Float) {
        val width = page.width.toFloat()
        page.pivotX = width * 0.5f
        page.pivotY = 0f
        page.translationX = 0f
        page.rotation = -15f * position
    }
}

class ScaleInOutTransformer : ABaseTransformer() {
    override fun onTransform(page: View, position: Float) {
        page.pivotX = if (position < 0f) 0f else page.width.toFloat()
        page.pivotY = page.height / 2f
        val scale = if (position < 0f) 1f + position else 1f - position
        page.scaleX = scale
        page.scaleY = scale
    }
}

class StackTransformer : ABaseTransformer() {
    override fun onTransform(page: View, position: Float) {
        page.translationX = if (position < 0f) 0f else -page.width * position
    }
}

class TabletTransformer : ABaseTransformer() {
    override fun onTransform(page: View, position: Float) {
        val rotation = if (position < 0f) 30f else -30f
        val absRotation = rotation * abs(position)
        page.translationX = Companion.getOffsetXForRotation(absRotation, page.width, page.height)
        page.pivotX = page.width * 0.5f
        page.pivotY = 0f
        page.rotationY = absRotation
    }

    companion object {
        private val OFFSET_MATRIX = Matrix()
        private val OFFSET_CAMERA = Camera()
        private val OFFSET_TEMP_FLOAT = FloatArray(2)

        @JvmStatic
        fun getOffsetXForRotation(degrees: Float, width: Int, height: Int): Float {
            OFFSET_MATRIX.reset()
            OFFSET_CAMERA.save()
            OFFSET_CAMERA.rotateY(abs(degrees))
            OFFSET_CAMERA.getMatrix(OFFSET_MATRIX)
            OFFSET_CAMERA.restore()
            OFFSET_TEMP_FLOAT[0] = width / 2f
            OFFSET_TEMP_FLOAT[1] = height / 2f
            OFFSET_MATRIX.preTranslate(-OFFSET_TEMP_FLOAT[0], -OFFSET_TEMP_FLOAT[1])
            OFFSET_MATRIX.postTranslate(OFFSET_TEMP_FLOAT[0], OFFSET_TEMP_FLOAT[1])
            OFFSET_MATRIX.mapPoints(OFFSET_TEMP_FLOAT)
            return width / 2f - OFFSET_TEMP_FLOAT[0]
        }
    }
}

class ZoomInTransformer : ABaseTransformer() {
    override fun onTransform(page: View, position: Float) {
        val scale = if (position < 0f) position + 1f else abs(1f - position)
        page.scaleX = scale
        page.scaleY = scale
        page.pivotX = page.width * 0.5f
        page.pivotY = page.height * 0.5f
        page.alpha = if (position < -1f || position > 1f) 0f else 1f - (scale - 1f)
    }
}

class ZoomOutSlideTransformer : ABaseTransformer() {
    override fun onTransform(page: View, position: Float) {
        if (position in -1f..1f) {
            val height = page.height.toFloat()
            val width = page.width.toFloat()
            val scaleFactor = max(0.85f, 1f - abs(position))
            val vertMargin = height * (1f - scaleFactor) / 2f
            val horzMargin = width * (1f - scaleFactor) / 2f
            page.pivotX = width * 0.5f
            page.pivotY = height * 0.5f
            page.translationX = if (position < 0f) {
                horzMargin - vertMargin / 2f
            } else {
                -horzMargin + vertMargin / 2f
            }
            page.scaleX = scaleFactor
            page.scaleY = scaleFactor
            page.alpha = 0.5f + ((scaleFactor - 0.85f) / 0.14999998f) * 0.5f
        }
    }
}

class ZoomOutTransformer : ABaseTransformer() {
    override fun onTransform(page: View, position: Float) {
        val scaleFactor = 1f + abs(position)
        page.scaleX = scaleFactor
        page.scaleY = scaleFactor
        page.pivotX = page.width * 0.5f
        page.pivotY = page.height * 0.5f
        page.alpha = if (position < -1f || position > 1f) 0f else 1f - (scaleFactor - 1f)
        if (position == -1f) {
            page.translationX = -page.width.toFloat()
        }
    }
}
