package ceui.lisa.page

import android.graphics.Paint
import android.graphics.Rect
import android.text.TextUtils
import android.view.View
import ceui.lisa.models.NovelDetail
import ceui.lisa.utils.Common

object UtilityMeasure {
    @JvmField
    val testWord: String = "中"

    private var pubLineShowWords = 0
    private var testWordRect: Rect? = null

    private fun getTestWordHeight(textModel: TextModel): Int {
        if (testWordRect == null) {
            val testWordPaint = Paint()
            testWordPaint.textSize = Utility.dip2px(textModel.textSize).toFloat()
            testWordPaint.isAntiAlias = true
            testWordRect = Rect()
            testWordPaint.getTextBounds(testWord, 0, testWord.length, testWordRect)
        }
        return testWordRect!!.height()
    }

    @JvmStatic
    fun getLineSpacingExtra(textSize: Float): Int {
        var lineSpacingExtra = 0
        try {
            testWordRect = null
            val textModel = TextModel()
            textModel.textSize = textSize - 8
            val contentHeight = getTestWordHeight(textModel)
            lineSpacingExtra = contentHeight
        } catch (_: Exception) {
        }
        return lineSpacingExtra
    }

    @JvmStatic
    fun getPageInfos(
        bookChapter: NovelDetail?,
        settingInfo: ReadSettingInfo?,
        ctContent: View?,
    ): List<PageInfoModel> {
        val lisPages: MutableList<PageInfoModel> = ArrayList()
        if (bookChapter == null ||
            TextUtils.isEmpty(bookChapter.novel_text) ||
            settingInfo == null ||
            ctContent == null
        ) {
            return lisPages
        }

        val chapterTextSize = ConstantPageInfo.tipTextSize
        val titleTextSize = settingInfo.frontSize * 1.3f
        val contentTextSize = settingInfo.frontSize

        val lisText: MutableList<TextModel> = ArrayList()
        var textModel: TextModel
        var start: Int

        start = 0
        textModel = TextModel()
        textModel.textSize = contentTextSize
        textModel.height = getTestWordHeight(textModel)
        lisText.add(textModel)

        while (true) {
            testWordRect = null
            textModel =
                getShowLines("第两千两百四十八章 上品地脉", titleTextSize, true, start, settingInfo, ctContent)
            if (textModel.textLength <= 0) {
                break
            }
            textModel.isTitle = true
            textModel.textSize = titleTextSize
            textModel.fakeBoldText = true
            lisText.add(textModel)
            start += textModel.textLength
        }

        val arrWrapContent = bookChapter.novel_text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        Common.showLog("arrWrapContent " + arrWrapContent.size)
        for (s in arrWrapContent) {
            start = 0
            var linePosition = 0
            while (true) {
                if (TextUtils.isEmpty(s)) {
                    textModel = TextModel()
                    textModel.textSize = contentTextSize
                    textModel.height = getTestWordHeight(textModel)
                    lisText.add(textModel)
                    break
                }

                textModel = getShowLines(s, contentTextSize, false, start, settingInfo, ctContent)
                if (textModel.textLength <= 0) {
                    break
                }
                textModel.partFirstLine = linePosition == 0
                textModel.isTitle = false
                textModel.fakeBoldText = false
                lisText.add(textModel)
                start += textModel.textLength
                linePosition++
            }
        }

        var pageInfo = PageInfoModel()
        var pageTextHeight = 0
        val ctContentWordsHeight =
            ctContent.measuredHeight - ctContent.paddingTop - ctContent.paddingBottom
        if (ctContentWordsHeight <= 0) {
            return lisPages
        }

        try {
            var i = 0
            while (i < lisText.size) {
                if (Common.isEmpty(pageInfo.lisText)) {
                    testWordRect = null

                    textModel = TextModel()
                    textModel.textSize = contentTextSize
                    textModel.height = settingInfo.lineSpacingExtra
                    pageInfo.lisText.add(textModel)
                    pageTextHeight += textModel.height

                    textModel = TextModel()
                    textModel.isChapter = true
                    textModel.textSize = chapterTextSize
                    textModel.text = "第两千两百四十八章 上品地脉"
                    textModel.height = getTestWordHeight(textModel)
                    pageInfo.lisText.add(textModel)
                    pageTextHeight += textModel.height

                    textModel = TextModel()
                    textModel.textSize = contentTextSize
                    textModel.height = (settingInfo.lineSpacingExtra * 1.3f).toInt()
                    pageInfo.lisText.add(textModel)
                    pageTextHeight += textModel.height
                } else {
                    if (lisText[i - 1].isTitle) {
                        testWordRect = null
                        textModel = TextModel()
                        textModel.textSize = titleTextSize
                        textModel.height = getTestWordHeight(textModel)
                        pageInfo.lisText.add(textModel)
                    } else {
                        textModel = TextModel()
                        textModel.textSize = contentTextSize
                        textModel.height =
                            if (lisText[i].partFirstLine) {
                                settingInfo.lineSpacingExtra * 2
                            } else {
                                settingInfo.lineSpacingExtra
                            }
                        pageInfo.lisText.add(textModel)
                    }
                    pageTextHeight += textModel.height
                }

                pageInfo.lisText.add(lisText[i])
                pageTextHeight += lisText[i].height

                if (pageTextHeight > ctContentWordsHeight) {
                    pageInfo.lisText.removeAt(pageInfo.lisText.size - 1)
                    pageInfo.pages = lisPages.size + 1
                    pageInfo.title = "第两千两百四十八章 上品地脉"
                    lisPages.add(pageInfo)

                    pageInfo = PageInfoModel()
                    pageTextHeight = 0
                    i--
                } else if (i == lisText.size - 1) {
                    lisPages.add(pageInfo)
                }
                i++
            }
        } catch (_: Exception) {
        }

        Common.showLog("arrWrapContent 11 " + lisPages.size)

        try {
            var isEmpty: Boolean
            for (i in lisPages.size - 1 downTo 0) {
                isEmpty = true
                for (j in lisPages[i].lisText.indices) {
                    textModel = lisPages[i].lisText[j]
                    if (!textModel.isChapter && !textModel.isTitle && !TextUtils.isEmpty(textModel.text)) {
                        isEmpty = false
                        break
                    }
                }

                if (isEmpty) {
                    lisPages.removeAt(i)
                } else {
                    break
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        Common.showLog("arrWrapContent 22 " + lisPages.size)
        return lisPages
    }

    @JvmStatic
    fun getPubShowLineWords(settingInfo: ReadSettingInfo, ctContent: View): Int {
        if (pubLineShowWords == 0) {
            val showWidth = ctContent.measuredWidth - ctContent.paddingLeft - ctContent.paddingRight

            val paint = Paint()
            paint.textSize = Utility.dip2px(settingInfo.frontSize).toFloat()

            val maxWidth = showWidth * 10
            val measuredWidth = FloatArray(1)

            val sbWords = StringBuffer(testWord)
            var i = 0
            while (true) {
                paint.breakText(sbWords.toString(), true, maxWidth.toFloat(), measuredWidth)
                if (measuredWidth[0] < showWidth) {
                    sbWords.append(testWord)
                } else {
                    pubLineShowWords = i
                    break
                }
                i++
            }
        }
        return pubLineShowWords
    }

    private fun getShowLines(
        content: String,
        textSize: Float,
        fakeBold: Boolean,
        start: Int,
        settingInfo: ReadSettingInfo,
        ctContent: View,
    ): TextModel {
        val textModel = TextModel()
        if (content.length <= start) {
            return textModel
        }

        val showWidth = ctContent.measuredWidth - ctContent.paddingLeft - ctContent.paddingRight

        val paint = Paint()
        paint.textSize = Utility.dip2px(textSize).toFloat()
        paint.isFakeBoldText = fakeBold
        paint.isAntiAlias = true

        val pubWords = getPubShowLineWords(settingInfo, ctContent)
        val measuredWidth = FloatArray(1)

        val sb = StringBuffer()
        if (content.length - start <= pubWords) {
            sb.append(content.substring(start))
        } else {
            sb.append(content.substring(start, start + pubWords))
        }

        val maxWidth = showWidth * 10
        var beginIndex: Int
        while (true) {
            paint.breakText(sb.toString(), true, maxWidth.toFloat(), measuredWidth)
            if (measuredWidth[0] < showWidth) {
                beginIndex = start + sb.length
                if (content.length <= beginIndex) {
                    break
                } else {
                    sb.append(content[beginIndex])
                }
            } else {
                break
            }
        }

        while (true) {
            paint.breakText(sb.toString(), true, maxWidth.toFloat(), measuredWidth)
            if (measuredWidth[0] > showWidth) {
                if (sb.length <= 1) {
                    break
                } else {
                    sb.deleteCharAt(sb.length - 1)
                }
            } else {
                break
            }
        }

        val text = sb.toString()
        textModel.text = text
        textModel.textLength = text.length
        textModel.textSize = textSize

        val rect = Rect()
        paint.getTextBounds(text, 0, textModel.textLength, rect)
        textModel.height =
            if (rect.height() < getTestWordHeight(textModel)) {
                getTestWordHeight(textModel)
            } else {
                rect.height()
            }

        return textModel
    }
}
