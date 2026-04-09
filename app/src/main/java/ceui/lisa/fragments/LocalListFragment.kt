package ceui.lisa.fragments

import android.view.View
import androidx.databinding.ViewDataBinding
import ceui.lisa.R
import ceui.lisa.core.LocalRepo
import ceui.lisa.core.RxRun
import ceui.lisa.core.RxRunnable
import ceui.lisa.core.TryCatchObserverImpl
import ceui.lisa.utils.Common
import java.util.ArrayList

abstract class LocalListFragment<Layout : ViewDataBinding, Item> : ListFragment<Layout, Item>() {
    protected lateinit var mLocalRepo: LocalRepo<List<Item>>

    open fun shouldLoadLocalDataAsync(): Boolean = false

    override fun fresh() {
        emptyRela.visibility = View.INVISIBLE
        if (shouldLoadLocalDataAsync()) {
            RxRun.runOn(object : RxRunnable<List<Item>>() {
                override fun execute(): List<Item> {
                    val firstList = mLocalRepo.first()
                    return firstList ?: ArrayList()
                }
            }, object : TryCatchObserverImpl<List<Item>>() {
                override fun next(items: List<Item>) {
                    handleFirstList(items)
                }

                override fun error(e: Throwable) {
                    mRefreshLayout.finishRefresh(false)
                }
            })
            return
        }
        val firstList = mLocalRepo.first()
        handleFirstList(firstList)
    }

    private fun handleFirstList(firstList: List<Item>?) {
        val items = firstList ?: emptyList()
        if (!Common.isEmpty(items)) {
            mModel.load(items, true)
            mModel.tidyAppViewModel()
            allItems = mModel.getContent()
            onFirstLoaded(items)
            mRecyclerView.visibility = View.VISIBLE
            emptyRela.visibility = View.INVISIBLE
            mAdapter.notifyItemRangeInserted(getStartSize(), items.size)
        } else {
            mRecyclerView.visibility = View.INVISIBLE
            emptyRela.visibility = View.VISIBLE
        }
        mRefreshLayout.finishRefresh(true)
    }

    override fun loadMore() {
        if (shouldLoadLocalDataAsync()) {
            RxRun.runOn(object : RxRunnable<List<Item>>() {
                override fun execute(): List<Item> {
                    val nextList = mLocalRepo.next()
                    return nextList ?: ArrayList()
                }
            }, object : TryCatchObserverImpl<List<Item>>() {
                override fun next(items: List<Item>) {
                    handleNextList(items)
                }

                override fun error(e: Throwable) {
                    mRefreshLayout.finishLoadMore(false)
                }
            })
            return
        }
        val nextList = mLocalRepo.next()
        handleNextList(nextList)
    }

    private fun handleNextList(nextList: List<Item>?) {
        val items = nextList ?: emptyList()
        if (mLocalRepo.hasNext() && !Common.isEmpty(items)) {
            mModel.load(items, false)
            mModel.tidyAppViewModel(items)
            allItems = mModel.getContent()
            onNextLoaded(items)
            mAdapter.notifyItemRangeInserted(getStartSize(), items.size)
        } else {
            if (mLocalRepo.showNoDataHint()) {
                Common.showToast(getString(R.string.string_224))
            }
        }
        mRefreshLayout.finishLoadMore(true)
    }

    override fun initData() {
        @Suppress("UNCHECKED_CAST")
        mLocalRepo = mModel.getBaseRepo() as LocalRepo<List<Item>>
        super.initData()
    }
}
