package ceui.lisa.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.annotation.CallSuper
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewbinding.ViewBinding
import ceui.lisa.R
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.EventAdapter
import ceui.lisa.adapters.IAdapter
import ceui.lisa.adapters.NAdapter
import ceui.lisa.adapters.SimpleUserAdapter
import ceui.lisa.adapters.UAdapter
import ceui.lisa.adapters.UserHAdapter
import ceui.lisa.core.Container
import ceui.lisa.core.PageData
import ceui.lisa.core.RemoteRepo
import ceui.lisa.http.NullCtrl
import ceui.lisa.interfaces.ListShow
import ceui.lisa.model.ListIllust
import ceui.lisa.models.Starable
import ceui.lisa.notification.BaseReceiver
import ceui.lisa.notification.CallBackReceiver
import ceui.lisa.notification.CommonReceiver
import ceui.lisa.utils.Common
import ceui.lisa.utils.Params

/**
 * 联网获取xx列表，
 *
 * @param Layout 这个列表的LayoutBinding.
 * @param Response Type: {@link ListIllust}这次请求的Response.
 * @param Item 这个列表的单个Item实体类.
 */
abstract class NetListFragment<Layout : ViewBinding, Response : ListShow<Item>, Item> :
    ListFragment<Layout, Item>() {

    lateinit var mRemoteRepo: RemoteRepo<Response>
        protected set
    protected var mResponse: Response? = null
    protected var mReceiver: BroadcastReceiver? = null
    protected var dataReceiver: BroadcastReceiver? = null
    protected var scrollReceiver: BroadcastReceiver? = null
    protected var isLoading = false

    /**
     * Fresh the page.
     */
    override fun fresh() {
        if (!mRemoteRepo.localData()) {
            emptyRela.visibility = View.INVISIBLE
            if (isLoading) return
            isLoading = true
            mRemoteRepo.getFirstData(object : NullCtrl<Response>() {
                override fun success(response: Response) {
                    Common.showLog("trace 000")
                    if (!isAdded) {
                        return
                    }
                    Common.showLog("trace 111")
                    mResponse = response
                    tryCatchResponse(mResponse!!)
                    val responseList = mResponse!!.list
                    if (!Common.isEmpty(responseList)) {
                        Common.showLog("trace 222 " + mAdapter.itemCount)
                        beforeFirstLoad(responseList)
                        val beforeLoadSize = getStartSize()
                        mModel.load(responseList, true)
                        if (mRemoteRepo.hasEffectiveUserFollowStatus()) {
                            mModel.tidyAppViewModel()
                        }
                        allItems = mModel.getContent()
                        val afterLoadSize = getStartSize()
                        onFirstLoaded(responseList)
                        mRecyclerView.visibility = View.VISIBLE
                        emptyRela.visibility = View.INVISIBLE
                        mAdapter.notifyItemRangeInserted(beforeLoadSize, afterLoadSize - beforeLoadSize)
                        Common.showLog(
                            "trace 777 " + mAdapter.itemCount +
                                " allItems.size():" + allItems.size +
                                " modelSize:" + mModel.getContent().size,
                        )
                    } else {
                        Common.showLog("trace 333")
                        mRecyclerView.visibility = View.INVISIBLE
                        emptyRela.visibility = View.VISIBLE
                    }
                    Common.showLog("trace 444")
                    mRemoteRepo.setNextUrl(mResponse!!.nextUrl)
                    mAdapter.setNextUrl(mResponse!!.nextUrl)
                    mRefreshLayout.setEnableLoadMore(!TextUtils.isEmpty(mResponse!!.nextUrl))
                }

                override fun must(isSuccess: Boolean) {
                    mRefreshLayout.finishRefresh(isSuccess)
                    isLoading = false
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    mRecyclerView.visibility = View.INVISIBLE
                    emptyRela.visibility = View.VISIBLE
                }
            })
        } else {
            showDataBase()
        }
    }

    private fun tryCatchResponse(response: Response) {
        try {
            onResponse(response)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun loadMore() {
        if (!TextUtils.isEmpty(mRemoteRepo.getNextUrl())) {
            if (isLoading) return
            isLoading = true
            mRemoteRepo.getNextData(object : NullCtrl<Response>() {
                override fun success(response: Response) {
                    if (!isAdded) {
                        return
                    }
                    mResponse = response
                    val responseList = mResponse!!.list
                    if (!Common.isEmpty(responseList)) {
                        beforeNextLoad(responseList)
                        val beforeLoadSize = getStartSize()
                        mModel.load(responseList, false)
                        if (mRemoteRepo.hasEffectiveUserFollowStatus()) {
                            mModel.tidyAppViewModel(responseList)
                        }
                        allItems = mModel.getContent()
                        val afterLoadSize = getStartSize()
                        onNextLoaded(responseList)
                        mAdapter.notifyItemRangeInserted(beforeLoadSize, afterLoadSize - beforeLoadSize)
                    }
                    mRemoteRepo.setNextUrl(mResponse!!.nextUrl)
                    mAdapter.setNextUrl(mResponse!!.nextUrl)
                    mRefreshLayout.setEnableLoadMore(!TextUtils.isEmpty(mResponse!!.nextUrl))
                }

                override fun must(isSuccess: Boolean) {
                    mRefreshLayout.finishLoadMore(isSuccess)
                    isLoading = false
                }
            })
        } else {
            if (mRemoteRepo.showNoDataHint()) {
                Common.showToast(getString(R.string.string_224))
            }
            mRefreshLayout.finishLoadMore()
        }
    }

    override fun initData() {
        @Suppress("UNCHECKED_CAST")
        run {
            mRemoteRepo = mModel.getBaseRepo() as RemoteRepo<Response>
        }
        super.initData()
    }

    /**
     * FragmentR页面，调试过程中不需要每次都刷新，就调用这个方法来加载数据。只是为了方便测试
     */
    open fun showDataBase() {
    }

    open fun onResponse(response: Response) {
    }

    @CallSuper
    override fun onAdapterPrepared() {
        mAdapter.setUuid(uuid)
        if (mAdapter is IAdapter || mAdapter is EventAdapter) {
            val intentFilter = IntentFilter()
            @Suppress("UNCHECKED_CAST")
            val adapter = mAdapter as BaseAdapter<Starable, *>
            mReceiver = CommonReceiver(adapter)
            intentFilter.addAction(Params.LIKED_ILLUST)
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceiver!!, intentFilter)
            if (mAdapter is IAdapter) {
                addPageLoadReceiver()
                addPageScrollReceiver()
            }
        } else if (mAdapter is UAdapter || mAdapter is UserHAdapter || mAdapter is SimpleUserAdapter) {
            val intentFilter = IntentFilter()
            @Suppress("UNCHECKED_CAST")
            val adapter = mAdapter as BaseAdapter<Starable, *>
            mReceiver = CommonReceiver(adapter)
            intentFilter.addAction(Params.LIKED_USER)
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceiver!!, intentFilter)
        } else if (mAdapter is NAdapter) {
            val intentFilter = IntentFilter()
            @Suppress("UNCHECKED_CAST")
            val adapter = mAdapter as BaseAdapter<Starable, *>
            mReceiver = CommonReceiver(adapter)
            intentFilter.addAction(Params.LIKED_NOVEL)
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceiver!!, intentFilter)
        }

        if (mAdapter is IAdapter) {
            mAdapter.onPreload = Runnable { loadMore() }
        }
    }

    override fun onDestroy() {
        if (mReceiver != null) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiver!!)
        }
        if (dataReceiver != null) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(dataReceiver!!)
        }
        if (scrollReceiver != null) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(scrollReceiver!!)
        }
        super.onDestroy()
    }

    private fun addPageLoadReceiver() {
        val intentFilter = IntentFilter()
        dataReceiver = CallBackReceiver(BaseReceiver.CallBack { _: Context, intent: Intent ->
            val bundle: Bundle? = intent.extras
            if (bundle != null) {
                val intentUUID = intent.getStringExtra(Params.PAGE_UUID)
                val pageData: PageData? = Container.get().getPage(intentUUID)
                if (pageData != null && TextUtils.equals(pageData.getUUID(), uuid)) {
                    val listIllust = bundle.getSerializable(Params.CONTENT) as ListIllust?
                    if (listIllust != null && !Common.isEmpty(listIllust.list)) {
                        if (!isAdded) {
                            return@CallBack
                        }
                        @Suppress("UNCHECKED_CAST")
                        run {
                            mResponse = listIllust as Response
                        }
                        val responseList = mResponse!!.list
                        if (!Common.isEmpty(responseList)) {
                            beforeNextLoad(responseList)
                            val beforeLoadSize = getStartSize()
                            mModel.load(responseList, false)
                            if (mRemoteRepo.hasEffectiveUserFollowStatus()) {
                                mModel.tidyAppViewModel(responseList)
                            }
                            allItems = mModel.getContent()
                            val afterLoadSize = getStartSize()
                            onNextLoaded(responseList)
                            mAdapter.notifyItemRangeInserted(beforeLoadSize, afterLoadSize - beforeLoadSize)
                        }
                        mRemoteRepo.setNextUrl(mResponse!!.nextUrl)
                        mAdapter.setNextUrl(mResponse!!.nextUrl)
                        mRefreshLayout.setEnableLoadMore(!TextUtils.isEmpty(mResponse!!.nextUrl))
                    }
                }
            }
        })
        intentFilter.addAction(Params.FRAGMENT_ADD_DATA)
        LocalBroadcastManager.getInstance(mContext).registerReceiver(dataReceiver!!, intentFilter)
    }

    private fun addPageScrollReceiver() {
        val intentFilter = IntentFilter()
        scrollReceiver = CallBackReceiver(BaseReceiver.CallBack { _: Context, intent: Intent ->
            val bundle: Bundle? = intent.extras
            if (bundle != null) {
                val index = bundle.getInt(Params.INDEX)
                val pageUUID = bundle.getString(Params.PAGE_UUID)
                if (TextUtils.equals(pageUUID, uuid)) {
                    try {
                        mRecyclerView.postDelayed(
                            {
                                mRecyclerView.smoothScrollToPosition(index + mAdapter.headerSize())
                            },
                            200L,
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
        intentFilter.addAction(Params.FRAGMENT_SCROLL_TO_POSITION)
        LocalBroadcastManager.getInstance(mContext).registerReceiver(scrollReceiver!!, intentFilter)
    }
}
