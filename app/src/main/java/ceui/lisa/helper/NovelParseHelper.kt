package ceui.lisa.helper

import android.text.TextUtils
import ceui.lisa.models.NovelDetail
import java.util.regex.Pattern

object NovelParseHelper {
    private val PAGE_PATTERN: Pattern = Pattern.compile("\\[newpage]")
    private val CHAPTER_PATTERN: Pattern = Pattern.compile("\\[chapter:(.+)]")

    @JvmStatic
    fun tryParseChapters(content: String?): List<NovelDetail.NovelChapterBean>? {
        if (TextUtils.isEmpty(content)) {
            return null
        }

        val result = ArrayList<NovelDetail.NovelChapterBean>()
        val pageContents = ArrayList(PAGE_PATTERN.split(content).asList())
        for (pageContentItem in pageContents) {
            if (TextUtils.isEmpty(pageContentItem)) {
                continue
            }
            var pageContent = pageContentItem
            val bean = NovelDetail.NovelChapterBean()
            bean.chapterIndex = result.size + 1

            val matcher = CHAPTER_PATTERN.matcher(pageContent)
            if (matcher.find()) {
                bean.chapterName = matcher.group(1)
                pageContent = pageContent.substring(0, matcher.start()) + pageContent.substring(matcher.end())
            } else {
                bean.chapterName = bean.chapterIndex.toString()
            }

            bean.chapterContent = pageContent
            result.add(bean)
        }

        return result
    }
}
