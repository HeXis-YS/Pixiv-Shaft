package ceui.lisa.activities

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager
import ceui.lisa.R
import ceui.lisa.core.Container
import ceui.lisa.core.Mapper
import ceui.lisa.core.PageData
import ceui.lisa.databinding.ActivityViewPagerBinding
import ceui.lisa.fragments.FragmentIllust
import ceui.lisa.fragments.FragmentImageDetail
import ceui.lisa.fragments.FragmentSingleUgoira
import ceui.lisa.helper.DeduplicateArrayList
import ceui.lisa.http.NullCtrl
import ceui.lisa.http.Retro
import ceui.lisa.model.ListIllust
import ceui.lisa.models.IllustsBean
import ceui.lisa.utils.Common
import ceui.lisa.utils.Params
import ceui.lisa.utils.PixivOperate
import ceui.loxia.ObjectPool
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class VActivity : BaseActivity<ActivityViewPagerBinding>() {

    private var pageUUID = ""
    private var index = 0

    override fun initBundle(bundle: Bundle) {
        pageUUID = bundle.getString(Params.PAGE_UUID).orEmpty()
        index = bundle.getInt(Params.POSITION)
    }

    override fun initLayout(): Int = R.layout.activity_view_pager

    override fun initView() {
        val pageData = Container.get().getPage(pageUUID)
        if (pageData == null) {
            finish()
            return
        }

        baseBind.viewPager.adapter =
            object : FragmentStatePagerAdapter(supportFragmentManager, 0) {
                override fun getItem(position: Int): Fragment {
                    val illustsBean = pageData.getList()[position]
                    return when {
                        illustsBean.id == 0 || !illustsBean.isVisible -> {
                            FragmentImageDetail.newInstance(illustsBean.image_urls.maxImage)
                        }

                        illustsBean.isGif -> FragmentSingleUgoira.newInstance(illustsBean)
                        else -> {
                            val exist = ObjectPool.getIllust(illustsBean.id.toLong()).value
                            if (exist == null) {
                                ObjectPool.updateIllust(illustsBean)
                            }
                            FragmentIllust.newInstance(illustsBean.id)
                        }
                    }
                }

                override fun getCount(): Int = pageData.getList().size

                override fun saveState(): Parcelable? {
                    val state = super.saveState()
                    val bundle = state as? Bundle
                    bundle?.putParcelableArray("states", null)
                    return bundle ?: state
                }
            }
        baseBind.viewPager.offscreenPageLimit = 2

        val listener = object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int,
            ) = Unit

            override fun onPageSelected(position: Int) {
                Common.showLog("Container onPageSelected $position")
                if (Common.isEmpty(pageData.getList())) {
                    return
                }
                if (position >= pageData.getList().size) {
                    return
                }

                if (Shaft.sSettings.isSaveViewHistory()) {
                    PixivOperate.insertIllustViewHistory(pageData.getList()[position])
                }

                if (position == pageData.getList().size - 1 || position == pageData.getList().size - 2) {
                    val nextUrl = pageData.getNextUrl()
                    if (!TextUtils.isEmpty(nextUrl)) {
                        if (!Container.get().isNetworking()) {
                            Common.showLog("Container 去请求下一页 $nextUrl")
                            Retro.getAppApi().getNextIllust(Shaft.sUserModel.access_token, nextUrl!!)
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(object : NullCtrl<ListIllust>() {
                                    override fun success(listIllust: ListIllust) {
                                        val mapper = Mapper<ListIllust>()
                                        val mapped = mapper.apply(listIllust) as ListIllust
                                        Common.showLog("Container 下一页请求成功 ")
                                        val intent = Intent(Params.FRAGMENT_ADD_DATA)
                                        intent.putExtra(Params.PAGE_UUID, pageUUID)
                                        intent.putExtra(Params.CONTENT, mapped)
                                        LocalBroadcastManager.getInstance(Shaft.getContext())
                                            .sendBroadcast(intent)

                                        DeduplicateArrayList.addAllWithNoRepeat(
                                            pageData.getList() as MutableCollection<IllustsBean>,
                                            mapped.list,
                                        )
                                        pageData.setNextUrl(mapped.nextUrl)
                                        baseBind.viewPager.adapter?.notifyDataSetChanged()
                                    }

                                    override fun must() {
                                        super.must()
                                        Container.get().setNetworking(false)
                                    }

                                    override fun subscribe(d: Disposable) {
                                        super.subscribe(d)
                                        Container.get().setNetworking(true)
                                    }
                                })
                        } else {
                            Common.showLog("Container 不去请求下一页 00")
                        }
                    } else {
                        Common.showLog("Container 不去请求下一页 11")
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) = Unit
        }
        baseBind.viewPager.addOnPageChangeListener(listener)

        if (index < pageData.getList().size) {
            baseBind.viewPager.currentItem = index
        }
        if (index == 0) {
            baseBind.viewPager.post {
                listener.onPageSelected(baseBind.viewPager.currentItem)
            }
        }
    }

    override fun initData() = Unit

    override fun onDestroy() {
        PixivOperate.clearBack()
        super.onDestroy()
    }

    override fun onPause() {
        val intent = Intent(Params.FRAGMENT_SCROLL_TO_POSITION)
        intent.putExtra(Params.INDEX, baseBind.viewPager.currentItem)
        intent.putExtra(Params.PAGE_UUID, pageUUID)
        LocalBroadcastManager.getInstance(Shaft.getContext()).sendBroadcast(intent)
        super.onPause()
    }

    override fun hideStatusBar(): Boolean = true
}
