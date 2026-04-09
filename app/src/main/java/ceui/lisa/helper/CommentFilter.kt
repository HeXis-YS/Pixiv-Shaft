package ceui.lisa.helper

import android.text.TextUtils
import ceui.lisa.activities.Shaft
import ceui.lisa.http.NullCtrl
import ceui.lisa.http.Retro
import ceui.lisa.models.ReplyCommentBean
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import java.io.IOException
import java.util.regex.Pattern

object CommentFilter {

    private var rules: List<CommentFilterRule> = ArrayList()

    init {
        updateRules()
    }

    @JvmStatic
    fun judge(commentsBean: ReplyCommentBean): Boolean {
        return rules.any { rule ->
            rule.judge(commentsBean.comment) ||
                ((commentsBean.parent_comment.id > 0) && rule.judge(commentsBean.parent_comment.comment))
        }
    }

    private fun updateRules() {
        updateRulesFromLocal()
        updateRulesFromRemote()
    }

    private fun updateRulesFromLocal() {
        try {
            val inputStream = Shaft.getContext().assets.open("comment.filter.rule.txt")
            val buffer = ByteArray(inputStream.available())
            inputStream.read(buffer)
            inputStream.close()
            val content = String(buffer)
            rules = content.split(Regex("\\r?\\n"), -1)
                .filter { !TextUtils.isEmpty(it) }
                .map(::CommentFilterRule)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateRulesFromRemote() {
        Retro.getResourceApi().getCommentFilterRule()
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : NullCtrl<ResponseBody>() {
                override fun success(responseBody: ResponseBody) {
                    try {
                        val content = responseBody.string()
                        if (TextUtils.isEmpty(content)) {
                            return
                        }

                        rules = content.split(Regex("\\r?\\n"), -1)
                            .filter { !TextUtils.isEmpty(it) }
                            .map(::CommentFilterRule)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            })
    }

    private class CommentFilterRule {
        private val ruleType: Int
        private val ruleValue: String
        private var patterns: List<Pattern>? = null

        constructor(ruleType: Int, ruleValue: String) {
            this.ruleType = ruleType
            this.ruleValue = ruleValue
            init()
        }

        constructor(ruleType: String, ruleValue: String) {
            this.ruleType = ruleType.toInt()
            this.ruleValue = ruleValue
            init()
        }

        constructor(ruleLine: String) {
            val separatorIndex = ruleLine.indexOf(",")
            this.ruleType = ruleLine.substring(0, separatorIndex).toInt()
            this.ruleValue = ruleLine.substring(separatorIndex + 1)
            init()
        }

        private fun init() {
            patterns = when (ruleType) {
                URL_DOMAIN -> {
                    val regexRuleValue = ruleValue.replace(".", "\\.")
                    listOf(Pattern.compile("https?://([0-9a-zA-Z]+\\.)*$regexRuleValue"))
                }

                REGEX_MATCH -> {
                    listOf(Pattern.compile(ruleValue))
                }

                LIST_ALL_REGEX_MATCH -> {
                    ruleValue.split("$$").map { Pattern.compile(it) }
                }

                else -> null
            }
        }

        fun judge(input: String?): Boolean {
            val localPatterns = patterns
            if (localPatterns.isNullOrEmpty() || TextUtils.isEmpty(input)) {
                return false
            }
            return when (ruleType) {
                URL_DOMAIN, REGEX_MATCH -> localPatterns[0].matcher(input).find()
                LIST_ALL_REGEX_MATCH -> localPatterns.all { it.matcher(input).find() }
                else -> false
            }
        }

        companion object {
            private const val URL_DOMAIN = 0
            private const val REGEX_MATCH = 1
            private const val LIST_ALL_REGEX_MATCH = 2
        }
    }
}
