package ceui.lisa.fragments

import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewbinding.ViewBinding
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.core.BaseRepo
import ceui.lisa.helper.StaggeredManager
import ceui.lisa.interfaces.FeedBack
import ceui.lisa.model.ListTrendingtag
import ceui.lisa.models.IllustsBean
import ceui.lisa.utils.DensityUtil
import ceui.lisa.view.LinearItemDecoration
import ceui.lisa.view.SpacesItemDecoration
import ceui.lisa.viewmodel.BaseModel
import ceui.loxia.ObjectPool
import jp.wasabeef.recyclerview.animators.BaseItemAnimator
import jp.wasabeef.recyclerview.animators.LandingAnimator

abstract class ListFragment<Layout : ViewBinding, Item> : BaseLazyFragment<Layout>() {

    protected lateinit var mRecyclerView: RecyclerView
    protected lateinit var mRefreshLayout: SwipeRefreshLayout
    protected lateinit var noData: ImageView
    protected lateinit var emptyRela: RelativeLayout
    lateinit var mAdapter: BaseAdapter<*, out ViewBinding>
        protected set
    lateinit var allItems: MutableList<Item>
        protected set
    lateinit var mModel: BaseModel<Item>
        protected set
    var mToolbar: Toolbar? = null
        protected set
    private var refreshEnabled = true
    private var loadMoreEnabled = true
    private var loadingMore = false
    private var loadMoreScrollListener: RecyclerView.OnScrollListener? = null

    override fun initLayout() {
        mLayoutID = R.layout.fragment_base_list
    }

    abstract fun adapter(): BaseAdapter<*, out ViewBinding>

    abstract fun repository(): BaseRepo

    open fun onAdapterPrepared() {
    }

    @Suppress("UNCHECKED_CAST")
    override fun initModel() {
        mModel = ViewModelProvider(this).get(modelClass()) as BaseModel<Item>
        allItems = mModel.getContent()
        if (mModel.getBaseRepo() == null) {
            mModel.setBaseRepo(repository())
        }
    }

    open fun modelClass(): Class<out BaseModel<*>> = BaseModel::class.java

