package ceui.lisa.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.MenuItem
import android.webkit.URLUtil
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import ceui.lisa.R
import ceui.lisa.databinding.FragmentNewSearchBinding
import ceui.lisa.fragments.BaseFragment
import ceui.lisa.fragments.FragmentFilter
import ceui.lisa.fragments.FragmentSearchIllust
import ceui.lisa.fragments.FragmentSearchNovel
import ceui.lisa.fragments.FragmentSearchUser
import ceui.lisa.interfaces.Callback
import ceui.lisa.utils.Common
import ceui.lisa.utils.Params
import ceui.lisa.utils.PixivOperate
import ceui.lisa.utils.SearchTypeUtil
import ceui.lisa.viewmodel.SearchModel
import com.mxn.soul.flowingdrawer_core.ElasticDrawer
import com.qmuiteam.qmui.skin.QMUISkinManager
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog

class SearchActivity : BaseActivity<FragmentNewSearchBinding>() {

    private val allPages = arrayOfNulls<BaseFragment<*>>(3)
    private var fragmentFilter: FragmentFilter? = null
    private var keyWord = ""
    private lateinit var searchModel: SearchModel
    private var index = 0
    private var position = 0
    private var isPremium = false

    override fun initBundle(bundle: Bundle) {
        keyWord = bundle.getString(Params.KEY_WORD) ?: ""
        index = bundle.getInt(Params.INDEX)
        searchModel = ViewModelProvider(this)[SearchModel::class.java]
        searchModel.keyword.value = keyWord
        searchModel.isNovel.value = index == 1

        isPremium = Shaft.sUserModel.user.isIs_premium
        searchModel.isPremium.value = isPremium
    }

    override fun initLayout(): Int {
        return R.layout.fragment_new_search
    }

