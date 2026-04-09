package ceui.lisa.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import ceui.lisa.interfaces.OnItemClickListener
import ceui.lisa.interfaces.OnItemLongClickListener
import ceui.lisa.models.Starable
import ceui.lisa.utils.Common

abstract class BaseAdapter<Item, BindView : ViewDataBinding>(
    targetList: List<Item>?,
    protected var mContext: Context,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    @Suppress("UNCHECKED_CAST")
    protected var allItems: MutableList<Item> =
        (targetList as? MutableList<Item>) ?: ArrayList(targetList ?: emptyList())
    protected var mLayoutID = -1
    protected var mOnItemClickListener: OnItemClickListener? = null
    protected var mOnItemLongClickListener: OnItemLongClickListener? = null

    /**
     * The nextUrl variable is typically used in APIs to indicate the URL for the next page of
     * results in a paginated response. Pagination is a common technique used to handle large
     * datasets by splitting them into smaller, more manageable chunks.
     */
    protected var adapterNextUrl: String? = null
    protected var adapterUuid: String? = null

    @JvmField
    var onPreload: Runnable? = null

    @JvmField
    var preloadItemCount = 5

    private var scrollState = RecyclerView.SCROLL_STATE_IDLE

    init {
        Common.showLog(javaClass.simpleName + " newInstance")
        initLayout()
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        checkPreload(position)
        when (getItemViewType(position)) {
            ITEM_NORMAL -> {
                val index = position - headerSize()
                tryCatchBindData(allItems[index], holder as ViewHolder<BindView>, index)
            }
            ITEM_HEAD -> {
            }
        }
    }

    override fun getItemCount(): Int = allItems.size + headerSize()

    abstract fun initLayout()

    abstract fun bindData(target: Item, bindView: ViewHolder<BindView>, position: Int)

    private fun tryCatchBindData(target: Item, bindView: ViewHolder<BindView>, position: Int) {
        try {
            bindData(target, bindView, position)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_NORMAL) {
            getNormalItem(parent)
        } else {
            requireNotNull(getHeader(parent))
        }
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener): BaseAdapter<Item, BindView> {
        mOnItemClickListener = onItemClickListener
        return this
    }

    fun setOnItemLongClickListener(onItemLongClickListener: OnItemLongClickListener): BaseAdapter<Item, BindView> {
        mOnItemLongClickListener = onItemLongClickListener
        return this
    }

    /**
     * Clear all the object
     */
    fun clear() {
        val size = allItems.size
        allItems.clear()
        notifyItemRangeRemoved(0, size)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < headerSize()) ITEM_HEAD else ITEM_NORMAL
    }

    open fun headerSize(): Int = 0

    open fun getHeader(parent: ViewGroup): ViewHolder<out ViewDataBinding>? = null

    open fun getNormalItem(parent: ViewGroup): ViewHolder<BindView> {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(mContext),
                mLayoutID,
                parent,
                false,
            ),
        )
    }

    fun getItemAt(index: Int): Item? = allItems.getOrNull(index)

    open fun setLiked(id: Int, isLike: Boolean) {
        if (id == 0) {
            return
        }
        if (allItems.isEmpty()) {
            return
        }
        for (i in allItems.indices) {
            val item = allItems[i]
            if (item is Starable && item.getItemID() == id) {
                item.setItemStared(isLike)
                if (headerSize() != 0) {
                    notifyItemChanged(i + headerSize())
                } else {
                    notifyItemChanged(i)
                }
            }
        }
    }

    fun setNextUrl(nextUrl: String?) {
        adapterNextUrl = nextUrl
    }

    /**
     * 赋值uuid
     *
     * @param uuid 宿主fragment 的 uuid
     */
    fun setUuid(uuid: String?) {
        adapterUuid = uuid
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                scrollState = newState
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }

    private fun checkPreload(position: Int) {
        if (onPreload != null &&
            position == maxOf(itemCount - 1 - preloadItemCount, 0) &&
            scrollState != RecyclerView.SCROLL_STATE_IDLE
        ) {
            onPreload?.run()
        }
    }

    companion object {
        const val ITEM_HEAD = 1023
        const val ITEM_NORMAL = 1024
    }
}
