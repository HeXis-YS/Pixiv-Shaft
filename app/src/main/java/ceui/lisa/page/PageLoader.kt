package ceui.lisa.page

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextUtils
import androidx.core.content.ContextCompat
import ceui.lisa.R
import ceui.lisa.models.NovelDetail
import ceui.lisa.utils.Common
import java.text.NumberFormat

class PageLoader(
    private val mPageView: PageView,
    private val mNovelDetail: NovelDetail?,
) {
    private lateinit var thisSettingInfo: ReadSettingInfo
    private var mBgColor = 0
    private val mContext: Context = mPageView.context
    private val paint = Paint()
    private var dataInitSuccess = false
    private var mDisplayWidth = 0
    private var mDisplayHeight = 0
    private var pageViewInitSuccess = false
    private var mCurPage: PageInfoModel? = null
    private var mCurPageList: List<PageInfoModel>? = null
    private var thisPage = 0
    private var lastTurnPageType = TurnPageType.NONE
    private var x = 0
    private var y = 0
    private var canvas: Canvas? = null
    private var threadUpdateReadPercentage: Thread? = null
    private var percentage: String? = null
    private var mProcessPaint: Paint? = null
    private var processX = 0f
    private var processY = 0f
    private var polarLeft = 0
    private var polarTop = 0
    private var polarRight = 0
    private var polarBottom = 0
    private var polar: Rect? = null

    init {
        initPageView()
    }

    fun prePage(execute: Boolean): Boolean {
        return try {
            if (thisPage > 0) {
                if (execute) {
                    thisPage--
                    mCurPage = mCurPageList!![thisPage]
                    lastTurnPageType = TurnPageType.PRE
                    updateReadPercentage()
                    mPageView.drawNextPage()
                }
                true
            } else {
                false
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
    }

    fun nextPage(execute: Boolean): Boolean {
        return try {
            val currentPageList = mCurPageList ?: return false
            if (thisPage < currentPageList.size - 1) {
                if (execute) {
                    thisPage++
                    mCurPage = currentPageList[thisPage]
                    lastTurnPageType = TurnPageType.NEXT
                    updateReadPercentage()
                    mPageView.drawNextPage()
                }
                true
            } else {
                false
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
    }

    fun pageCancel() {
        try {
            val currentPageList = mCurPageList ?: return
            if (lastTurnPageType == TurnPageType.PRE) {
                thisPage++
                mCurPage = currentPageList[thisPage]
            } else if (lastTurnPageType == TurnPageType.NEXT) {
                thisPage--
                mCurPage = currentPageList[thisPage]
            }

            updateReadPercentage()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun turnPage() {
    }

    fun pageViewInitSuccess(width: Int, height: Int) {
        mDisplayWidth = width
        mDisplayHeight = height
        pageViewInitSuccess = true
        init()
    }

    fun isRequesting(): Boolean {
        return false
    }

    fun drawPage(nextBitmap: Bitmap?, b: Boolean) {
        try {
            Common.showLog("drawContent drawPage ")
            val bgBitmap = mPageView.getBgBitmap() ?: return
            drawBackground(bgBitmap, b)
            if (nextBitmap == null) {
                return
            }
            drawContent(nextBitmap)
            mPageView.invalidate()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun init() {
        mPageView.setPageMode(thisSettingInfo.pageAnimType)
        mCurPageList = UtilityMeasure.getPageInfos(mNovelDetail, thisSettingInfo, mPageView)
        if (!Common.isEmpty(mCurPageList)) {
            mCurPage = mCurPageList!![0]
            Common.showLog("arrWrapContent 33 " + mCurPageList!!.size)
            updateReadPercentage()
        }
    }

    fun dataInitSuccess() {
        dataInitSuccess = true
        init()
    }

    private fun drawContent(bitmap: Bitmap) {
        try {
            Common.showLog("drawContent 00 ")
            canvas = Canvas(bitmap)
            if (thisSettingInfo.pageAnimType == PageMode.SCROLL) {
                canvas!!.drawColor(mBgColor)
            }

            val currentPage = mCurPage ?: return
            if (Common.isEmpty(currentPage.lisText)) {
                return
            }

            for (i in currentPage.lisText.indices) {
                val textModel = currentPage.lisText[i]
                paint.textSize = Utility.dip2px(textModel.textSize).toFloat()
                paint.isFakeBoldText = textModel.fakeBoldText
                paint.isAntiAlias = true

                if (i == 0) {
                    x = mPageView.paddingLeft
                    y = mPageView.paddingTop + textModel.height
                } else {
                    y += textModel.height
                }

                if (!TextUtils.isEmpty(textModel.text)) {
                    canvas!!.drawText(textModel.text!!, x.toFloat(), y.toFloat(), paint)
                }
            }

            drawProcess()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun drawBackground(bitmap: Bitmap, isUpdate: Boolean) {
        try {
            canvas = Canvas(bitmap)
            if (!isUpdate) {
                canvas!!.drawColor(mBgColor)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun initPageView() {
        try {
            thisSettingInfo = ReadSettingInfo()
            thisSettingInfo.lineSpacingExtra =
                UtilityMeasure.getLineSpacingExtra(thisSettingInfo.frontSize)

            mBgColor = getBgColor(thisSettingInfo.lightType)
            mPageView.setBgColor(mBgColor)
            paint.color = mContext.resources.getColor(thisSettingInfo.frontColor)
            mPageView.setPageMode(thisSettingInfo.pageAnimType)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun getBgColor(lightType: Int): Int {
        return when (lightType) {
            ConstantSetting.LIGHTTYPE_1 -> ContextCompat.getColor(mContext, R.color.bg1)
            ConstantSetting.LIGHTTYPE_2 -> ContextCompat.getColor(mContext, R.color.bg2)
            ConstantSetting.LIGHTTYPE_3 -> ContextCompat.getColor(mContext, R.color.bg3)
            ConstantSetting.LIGHTTYPE_4 -> ContextCompat.getColor(mContext, R.color.bg4)
            ConstantSetting.LIGHTTYPE_5 -> ContextCompat.getColor(mContext, R.color.bg5)
            else -> ContextCompat.getColor(mContext, R.color.bg1)
        }
    }

    private fun updateReadPercentage() {
        try {
            threadUpdateReadPercentage =
                object : Thread() {
                    override fun run() {
                        val currentPageList = mCurPageList
                        val currentPage = mCurPage
                        if (mNovelDetail == null ||
                            Common.isEmpty(currentPageList) ||
                            currentPage == null
                        ) {
                            return
                        }

                        percentage = getPercent(thisPage + 1, currentPageList!!.size) + "%"
                        Common.showLog("percentage $percentage")
                    }
                }
            threadUpdateReadPercentage!!.start()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun drawProcess() {
        if (TextUtils.isEmpty(percentage)) {
            return
        }

        try {
            if (mProcessPaint == null) {
                mProcessPaint = Paint()
                mProcessPaint!!.textSize = Utility.dip2px(ConstantPageInfo.processTextSize).toFloat()
                mProcessPaint!!.isAntiAlias = true
                mProcessPaint!!.isDither = true

                processX =
                    (
                        mDisplayWidth - mPageView.paddingRight -
                            ScreenUtils.dpToPx(15) -
                            ScreenUtils.dpToPx(12)
                        ).toFloat()
                processY = (mDisplayHeight - ScreenUtils.dpToPx(4)).toFloat()
            }

            canvas!!.drawText(percentage!!, processX, processY, mProcessPaint!!)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    companion object {
        @JvmStatic
        fun getPercent(diliverNum: Int, queryMailNum: Int): String {
            var result = ""
            try {
                val numberFormat = NumberFormat.getInstance()
                numberFormat.maximumFractionDigits = 1
                result =
                    numberFormat.format(diliverNum.toFloat() / queryMailNum.toFloat() * 100)

                if (TextUtils.equals(result, "0") || TextUtils.equals(result, "0.0")) {
                    result = "0.1"
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return result
        }
    }
}
