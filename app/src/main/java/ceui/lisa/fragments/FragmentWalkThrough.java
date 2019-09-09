package ceui.lisa.fragments;

import android.content.Intent;
import android.view.View;


import ceui.lisa.R;
import ceui.lisa.activities.ViewPagerActivity;
import ceui.lisa.adapters.IllustAdapter;
import ceui.lisa.http.Retro;
import ceui.lisa.interfaces.OnItemClickListener;
import ceui.lisa.model.IllustsBean;
import ceui.lisa.model.ListIllustResponse;
import ceui.lisa.utils.DensityUtil;
import ceui.lisa.utils.IllustChannel;
import ceui.lisa.view.GridItemDecoration;
import ceui.lisa.view.GridScrollChangeManager;
import io.reactivex.Observable;

import static ceui.lisa.activities.Shaft.sUserModel;

public class FragmentWalkThrough extends AutoClipFragment<ListIllustResponse, IllustAdapter, IllustsBean> {

    @Override
    void initLayout() {
        mLayoutID = R.layout.fragment_illust_list;
    }

    @Override
    String getToolbarTitle() {
        return "画廊";
    }

    @Override
    void initRecyclerView() {
        GridScrollChangeManager manager = new GridScrollChangeManager(mContext, 2);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addItemDecoration(new GridItemDecoration(2, DensityUtil.dp2px(4.0f), false));
    }

    @Override
    Observable<ListIllustResponse> initApi() {
        return Retro.getAppApi().getLoginBg(sUserModel.getResponse().getAccess_token());
    }

    @Override
    Observable<ListIllustResponse> initNextApi() {
        return null;
    }

    @Override
    void initAdapter() {
        mAdapter = new IllustAdapter(allItems, mContext, mRecyclerView, mRefreshLayout);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position, int viewType) {
                IllustChannel.get().setIllustList(allItems);
                Intent intent = new Intent(mContext, ViewPagerActivity.class);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });
    }
}
