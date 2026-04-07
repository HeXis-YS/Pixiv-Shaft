package ceui.lisa.fragments;

import android.view.View;

import androidx.databinding.ViewDataBinding;

import java.util.ArrayList;
import java.util.List;

import ceui.lisa.R;
import ceui.lisa.core.RxRun;
import ceui.lisa.core.RxRunnable;
import ceui.lisa.core.TryCatchObserverImpl;
import ceui.lisa.core.LocalRepo;
import ceui.lisa.utils.Common;

public abstract class LocalListFragment<Layout extends ViewDataBinding, Item>
        extends ListFragment<Layout, Item> {

    protected LocalRepo<List<Item>> mLocalRepo;

    public boolean shouldLoadLocalDataAsync() {
        return false;
    }

    @Override
    public void fresh() {
        emptyRela.setVisibility(View.INVISIBLE);
        if (shouldLoadLocalDataAsync()) {
            RxRun.runOn(new RxRunnable<List<Item>>() {
                @Override
                public List<Item> execute() {
                    List<Item> firstList = mLocalRepo.first();
                    return firstList == null ? new ArrayList<>() : firstList;
                }
            }, new TryCatchObserverImpl<List<Item>>() {
                @Override
                public void next(List<Item> items) {
                    handleFirstList(items);
                }

                @Override
                public void error(Throwable e) {
                    mRefreshLayout.finishRefresh(false);
                }
            });
            return;
        }
        List<Item> firstList = mLocalRepo.first();
        handleFirstList(firstList);
    }

    private void handleFirstList(List<Item> firstList) {
        if (!Common.isEmpty(firstList)) {
            if (mModel != null) {
                mModel.load(firstList, true);
                mModel.tidyAppViewModel();
                allItems = mModel.getContent();
            }
            onFirstLoaded(firstList);
            mRecyclerView.setVisibility(View.VISIBLE);
            emptyRela.setVisibility(View.INVISIBLE);
            mAdapter.notifyItemRangeInserted(getStartSize(), firstList.size());
        } else {
            mRecyclerView.setVisibility(View.INVISIBLE);
            emptyRela.setVisibility(View.VISIBLE);
        }
        mRefreshLayout.finishRefresh(true);
    }

    @Override
    public void loadMore() {
        if (shouldLoadLocalDataAsync()) {
            RxRun.runOn(new RxRunnable<List<Item>>() {
                @Override
                public List<Item> execute() {
                    List<Item> nextList = mLocalRepo.next();
                    return nextList == null ? new ArrayList<>() : nextList;
                }
            }, new TryCatchObserverImpl<List<Item>>() {
                @Override
                public void next(List<Item> items) {
                    handleNextList(items);
                }

                @Override
                public void error(Throwable e) {
                    mRefreshLayout.finishLoadMore(false);
                }
            });
            return;
        }
        List<Item> nextList = mLocalRepo.next();
        handleNextList(nextList);
    }

    private void handleNextList(List<Item> nextList) {
        if (mLocalRepo.hasNext() && !Common.isEmpty(nextList)) {
            if (mModel != null) {
                mModel.load(nextList, false);
                mModel.tidyAppViewModel(nextList);
                allItems = mModel.getContent();
            }
            onNextLoaded(nextList);
            mAdapter.notifyItemRangeInserted(getStartSize(), nextList.size());
        } else {
            if (mLocalRepo.showNoDataHint()) {
                Common.showToast(getString(R.string.string_224));
            }
        }
        mRefreshLayout.finishLoadMore(true);
    }

    @Override
    protected void initData() {
        mLocalRepo = (LocalRepo<List<Item>>) mModel.getBaseRepo();
        super.initData();
    }
}