    override fun initView() {
        val titles = arrayOf(
            getString(R.string.string_136),
            getString(R.string.string_138),
            getString(R.string.string_432),
        )
        baseBind.searchBox.setText(keyWord)
        baseBind.viewPager.adapter =
            object : FragmentPagerAdapter(supportFragmentManager, 0) {
                @NonNull
                override fun getItem(position: Int): Fragment {
                    if (allPages[position] == null) {
                        allPages[position] =
                            when (position) {
                                0 -> FragmentSearchIllust.newInstance()
                                1 -> FragmentSearchNovel.newInstance()
                                2 -> FragmentSearchUser.newInstance(keyWord)
                                else -> FragmentSearchIllust.newInstance()
                            }
                    }
                    return allPages[position]!!
                }

                override fun getCount(): Int {
                    return titles.size
                }

                @Nullable
                override fun getPageTitle(position: Int): CharSequence {
                    return titles[position]
                }
            }
        baseBind.viewPager.addOnPageChangeListener(
            object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int,
                ) {
                }

                override fun onPageSelected(position: Int) {
                    val currentFragmentFilter = fragmentFilter ?: return
                    this@SearchActivity.position = position
                    if (this@SearchActivity.position == 2) {
                        baseBind.drawerlayout.setTouchMode(ElasticDrawer.TOUCH_MODE_NONE)
                        if (baseBind.drawerlayout.isMenuVisible) {
                            baseBind.drawerlayout.closeMenu(true)
                        }
                    }
                    if (this@SearchActivity.position != 2) {
                        baseBind.drawerlayout.setTouchMode(ElasticDrawer.TOUCH_MODE_BEZEL)
                    }

                    val isNovel: MutableLiveData<Boolean> = searchModel.isNovel
                    if (isNovel.value != null) {
                        if (position == 0 && isNovel.value == true) {
                            isNovel.value = false
                        } else if (position == 1 && isNovel.value == false) {
                            isNovel.value = true
                        }
                    }
                }

                override fun onPageScrollStateChanged(state: Int) {
                }
            },
        )
        baseBind.viewPager.offscreenPageLimit = 2
        baseBind.tabLayout.setupWithViewPager(baseBind.viewPager)
        baseBind.drawerlayout.setTouchMode(ElasticDrawer.TOUCH_MODE_BEZEL)
        if (index != 0) {
            baseBind.viewPager.currentItem = index
        }

        if (Shaft.getMMKV().decodeBool(Params.MMKV_KEY_ISSHOWTIPS_SEARCHSORT, true)) {
            tipDialog(mContext)
            baseBind.drawerlayout.openMenu(true)
        }
    }

    override fun initData() {
        baseBind.toolbar.setNavigationOnClickListener {
            mActivity.finish()
        }
        baseBind.toolbar.inflateMenu(R.menu.illust_filter)
        baseBind.toolbar.setOnMenuItemClickListener(
            Toolbar.OnMenuItemClickListener { item: MenuItem ->
                if (item.itemId == R.id.action_filter) {
                    Common.hideKeyboard(mActivity)
                    if (position == 0 || position == 1) {
                        if (baseBind.drawerlayout.isMenuVisible) {
                            baseBind.drawerlayout.closeMenu(true)
                        } else {
                            baseBind.drawerlayout.openMenu(true)
                        }
                    } else {
                        Common.showToast(getString(R.string.string_435))
                    }
                    return@OnMenuItemClickListener true
                }
                false
            },
        )
        baseBind.searchBox.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    charSequence: CharSequence,
                    i: Int,
                    i1: Int,
                    i2: Int,
                ) {
                }

                override fun onTextChanged(
                    charSequence: CharSequence,
                    i: Int,
                    i1: Int,
                    i2: Int,
                ) {
                }

                override fun afterTextChanged(editable: Editable) {
                    searchModel.keyword.value = baseBind.searchBox.text.toString()
                }
            },
        )
        baseBind.searchBox.setOnEditorActionListener(
            TextView.OnEditorActionListener { _, _, _ ->
                val trimmedKeyword = baseBind.searchBox.text.toString().trim()
                if (TextUtils.isEmpty(trimmedKeyword) && TextUtils.isEmpty(searchModel.starSize.value)) {
                    Common.showToast(getString(R.string.string_139))
                    return@OnEditorActionListener false
                }

                if (URLUtil.isValidUrl(trimmedKeyword)) {
                    try {
                        PixivOperate.insertSearchHistory(trimmedKeyword, SearchTypeUtil.SEARCH_TYPE_DB_URL)
                        val intent = Intent(mContext, OutWakeActivity::class.java)
                        intent.data = Uri.parse(trimmedKeyword)
                        startActivity(intent)
                        mActivity.finish()
                    } catch (e: Exception) {
                        Common.showToast(e.toString())
                        e.printStackTrace()
                    }
                } else if (Common.isNumeric(trimmedKeyword)) {
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
                                mActivity.finish()
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
                                intent.putExtra(Params.USER_ID, Integer.valueOf(trimmedKeyword))
                                startActivity(intent)
                                mActivity.finish()
                            }
                        },
                    )
                } else {
                    searchModel.nowGo.value = "search_now"
                    Common.hideKeyboard(mActivity)
                }

                true
            },
        )

        fragmentFilter = FragmentFilter()
        val fragmentManager: FragmentManager = supportFragmentManager
        val currentFragmentFilter = fragmentFilter!!
        if (!currentFragmentFilter.isAdded) {
            fragmentManager
                .beginTransaction()
                .add(R.id.id_container_menu, currentFragmentFilter)
                .commitNowAllowingStateLoss()
        } else {
            fragmentManager
                .beginTransaction()
                .show(currentFragmentFilter)
                .commitNowAllowingStateLoss()
        }
    }

    private fun tipDialog(context: Context) {
        val qmuiDialog =
            QMUIDialog.MessageDialogBuilder(context)
                .setTitle(context.getString(R.string.string_433))
                .setMessage(context.getString(R.string.string_434))
                .setSkinManager(QMUISkinManager.defaultInstance(context))
                .addAction(
                    context.getString(R.string.string_190),
                    object : QMUIDialogAction.ActionListener {
                        override fun onClick(dialog: QMUIDialog, index: Int) {
                            Shaft.getMMKV().encode(Params.MMKV_KEY_ISSHOWTIPS_SEARCHSORT, false)
                            dialog.dismiss()
                        }
                    },
                )
                .create()
        qmuiDialog.show()
    }
}