    override fun initView() {
        val view = rootView ?: return

        mToolbar = view.findViewById(R.id.toolbar)
        mToolbar?.let { initToolbar(it) }

        mRecyclerView = view.findViewById(R.id.recyclerView)
        initRecyclerView()
        mRecyclerView.itemAnimator = animation()

        mRefreshLayout = view.findViewById(R.id.refreshLayout)
        noData = view.findViewById(R.id.no_data)
        emptyRela = view.findViewById(R.id.no_data_rela)
        emptyRela.setOnClickListener {
            emptyRela.visibility = View.INVISIBLE
            autoRefresh()
        }

        val baseRepo = mModel.getBaseRepo()!!
        setEnableRefresh(baseRepo.enableRefresh())
        setEnableLoadMore(baseRepo.hasNext())

        mRefreshLayout.setOnRefreshListener {
            performRefresh()
        }
        if (loadMoreScrollListener == null) {
            loadMoreScrollListener = object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy <= 0 || !loadMoreEnabled || loadingMore) {
                        return
                    }
                    if (!recyclerView.canScrollVertically(1) && mModel.getBaseRepo()!!.hasNext()) {
                        loadingMore = true
                        try {
                            performLoadMore()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            finishLoadMore(false)
                        }
                    }
                }
            }
            mRecyclerView.addOnScrollListener(loadMoreScrollListener!!)
        }

        allItems = mModel.getContent()
        mAdapter = adapter()
        mRecyclerView.adapter = mAdapter

        onAdapterPrepared()

        if (!isLazy() && shouldAutoRefresh() && !mModel.isLoaded()) {
            autoRefresh()
        }
    }

    open fun refresh() {
        autoRefresh()
    }

    override fun lazyData() {
        if (shouldAutoRefresh() && !mModel.isLoaded()) {
            autoRefresh()
        }
    }

    open fun forceRefresh() {
        scrollToTop(object : FeedBack {
            override fun doSomething() {
                autoRefresh()
            }
        })
    }

    open fun scrollToTop(feedBack: FeedBack?) {
        try {
            mRecyclerView.smoothScrollToPosition(0)
            mRecyclerView.postDelayed(
                {
                    feedBack?.doSomething()
                },
                animateDuration,
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    open fun scrollToTop() {
        scrollToTop(null)
    }

    abstract fun fresh()

    abstract fun loadMore()

    /**
     * 指定是否显示Toolbar
     *
     * @return default true
     */
    open fun showToolbar(): Boolean = true

    /**
     * 指定Toolbar title
     *
     * @return title
     */
    open fun getToolbarTitle(): String = ""

    open fun initRecyclerView() {
        verticalRecyclerView()
    }

    fun verticalRecyclerView() {
        mRecyclerView.layoutManager = LinearLayoutManager(mContext)
        mRecyclerView.addItemDecoration(LinearItemDecoration(DensityUtil.dp2px(12.0f)))
    }

    protected fun staggerRecyclerView() {
        val manager = StaggeredManager(Shaft.sSettings.lineCount, StaggeredGridLayoutManager.VERTICAL)
        mRecyclerView.layoutManager = manager
        mRecyclerView.addItemDecoration(SpacesItemDecoration(DensityUtil.dp2px(8.0f)))
    }

    /**
     * 决定刚进入页面是否直接刷新，一般都是直接刷新，但是FragmentHotTag，不要直接刷新
     *
     * @return default true
     */
    open fun shouldAutoRefresh(): Boolean = true

    open fun initToolbar(toolbar: Toolbar) {
        if (showToolbar()) {
            toolbar.visibility = View.VISIBLE
            val title = toolbar.findViewById<TextView>(R.id.toolbar_title)
            if (title != null) {
                title.text = getToolbarTitle()
                title.movementMethod = ScrollingMovementMethod.getInstance()
                title.setHorizontallyScrolling(true)
            } else {
                toolbar.title = getToolbarTitle()
            }
            toolbar.setNavigationOnClickListener { finish() }
        } else {
            toolbar.visibility = View.GONE
        }
    }

    open fun beforeFirstLoad(items: List<Item>) {
    }

    open fun beforeNextLoad(items: List<Item>) {
    }

    open fun onFirstLoaded(items: List<Item>) {
        items.forEach { item ->
            when (item) {
                is IllustsBean -> ObjectPool.updateIllust(item)
                is ListTrendingtag.TrendTagsBean -> item.illust?.let { ObjectPool.updateIllust(it) }
            }
        }
    }

    open fun onNextLoaded(items: List<Item>) {
        items.forEach { item ->
            if (item is IllustsBean) {
                ObjectPool.updateIllust(item)
            }
        }
    }

    /**
     * mAdapter is not null
     * Clear all items on the page
     */
    open fun clear() {
        if (this::mAdapter.isInitialized) {
            mAdapter.clear()
        }
    }

    open fun clearAndRefresh() {
        clear()
        if (this::mRefreshLayout.isInitialized) {
            autoRefresh()
        }
    }

    open fun animation(): BaseItemAnimator? {
        val baseItemAnimator = LandingAnimator()
        baseItemAnimator.addDuration = animateDuration
        baseItemAnimator.removeDuration = animateDuration
        baseItemAnimator.moveDuration = animateDuration
        baseItemAnimator.changeDuration = animateDuration
        return baseItemAnimator
    }

    open fun getStartSize(): Int = allItems.size + mAdapter.headerSize()

    open fun nowRefresh() {
        mRecyclerView.smoothScrollToPosition(0)
        autoRefresh()
    }

    open fun getCount(): Int = if (this::allItems.isInitialized) allItems.size else 0

    open fun setEnableRefresh(enable: Boolean) {
        refreshEnabled = enable
        mRefreshLayout.isEnabled = enable
        if (!enable) {
            mRefreshLayout.isRefreshing = false
        }
    }

    open fun setEnableLoadMore(enable: Boolean) {
        loadMoreEnabled = enable
        if (!enable) {
            loadingMore = false
        }
    }

    open fun finishRefresh(success: Boolean = true) {
        mRefreshLayout.isRefreshing = false
    }

    open fun finishLoadMore(success: Boolean = true) {
        loadingMore = false
    }

    open fun autoRefresh() {
        if (!refreshEnabled) {
            finishRefresh(false)
            return
        }
        mRefreshLayout.post {
            mRefreshLayout.isRefreshing = true
            performRefresh()
        }
    }

    private fun performRefresh() {
        try {
            if (mRecyclerView.layoutManager is StaggeredGridLayoutManager &&
                mRecyclerView.itemAnimator == null
            ) {
                mRecyclerView.itemAnimator = animation()
            }
            if (!refreshEnabled) {
                finishRefresh(false)
                return
            }
            clear()
            fresh()
        } catch (e: Exception) {
            e.printStackTrace()
            finishRefresh(false)
        }
    }

    private fun performLoadMore() {
        try {
            if (mRecyclerView.layoutManager is StaggeredGridLayoutManager &&
                mRecyclerView.itemAnimator != null
            ) {
                mRecyclerView.itemAnimator = null
            }
            if (mModel.getBaseRepo()!!.hasNext()) {
                loadMore()
            } else {
                finishLoadMore(false)
                setEnableLoadMore(false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            finishLoadMore(false)
        }
    }

    @get:JvmName("getCountProperty")
    val count: Int
        get() = getCount()

    companion object {
        @JvmField
        val animateDuration = 400L

        @JvmField
        val PAGE_SIZE = 20
    }
}
