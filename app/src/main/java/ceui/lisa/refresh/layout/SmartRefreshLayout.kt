package ceui.lisa.refresh.layout

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ceui.lisa.refresh.layout.api.RefreshLayout

class SmartRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : SwipeRefreshLayout(context, attrs), RefreshLayout {

    private var refreshListener: ceui.lisa.refresh.layout.listener.OnRefreshListener? = null
    private var loadMoreListener: ceui.lisa.refresh.layout.listener.OnLoadMoreListener? = null
    private var enableRefresh = true
    private var enableLoadMore = true
    private var loadingMore = false
    private var lastCanScrollDown = true
    private var currentScrollable: View? = null

    init {
        super.setOnRefreshListener {
            if (enableRefresh) {
                refreshListener?.onRefresh(this)
            } else {
                isRefreshing = false
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        bindScrollableTarget()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        bindScrollableTarget()
    }

    private fun bindScrollableTarget() {
        currentScrollable?.let { detachScrollableTarget(it) }
        currentScrollable = findScrollableTarget(this)
        currentScrollable?.let { attachScrollableTarget(it) }
    }

    private fun detachScrollableTarget(view: View) {
        if (view is RecyclerView) {
            view.clearOnScrollListeners()
        } else if (view is NestedScrollView) {
            view.setOnScrollChangeListener(null as NestedScrollView.OnScrollChangeListener?)
        }
    }

    private fun attachScrollableTarget(view: View) {
        when (view) {
            is RecyclerView -> {
                view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        maybeTriggerLoadMore(recyclerView.canScrollVertically(1))
                    }
                })
            }

            is NestedScrollView -> {
                view.setOnScrollChangeListener { v, _, scrollY, _, oldScrollY ->
                    if (scrollY != oldScrollY) {
                        maybeTriggerLoadMore(v.canScrollVertically(1))
                    }
                }
            }
        }
    }

    private fun findScrollableTarget(root: View): View? {
        if (root is RecyclerView || root is NestedScrollView) return root
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                findScrollableTarget(root.getChildAt(i))?.let { return it }
            }
        }
        return null
    }

    private fun maybeTriggerLoadMore(canScrollDown: Boolean) {
        if (!enableLoadMore || loadingMore) {
            lastCanScrollDown = canScrollDown
            return
        }
        if (lastCanScrollDown && !canScrollDown) {
            loadingMore = true
            loadMoreListener?.onLoadMore(this)
        }
        lastCanScrollDown = canScrollDown
    }

    override fun setEnableRefresh(enable: Boolean) {
        enableRefresh = enable
        isEnabled = enable
        if (!enable) {
            isRefreshing = false
        }
    }

    override fun setEnableLoadMore(enable: Boolean) {
        enableLoadMore = enable
        if (!enable) {
            loadingMore = false
        }
    }

    override fun setDragRate(rate: Float) = Unit

    override fun setHeaderTriggerRate(rate: Float) = Unit

    override fun setHeaderMaxDragRate(rate: Float) = Unit

    override fun setOnRefreshListener(listener: ceui.lisa.refresh.layout.listener.OnRefreshListener?) {
        refreshListener = listener
    }

    override fun setOnLoadMoreListener(listener: ceui.lisa.refresh.layout.listener.OnLoadMoreListener?) {
        loadMoreListener = listener
    }

    override fun autoRefresh() {
        if (!enableRefresh) return
        post {
            isRefreshing = true
            refreshListener?.onRefresh(this)
        }
    }

    override fun finishRefresh(success: Boolean) {
        isRefreshing = false
    }

    override fun finishLoadMore(success: Boolean) {
        loadingMore = false
    }

    override fun setPrimaryColors(vararg colors: Int) = Unit
}
