package ceui.lisa.transformer

import android.graphics.Bitmap
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.transition.Transition

class UniformScaleTransformation(
    private val target: ImageView,
    private val changeSize: Boolean,
) : ImageViewTarget<Bitmap>(target) {

    override fun onResourceReady(
        resource: Bitmap,
        transition: Transition<in Bitmap>?,
    ) {
        super.onResourceReady(resource, transition)
        if (changeSize) {
            val width = resource.width
            val height = resource.height
            val imageViewWidth = target.width
            val imageViewHeight = height * imageViewWidth / width
            val params: ViewGroup.LayoutParams = target.layoutParams
            params.height = imageViewHeight
            target.layoutParams = params
        }
    }

    override fun setResource(resource: Bitmap?) {
        view.setImageBitmap(resource)
    }
}
