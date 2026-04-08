package ceui.lisa.fragments

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import ceui.lisa.R
import ceui.lisa.activities.TemplateActivity
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.PivisionHAdapter
import ceui.lisa.core.BaseRepo
import ceui.lisa.databinding.FragmentPivisionHorizontalBinding
import ceui.lisa.databinding.RecyArticalHorizonBinding
import ceui.lisa.interfaces.OnItemClickListener
import ceui.lisa.model.ListArticle
import ceui.lisa.models.SpotlightArticlesBean
import ceui.lisa.repo.PivisionRepo
import ceui.lisa.utils.DensityUtil
import ceui.lisa.view.LinearItemHorizontalDecoration
import jp.wasabeef.recyclerview.animators.BaseItemAnimator

class FragmentPivisionHorizontal :
    NetListFragment<FragmentPivisionHorizontalBinding, ListArticle, SpotlightArticlesBean>() {

    override fun adapter(): BaseAdapter<SpotlightArticlesBean, RecyArticalHorizonBinding> {
        return PivisionHAdapter(allItems, mContext).setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(v: View?, position: Int, viewType: Int) {
                TemplateActivity.startWeb(
                    mContext,
                    getString(R.string.pixiv_special),
                    allItems[position].article_url,
                    true
                )
            }
        })
    }

    override fun initLayout() {
        mLayoutID = R.layout.fragment_pivision_horizontal
    }

    override fun repository(): BaseRepo = PivisionRepo("all", true)

    override fun initRecyclerView() {
        baseBind.recyclerView.addItemDecoration(LinearItemHorizontalDecoration(DensityUtil.dp2px(12.0f)))
        val manager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
        baseBind.recyclerView.layoutManager = manager
        baseBind.recyclerView.setHasFixedSize(true)
        val layoutParams = baseBind.recyclerView.layoutParams
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams.height =
            mContext.resources.getDimensionPixelSize(R.dimen.article_horizontal_height) +
            mContext.resources.getDimensionPixelSize(R.dimen.twenty_four_dp)
        baseBind.recyclerView.layoutParams = layoutParams
    }

    override fun initView() {
        super.initView()
        baseBind.seeMore.setOnClickListener {
            TemplateActivity.startPv(mContext)
        }
    }

    override fun animation(): BaseItemAnimator? = null

    override fun onFirstLoaded(spotlightArticlesBeans: List<SpotlightArticlesBean>) {
        mRefreshLayout.setEnableRefresh(true)
        mRefreshLayout.setEnableLoadMore(false)
    }

    override fun showDataBase() {
        baseBind.refreshLayout.finishRefresh(true)
        emptyRela.visibility = View.VISIBLE
    }
}
