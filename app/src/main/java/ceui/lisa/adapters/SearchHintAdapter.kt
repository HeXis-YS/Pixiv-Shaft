package ceui.lisa.adapters

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import ceui.lisa.R
import ceui.lisa.databinding.RecySearchHintBinding
import ceui.lisa.model.ListTrendingtag
import ceui.lisa.utils.Common
import java.util.Locale
import java.util.regex.Pattern

class SearchHintAdapter(
    targetList: List<ListTrendingtag.TrendTagsBean>,
    context: Context,
    keyword: String,
) : BaseAdapter<ListTrendingtag.TrendTagsBean, RecySearchHintBinding>(targetList, context) {

    private var mKeyword: String = keyword

    override fun initLayout() {
        mLayoutID = R.layout.recy_search_hint
    }

    override fun bindData(
        target: ListTrendingtag.TrendTagsBean,
        bindView: ViewHolder<RecySearchHintBinding>,
        position: Int,
    ) {
        val string = matcherSearchText(
            Common.resolveThemeAttribute(mContext, androidx.appcompat.R.attr.colorPrimary),
            target.name.orEmpty(),
            mKeyword,
        )
        bindView.baseBind.titleText.text = string
        if (!TextUtils.isEmpty(target.translated_name) && target.translated_name != target.name) {
            bindView.baseBind.translatedText.text = String.format("译：%s", target.translated_name)
        }
        if (mOnItemClickListener != null) {
            bindView.itemView.setOnClickListener { view ->
                mOnItemClickListener.onItemClick(view, position, 0)
            }
        }
        if (mOnItemLongClickListener != null) {
            bindView.itemView.setOnLongClickListener { view ->
                mOnItemLongClickListener.onItemLongClick(view, position, 0)
                true
            }
        }
    }

    private fun matcherSearchText(color: Int, text: String, keyword: String): SpannableString {
        val spannableString = SpannableString(text)
        val pattern = Pattern.compile(keyword)
        val matcher = pattern.matcher(SpannableString(text.lowercase(Locale.getDefault())))
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            spannableString.setSpan(
                ForegroundColorSpan(color),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }
        return spannableString
    }

    fun getKeyword(): String {
        return mKeyword
    }

    fun setKeyword(keyword: String) {
        mKeyword = keyword
    }
}
