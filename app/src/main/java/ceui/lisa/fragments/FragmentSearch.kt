package ceui.lisa.fragments

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import ceui.lisa.R
import ceui.lisa.activities.OutWakeActivity
import ceui.lisa.activities.SearchActivity
import ceui.lisa.activities.Shaft
import ceui.lisa.activities.UActivity
import ceui.lisa.adapters.SearchHintAdapter
import ceui.lisa.core.RxRun
import ceui.lisa.core.RxRunnable
import ceui.lisa.core.TryCatchObserverImpl
import ceui.lisa.database.AppDatabase
import ceui.lisa.database.SearchEntity
import ceui.lisa.databinding.FragmentSearchBinding
import ceui.lisa.databinding.RecySingleLineTextWithDeleteBinding
import ceui.lisa.http.ErrorCtrl
import ceui.lisa.http.NullCtrl
import ceui.lisa.http.Retro
import ceui.lisa.interfaces.Callback
import ceui.lisa.interfaces.OnItemClickListener
import ceui.lisa.interfaces.OnItemLongClickListener
import ceui.lisa.model.ListTrendingtag
import ceui.lisa.utils.ClipBoardUtils
import ceui.lisa.utils.Common
import ceui.lisa.utils.Params
import ceui.lisa.utils.PixivOperate
import ceui.lisa.utils.SearchTypeUtil
import com.qmuiteam.qmui.skin.QMUISkinManager
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import com.zhy.view.flowlayout.FlowLayout
import com.zhy.view.flowlayout.TagAdapter
import com.zhy.view.flowlayout.TagFlowLayout
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class FragmentSearch : BaseFragment<FragmentSearchBinding>() {
    companion object {
        private const val SEARCH_MODE_KEYWORD = 0
        private const val SEARCH_MODE_ILLUST_ID = 1
        private const val SEARCH_MODE_USER_ID = 2
        private const val SEARCH_MODE_NOVEL_ID = 3
        private const val SEARCH_MODE_URL = 4
        private const val SEARCH_MODE_ALL = 5
    }

    private var searchEmitter: ObservableEmitter<String>? = null
    private var searchType = SearchTypeUtil.defaultSearchType
    private var hasSwitchSearchType = false

    override fun initLayout() {
        mLayoutID = R.layout.fragment_search
    }

    override fun initData() {
        val searchTypes = SearchTypeUtil.SEARCH_TYPE_NAME

        val headParams = baseBind.head.layoutParams
        headParams.height = Shaft.statusHeight
        baseBind.head.layoutParams = headParams

        Observable.create<String> { emitter ->
            searchEmitter = emitter
        }.subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .debounce(800, TimeUnit.MILLISECONDS)
            .subscribe(
                object : ErrorCtrl<String>() {
                    override fun next(t: String) {
                        completeWord(t)
                    }
                },
            )

        baseBind.inputBox.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    charSequence: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                }

                override fun onTextChanged(
                    charSequence: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {
                    val inputs = charSequence.toString()
                    baseBind.clear.visibility =
                        if (inputs.isNotEmpty()) {
                            View.VISIBLE
                        } else {
                            View.INVISIBLE
                        }

                    val shouldRequestKeyword =
                        inputs.isNotEmpty() &&
                            !inputs.endsWith(" ") &&
                            (
                                searchType == SEARCH_MODE_KEYWORD ||
                                    (searchType == SEARCH_MODE_ALL && !Common.isNumeric(inputs))
                            )

                    if (shouldRequestKeyword && searchEmitter != null) {
                        val keys = inputs.split(" ").filter { !TextUtils.isEmpty(it) }
                        if (keys.isNotEmpty()) {
                            searchEmitter?.onNext(keys.last())
                        }
                    } else {
                        baseBind.hintList.visibility = View.GONE
                    }
                }

                override fun afterTextChanged(editable: Editable) {
                }
            },
        )

        baseBind.inputBox.setOnEditorActionListener(
            object : TextView.OnEditorActionListener {
                override fun onEditorAction(
                    textView: TextView,
                    actionId: Int,
                    keyEvent: KeyEvent?,
                ): Boolean {
                    return if (!TextUtils.isEmpty(baseBind.inputBox.text.toString())) {
                        Common.hideKeyboard(mActivity)
                        dispatchClick(baseBind.inputBox.text.toString(), searchType)
                        true
                    } else {
                        Common.showToast(getString(R.string.string_148))
                        false
                    }
                }
            },
        )

        baseBind.inputBox.setOnFocusChangeListener(
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus && baseBind.hintList.adapter != null) {
                    baseBind.hintList.visibility = View.VISIBLE
                }
            },
        )

        baseBind.clear.setOnClickListener {
            baseBind.inputBox.setText("")
        }

        baseBind.inputBox.setOnTouchListener(
            View.OnTouchListener { _, _: MotionEvent? ->
                val adapter = baseBind.hintList.adapter as? SearchHintAdapter
                if (adapter != null &&
                    adapter.getKeyword() == baseBind.inputBox.text.toString()
                ) {
                    baseBind.hintList.visibility = View.VISIBLE
                }
                false
            },
        )

        baseBind.container.setOnClickListener {
            if (baseBind.hintList.visibility == View.VISIBLE) {
                baseBind.hintList.visibility = View.INVISIBLE
            }
        }

        baseBind.more.setOnClickListener {
            popUpSearchTypeSwitcher()
        }

        baseBind.inputBox.hint = searchTypes[searchType]
        getHotTags()
    }

    private fun dispatchClick(keyWord: String, searchType: Int) {
        val trimmedKeyword = keyWord.trim()
        when (searchType) {
            SEARCH_MODE_KEYWORD -> {
                baseBind.hintList.visibility = View.INVISIBLE
                val intent = Intent(mContext, SearchActivity::class.java)
                intent.putExtra(Params.KEY_WORD, trimmedKeyword)
                intent.putExtra(Params.INDEX, 0)
                startActivity(intent)
            }

            SEARCH_MODE_ILLUST_ID -> {
                if (Common.isNumeric(trimmedKeyword)) {
                    PixivOperate.insertSearchHistory(
                        trimmedKeyword,
                        SearchTypeUtil.SEARCH_TYPE_DB_ILLUSTSID,
                    )
                    PixivOperate.getIllustByID(Shaft.sUserModel, tryParseId(trimmedKeyword), mContext)
                } else {
                    Common.showToast(getString(R.string.string_154))
                }
            }

            SEARCH_MODE_USER_ID -> {
                if (Common.isNumeric(trimmedKeyword)) {
                    PixivOperate.insertSearchHistory(
                        trimmedKeyword,
                        SearchTypeUtil.SEARCH_TYPE_DB_USERID,
                    )
                    val intent = Intent(mContext, UActivity::class.java)
                    intent.putExtra(Params.USER_ID, trimmedKeyword.toInt())
                    startActivity(intent)
                } else {
                    Common.showToast(getString(R.string.string_154))
                }
            }

            SEARCH_MODE_NOVEL_ID -> {
                if (Common.isNumeric(trimmedKeyword)) {
                    PixivOperate.insertSearchHistory(
                        trimmedKeyword,
                        SearchTypeUtil.SEARCH_TYPE_DB_NOVELID,
                    )
                    PixivOperate.getNovelByID(
                        Shaft.sUserModel,
                        tryParseId(trimmedKeyword),
                        mContext,
                        null,
                    )
                } else {
                    Common.showToast(getString(R.string.string_154))
                }
            }

            SEARCH_MODE_URL -> {
                if (!TextUtils.isEmpty(trimmedKeyword) && URLUtil.isValidUrl(trimmedKeyword)) {
                    try {
                        PixivOperate.insertSearchHistory(
                            trimmedKeyword,
                            SearchTypeUtil.SEARCH_TYPE_DB_URL,
                        )
                        val intent = Intent(mContext, OutWakeActivity::class.java)
                        intent.data = Uri.parse(trimmedKeyword)
                        startActivity(intent)
                    } catch (e: Exception) {
                        Common.showToast(e.toString())
                        e.printStackTrace()
                    }
                } else {
                    Common.showToast(getString(R.string.string_408))
                }
            }

            SEARCH_MODE_ALL -> {
                when {
                    URLUtil.isValidUrl(trimmedKeyword) -> {
                        try {
                            PixivOperate.insertSearchHistory(
                                trimmedKeyword,
                                SearchTypeUtil.SEARCH_TYPE_DB_URL,
                            )
                            val intent = Intent(mContext, OutWakeActivity::class.java)
                            intent.data = Uri.parse(trimmedKeyword)
                            startActivity(intent)
                        } catch (e: Exception) {
                            Common.showToast(e.toString())
                            e.printStackTrace()
                        }
                    }

                    Common.isNumeric(trimmedKeyword) -> {
                        val tipDialog =
                            QMUITipDialog.Builder(mContext)
                                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                                .setTipWord(getString(R.string.string_429))
                                .create()
                        tipDialog.show()
                        PixivOperate.getIllustByID(
                            Shaft.sUserModel,
                            tryParseId(trimmedKeyword),
                            mContext,
                            object : Callback<Void> {
                                override fun doSomething(t: Void) {
                                    PixivOperate.insertSearchHistory(
                                        trimmedKeyword,
                                        SearchTypeUtil.SEARCH_TYPE_DB_ILLUSTSID,
                                    )
                                    tipDialog.dismiss()
                                }
                            },
                            object : Callback<Void> {
                                override fun doSomething(t: Void) {
                                    tipDialog.dismiss()
                                    PixivOperate.insertSearchHistory(
                                        trimmedKeyword,
                                        SearchTypeUtil.SEARCH_TYPE_DB_USERID,
                                    )
                                    val intent = Intent(mContext, UActivity::class.java)
                                    intent.putExtra(Params.USER_ID, trimmedKeyword.toInt())
                                    startActivity(intent)
                                }
                            },
                        )
                    }

                    else -> {
                        baseBind.hintList.visibility = View.INVISIBLE
                        val intent = Intent(mContext, SearchActivity::class.java)
                        intent.putExtra(Params.KEY_WORD, trimmedKeyword)
                        intent.putExtra(Params.INDEX, 0)
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun completeWord(key: String) {
        Retro.getAppApi().searchCompleteWord(Shaft.sUserModel.access_token, key)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                object : NullCtrl<ListTrendingtag>() {
                    override fun success(listTrendingtag: ListTrendingtag) {
                        baseBind.hintList.layoutManager = LinearLayoutManager(mContext)
                        val searchHintAdapter = SearchHintAdapter(listTrendingtag.list, mContext, key)
                        searchHintAdapter.setOnItemClickListener(
                            OnItemClickListener { _, position, _ ->
                                baseBind.hintList.visibility = View.INVISIBLE
                                val keyword = listTrendingtag.list[position].getTag()
                                val intent = Intent(mContext, SearchActivity::class.java)
                                intent.putExtra(Params.KEY_WORD, keyword)
                                intent.putExtra(Params.INDEX, 0)
                                startActivity(intent)
                            },
                        )
                        searchHintAdapter.setOnItemLongClickListener(
                            OnItemLongClickListener { _, position, _ ->
                                baseBind.hintList.visibility = View.INVISIBLE
                                val currentInput = baseBind.inputBox.text.toString()
                                val keys = currentInput.split(" ").filter { !TextUtils.isEmpty(it) }.toMutableList()
                                val tagName = listTrendingtag.list[position].getTag()
                                val builder = StringBuilder()
                                if (keys.isNotEmpty()) {
                                    keys[keys.lastIndex] = tagName
                                    builder.append(keys.joinToString(" "))
                                } else {
                                    builder.append(tagName)
                                }
                                baseBind.inputBox.setText(builder.append(" ").toString())
                                baseBind.inputBox.setSelection(baseBind.inputBox.text.length)
                            },
                        )
                        baseBind.hintList.adapter = searchHintAdapter
                        baseBind.hintList.visibility = View.VISIBLE
                    }
                },
            )
    }

    private fun getHotTags() {
        Retro.getAppApi().getHotTags(Shaft.sUserModel.access_token, Params.TYPE_ILLUST)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                object : NullCtrl<ListTrendingtag>() {
                    override fun success(listTrendingtag: ListTrendingtag) {
                        baseBind.hotTags.adapter =
                            object : TagAdapter<ListTrendingtag.TrendTagsBean>(
                                listTrendingtag.list.subList(0, 15),
                            ) {
                                override fun getView(
                                    parent: FlowLayout,
                                    position: Int,
                                    trendTagsBean: ListTrendingtag.TrendTagsBean,
                                ): View {
                                    val textView =
                                        LayoutInflater.from(mContext).inflate(
                                            R.layout.recy_single_line_text,
                                            parent,
                                            false,
                                        ) as TextView
                                    textView.text =
                                        if (!TextUtils.isEmpty(trendTagsBean.translated_name)) {
                                            String.format(
                                                "%s/%s",
                                                trendTagsBean.getTag(),
                                                trendTagsBean.translated_name,
                                            )
                                        } else {
                                            trendTagsBean.getTag()
                                        }
                                    return textView
                                }
                            }
                        baseBind.hotTags.setOnTagClickListener(
                            TagFlowLayout.OnTagClickListener { _, position, _ ->
                                baseBind.hintList.visibility = View.INVISIBLE
                                val keyword = listTrendingtag.list[position].getTag()
                                val intent = Intent(mContext, SearchActivity::class.java)
                                intent.putExtra(Params.KEY_WORD, keyword)
                                intent.putExtra(Params.INDEX, 0)
                                startActivity(intent)
                                false
                            },
                        )
                        baseBind.hotTags.setOnTagLongClickListener(
                            TagFlowLayout.OnTagLongClickListener { _, position, _ ->
                                Common.copy(mContext, listTrendingtag.list[position].getTag())
                                true
                            },
                        )
                    }
                },
            )
    }

    override fun onResume() {
        super.onResume()
        loadHistory()
        predictSearchType()
    }

    private fun loadHistory() {
        RxRun.runOn(
            object : RxRunnable<List<SearchEntity>>() {
                override fun execute(): List<SearchEntity> {
                    return AppDatabase.searchDao(Shaft.getContext()).getAll(50)
                }
            },
            object : TryCatchObserverImpl<List<SearchEntity>>() {
                override fun next(history: List<SearchEntity>) {
                    bindHistory(history)
                }
            },
        )
    }

    private fun bindHistory(history: List<SearchEntity>) {
        baseBind.searchHistory.adapter =
            object : TagAdapter<SearchEntity>(history) {
                override fun getView(
                    parent: FlowLayout,
                    position: Int,
                    searchEntity: SearchEntity,
                ): View {
                    val binding =
                        RecySingleLineTextWithDeleteBinding.inflate(
                            LayoutInflater.from(mContext),
                            parent,
                            false,
                        )
                    if (searchEntity.isPinned) {
                        binding.fixed.visibility = View.VISIBLE
                        binding.deleteItem.visibility = View.GONE
                    } else {
                        binding.fixed.visibility = View.GONE
                        binding.deleteItem.visibility = View.VISIBLE
                    }
                    binding.tagTitle.text = searchEntity.keyword
                    binding.deleteItem.setOnClickListener {
                        deleteSearchEntity(searchEntity)
                        Common.showToast("删除成功")
                        loadHistory()
                    }
                    return binding.root
                }
            }

        if (history.isNotEmpty()) {
            baseBind.clearHistory.visibility = View.VISIBLE
            baseBind.clearHistory.setOnClickListener {
                QMUIDialog.MessageDialogBuilder(mActivity)
                    .setTitle(getString(R.string.string_143))
                    .setMessage(getString(R.string.string_144))
                    .setSkinManager(QMUISkinManager.defaultInstance(mContext))
                    .addAction(
                        getString(R.string.string_142),
                        object : QMUIDialogAction.ActionListener {
                            override fun onClick(dialog: QMUIDialog, index: Int) {
                                dialog.dismiss()
                            }
                        },
                    )
                    .addAction(
                        0,
                        getString(R.string.string_141),
                        QMUIDialogAction.ACTION_PROP_NEGATIVE,
                        object : QMUIDialogAction.ActionListener {
                            override fun onClick(dialog: QMUIDialog, index: Int) {
                                AppDatabase.searchDao(Shaft.getContext()).deleteAllUnpinned()
                                Common.showToast(getString(R.string.string_140))
                                dialog.dismiss()
                                onResume()
                            }
                        },
                    )
                    .show()
            }
        } else {
            baseBind.clearHistory.visibility = View.INVISIBLE
        }

        baseBind.searchHistory.setOnTagClickListener(
            TagFlowLayout.OnTagClickListener { _, position, _ ->
                val searchEntity = history[position]
                when (searchEntity.searchType) {
                    SearchTypeUtil.SEARCH_TYPE_DB_KEYWORD -> {
                        baseBind.hintList.visibility = View.INVISIBLE
                        val intent = Intent(mContext, SearchActivity::class.java)
                        intent.putExtra(Params.KEY_WORD, searchEntity.keyword)
                        intent.putExtra(Params.INDEX, 0)
                        startActivity(intent)
                    }

                    SearchTypeUtil.SEARCH_TYPE_DB_ILLUSTSID -> {
                        searchEntity.searchTime = System.currentTimeMillis()
                        persistSearchEntity(searchEntity)
                        PixivOperate.getIllustByID(
                            Shaft.sUserModel,
                            tryParseId(searchEntity.keyword!!),
                            mContext,
                        )
                    }

                    SearchTypeUtil.SEARCH_TYPE_DB_USERKEYWORD -> {
                        baseBind.hintList.visibility = View.INVISIBLE
                        val intent = Intent(mContext, SearchActivity::class.java)
                        intent.putExtra(Params.KEY_WORD, searchEntity.keyword)
                        intent.putExtra(Params.INDEX, 0)
                        startActivity(intent)
                    }

                    SearchTypeUtil.SEARCH_TYPE_DB_USERID -> {
                        searchEntity.searchTime = System.currentTimeMillis()
                        persistSearchEntity(searchEntity)
                        val intent = Intent(mContext, UActivity::class.java)
                        intent.putExtra(Params.USER_ID, searchEntity.keyword!!.toInt())
                        startActivity(intent)
                    }

                    SearchTypeUtil.SEARCH_TYPE_DB_NOVELID -> {
                        searchEntity.searchTime = System.currentTimeMillis()
                        persistSearchEntity(searchEntity)
                        PixivOperate.getNovelByID(
                            Shaft.sUserModel,
                            tryParseId(searchEntity.keyword!!),
                            mContext,
                            null,
                        )
                    }

                    SearchTypeUtil.SEARCH_TYPE_DB_URL -> {
                        searchEntity.searchTime = System.currentTimeMillis()
                        persistSearchEntity(searchEntity)
                        val intent = Intent(mContext, OutWakeActivity::class.java)
                        intent.data = Uri.parse(searchEntity.keyword!!)
                        startActivity(intent)
                    }
                }
                false
            },
        )

        baseBind.searchHistory.setOnTagLongClickListener(
            TagFlowLayout.OnTagLongClickListener { _, position, _ ->
                val searchEntity = history[position]
                QMUIDialog.MessageDialogBuilder(mContext)
                    .setTitle(R.string.string_87)
                    .setMessage(searchEntity.keyword)
                    .setSkinManager(QMUISkinManager.defaultInstance(mActivity))
                    .addAction(
                        getString(R.string.string_142),
                        object : QMUIDialogAction.ActionListener {
                            override fun onClick(dialog: QMUIDialog, index: Int) {
                                dialog.dismiss()
                            }
                        },
                    )
                    .addAction(
                        if (searchEntity.isPinned) {
                            getString(R.string.string_443)
                        } else {
                            getString(R.string.string_442)
                        },
                        object : QMUIDialogAction.ActionListener {
                            override fun onClick(dialog: QMUIDialog, index: Int) {
                                searchEntity.isPinned = !searchEntity.isPinned
                                persistSearchEntity(searchEntity)
                                baseBind.searchHistory.adapter.notifyDataChanged()
                                dialog.dismiss()
                            }
                        },
                    )
                    .addAction(
                        getString(R.string.string_120),
                        object : QMUIDialogAction.ActionListener {
                            override fun onClick(dialog: QMUIDialog, index: Int) {
                                Common.copy(mContext, searchEntity.keyword!!)
                                dialog.dismiss()
                            }
                        },
                    )
                    .show()
                true
            },
        )
    }

    private fun persistSearchEntity(searchEntity: SearchEntity) {
        RxRun.runOn(
            object : RxRunnable<SearchEntity>() {
                override fun execute(): SearchEntity {
                    AppDatabase.searchDao(mContext).insert(searchEntity)
                    return searchEntity
                }
            },
            TryCatchObserverImpl(),
        )
    }

    private fun deleteSearchEntity(searchEntity: SearchEntity) {
        RxRun.runOn(
            object : RxRunnable<SearchEntity>() {
                override fun execute(): SearchEntity {
                    AppDatabase.searchDao(mContext).deleteSearchEntity(searchEntity)
                    return searchEntity
                }
            },
            TryCatchObserverImpl(),
        )
    }

    private fun predictSearchType() {
        if (hasSwitchSearchType) {
            return
        }
        if (!TextUtils.isEmpty(baseBind.inputBox.text.toString())) {
            return
        }
        mActivity.window.decorView.post {
            val content = ClipBoardUtils.getClipboardContent(mContext)
            val previousClipboardValue =
                Shaft.getMMKV().getString(Params.FRAGMENT_SEARCH_CLIPBOARD_VALUE, "")
            if (!TextUtils.isEmpty(previousClipboardValue) && previousClipboardValue == content) {
                return@post
            }
            val suggestSearchType = SearchTypeUtil.getSuggestSearchType(content)
            if (suggestSearchType != searchType) {
                searchType = suggestSearchType
                baseBind.inputBox.hint = SearchTypeUtil.SEARCH_TYPE_NAME[searchType]
                popUpSearchTypeSwitcher(true, content)
            }
        }
    }

    private fun popUpSearchTypeSwitcher() {
        popUpSearchTypeSwitcher(false, null)
    }

    private fun popUpSearchTypeSwitcher(fromClipboard: Boolean, clipboardContent: String?) {
        val searchTypes = SearchTypeUtil.SEARCH_TYPE_NAME
        QMUIDialog.CheckableDialogBuilder(mContext)
            .setTitle(if (fromClipboard) R.string.string_425 else R.string.string_424)
            .setCheckedIndex(searchType)
            .setSkinManager(QMUISkinManager.defaultInstance(mContext))
            .addItems(
                searchTypes,
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, which: Int) {
                        if (searchType != which) {
                            searchType = which
                            baseBind.inputBox.hint = searchTypes[which]
                        }
                        if (fromClipboard) {
                            Shaft.getMMKV().putString(
                                Params.FRAGMENT_SEARCH_CLIPBOARD_VALUE,
                                clipboardContent,
                            )
                        }
                        if (fromClipboard &&
                            searchType != SEARCH_MODE_KEYWORD &&
                            searchType != SEARCH_MODE_ALL
                        ) {
                            baseBind.inputBox.setText(clipboardContent)
                        }
                        dialog.dismiss()
                    }
                },
            ).create()
            .show()
        if (fromClipboard) {
            hasSwitchSearchType = true
        }
    }
}
