package ceui.lisa.fragments

import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.NAdapter
import ceui.lisa.core.RemoteRepo
import ceui.lisa.databinding.FragmentBaseListBinding
import ceui.lisa.databinding.RecyNovelBinding
import ceui.lisa.model.ListNovel
import ceui.lisa.models.NovelBean
import ceui.lisa.repo.LatestNovelRepo

class FragmentLatestNovel : NetListFragment<FragmentBaseListBinding, ListNovel, NovelBean>() {
    override fun repository(): RemoteRepo<ListNovel> = LatestNovelRepo()

    override fun adapter(): BaseAdapter<NovelBean, RecyNovelBinding> = NAdapter(allItems, mContext)

    override fun showToolbar(): Boolean = false
}
