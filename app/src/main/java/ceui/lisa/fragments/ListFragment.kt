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
import ceui.lisa.refresh.layout.api.RefreshLayout
import ceui.lisa.refresh.layout.listener.OnLoadMoreListener
import ceui.lisa.refresh.layout.listener.OnRefreshListener
import jp.wasabeef.recyclerview.animators.BaseItemAnimator
import jp.wasabeef.recyclerview.animators.LandingAnimator

abstract class ListFragment<Layout : ViewBinding, Item> : BaseLazyFragment<Layout>() {

    protected lateinit var mRecyclerView: RecyclerView
    protected lateinit var mRefreshLayout: RefreshLayout
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
        mRefreshLayout.setDragRate(0.8f)
        mRefreshLayout.setHeaderTriggerRate(1.0f)
        mRefreshLayout.setHeaderMaxDragRate(1.5f)
        noData = view.findViewById(R.id.no_data)
        emptyRela = view.findViewById(R.id.no_data_rela)
        emptyRela.setOnClickListener {
            emptyRela.visibility = View.INVISIBLE
            mRefreshLayout.autoRefresh()
        }

        val baseRepo = mModel.getBaseRepo()!!
        mRefreshLayout.setEnableRefresh(baseRepo.enableRefresh())
        mRefreshLayout.setEnableLoadMore(baseRepo.hasNext())

        mRefreshLayout.setOnRefreshListener(OnRefreshListener {
            try {
                if (mRecyclerView.layoutManager is StaggeredGridLayoutManager &&
                    mRecyclerView.itemAnimator == null
                ) {
                    mRecyclerView.itemAnimator = animation()
                }
                clear()
                fresh()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
        mRefreshLayout.setOnLoadMoreListener(OnLoadMoreListener {
            try {
                if (mRecyclerView.layoutManager is StaggeredGridLayoutManager &&
                    mRecyclerView.itemAnimator != null
                ) {
                    mRecyclerView.itemAnimator = null
                }
                if (mModel.getBaseRepo()!!.hasNext()) {
                    loadMore()
                } else {
                    mRefreshLayout.finishLoadMore()
                    mRefreshLayout.setEnableLoadMore(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        allItems = mModel.getContent()
        mAdapter = adapter()
        mRecyclerView.adapter = mAdapter

        onAdapterPrepared()

        if (!isLazy() && autoRefresh() && !mModel.isLoaded()) {
            mRefreshLayout.autoRefresh()
        }
    }

    open fun refresh() {
        mRefreshLayout.autoRefresh()
    }

    override fun lazyData() {
        if (autoRefresh() && !mModel.isLoaded()) {
            mRefreshLayout.autoRefresh()
        }
    }

    open fun forceRefresh() {
        scrollToTop(object : FeedBack {
            override fun doSomething() {
                mRefreshLayout.autoRefresh()
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
    open fun autoRefresh(): Boolean = true

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
            mRefreshLayout.autoRefresh()
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
        mRefreshLayout.autoRefresh()
    }

    open fun getCount(): Int = if (this::allItems.isInitialized) allItems.size else 0

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
