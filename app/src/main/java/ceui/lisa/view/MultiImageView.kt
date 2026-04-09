package ceui.lisa.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import ceui.lisa.interfaces.OnItemClickListener
import ceui.lisa.utils.DensityUtil

class MultiImageView : LinearLayout {

    private var imagesList: List<GlideUrl>? = null

    private var pxOneMaxWandH = 0
    private var pxMoreWandH = 0
    private val pxImagePadding = DensityUtil().dip2px(3.0f)

    private var maxPerRowCount = 3

    private var onePicPara: LayoutParams? = null
    private var morePara: LayoutParams? = null
    private var moreParaColumnFirst: LayoutParams? = null
    private var rowPara: LayoutParams? = null

    private var onItemClickListener: OnItemClickListener? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    @Throws(IllegalArgumentException::class)
    fun setList(lists: List<GlideUrl>?) {
        if (lists == null) {
            throw IllegalArgumentException("imageList is null...")
        }
        imagesList = lists

        if (MAX_WIDTH > 0) {
            pxMoreWandH = if (lists.size == 2 || lists.size == 4) {
                (MAX_WIDTH - pxImagePadding) / 2
            } else {
                (MAX_WIDTH - pxImagePadding * 2) / 3
            }
            pxOneMaxWandH = MAX_WIDTH
            initImageLayoutParams()
        }

        initView()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (MAX_WIDTH == 0) {
            val width = measureWidth(widthMeasureSpec)
            if (width > 0) {
                MAX_WIDTH = width - paddingLeft - paddingRight
                val imageList = imagesList
                if (!imageList.isNullOrEmpty()) {
                    setList(imageList)
                }
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun measureWidth(measureSpec: Int): Int {
        var result = 0
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = minOf(result, specSize)
        }
        return result
    }

    private fun initImageLayoutParams() {
        val wrap = LayoutParams.WRAP_CONTENT
        val match = LayoutParams.MATCH_PARENT

        onePicPara = LayoutParams(pxOneMaxWandH, wrap)

        moreParaColumnFirst = LayoutParams(pxMoreWandH, pxMoreWandH)
        morePara = LayoutParams(pxMoreWandH, pxMoreWandH).apply {
            setMargins(pxImagePadding, 0, 0, 0)
        }

        rowPara = LayoutParams(match, wrap)
    }

    private fun initView() {
        orientation = VERTICAL
        removeAllViews()
        if (MAX_WIDTH == 0) {
            addView(View(context))
            return
        }

        val imageList = imagesList ?: return
        if (imageList.isEmpty()) {
            return
        }

        if (imageList.size == 1) {
            addView(createImageView(0, false))
            return
        }

        val allCount = imageList.size
        maxPerRowCount = if (allCount == 4) 2 else 3
        val rowCount = allCount / maxPerRowCount + if (allCount % maxPerRowCount > 0) 1 else 0
        for (rowCursor in 0 until rowCount) {
            val rowLayout = LinearLayout(context).apply {
                orientation = HORIZONTAL
                layoutParams = rowPara
                if (rowCursor != 0) {
                    setPadding(0, pxImagePadding, 0, 0)
                }
            }

            val columnCount = if (rowCursor != rowCount - 1) {
                maxPerRowCount
            } else if (allCount % maxPerRowCount == 0) {
                maxPerRowCount
            } else {
                allCount % maxPerRowCount
            }

            addView(rowLayout)

            val rowOffset = rowCursor * maxPerRowCount
            for (columnCursor in 0 until columnCount) {
                val position = columnCursor + rowOffset
                rowLayout.addView(createImageView(position, true))
            }
        }
    }

    private fun createImageView(position: Int, isMultiImage: Boolean): ImageView {
        val url = imagesList!![position]
        val imageView = ColorFilterImageView(context)
        if (isMultiImage) {
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.layoutParams =
                if (position % maxPerRowCount == 0) moreParaColumnFirst else morePara
        } else {
            imageView.adjustViewBounds = true
            imageView.scaleType = ImageView.ScaleType.FIT_START
            imageView.layoutParams = onePicPara
        }

        imageView.id = url.hashCode()
        imageView.setOnClickListener { v ->
            onItemClickListener?.onItemClick(v, position, position)
        }
        Glide.with(context).load(url).into(imageView)
        return imageView
    }

    companion object {
        @JvmField
        var MAX_WIDTH = 0
    }
}
