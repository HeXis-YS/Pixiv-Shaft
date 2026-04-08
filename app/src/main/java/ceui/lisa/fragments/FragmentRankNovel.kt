package ceui.lisa.fragments

import android.os.Bundle
import ceui.lisa.R
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.NAdapter
import ceui.lisa.core.RemoteRepo
import ceui.lisa.databinding.FragmentBaseListBinding
import ceui.lisa.databinding.RecyNovelBinding
import ceui.lisa.model.ListNovel
import ceui.lisa.models.NovelBean
import ceui.lisa.repo.RankNovelRepo
import ceui.lisa.utils.Params

class FragmentRankNovel : NetListFragment<FragmentBaseListBinding, ListNovel, NovelBean>() {
    private var mIndex = -1
    private var queryDate = ""

    companion object {
        private val API_TITLES_VALUES = arrayOf("day", "week", "day_male", "day_female", "week_rookie", "day_r18")

        @JvmStatic
        fun newInstance(index: Int, date: String?): FragmentRankNovel {
            val args = Bundle()
            args.putInt(Params.INDEX, index)
            args.putString(Params.DAY, date)
            val fragment = FragmentRankNovel()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initBundle(bundle: Bundle) {
        mIndex = bundle.getInt(Params.INDEX)
        queryDate = bundle.getString(Params.DAY).orEmpty()
    }

    override fun showToolbar(): Boolean = false

    override fun repository(): RemoteRepo<ListNovel> = RankNovelRepo(API_TITLES_VALUES[mIndex], queryDate)

    override fun adapter(): BaseAdapter<NovelBean, RecyNovelBinding> = NAdapter(allItems, mContext)
}
