package ceui.lisa.fragments

import androidx.recyclerview.widget.LinearLayoutManager
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.NAdapterWithHeadView
import ceui.lisa.core.RemoteRepo
import ceui.lisa.databinding.FragmentBaseListBinding
import ceui.lisa.databinding.RecyNovelBinding
import ceui.lisa.model.ListNovel
import ceui.lisa.models.NovelBean
import ceui.lisa.repo.RecmdNovelRepo
import ceui.lisa.utils.DensityUtil
import ceui.lisa.view.LinearItemWithHeadDecoration

class FragmentRecmdNovel : NetListFragment<FragmentBaseListBinding, ListNovel, NovelBean>() {
    private val ranking = ArrayList<NovelBean>()

    override fun repository(): RemoteRepo<ListNovel> = RecmdNovelRepo()

    override fun adapter(): BaseAdapter<NovelBean, RecyNovelBinding> = NAdapterWithHeadView(allItems, mContext)

    override fun showToolbar(): Boolean = false

    override fun onFirstLoaded(novelBeans: List<NovelBean>) {
        ranking.clear()
        ranking.addAll(mResponse.ranking_novels.orEmpty())
        (mAdapter as NAdapterWithHeadView).setHeadData(ranking)
    }

    override fun initRecyclerView() {
        mRecyclerView.layoutManager = LinearLayoutManager(mContext)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.addItemDecoration(LinearItemWithHeadDecoration(DensityUtil.dp2px(12.0f)))
    }
}
