package ceui.lisa.fragments

import android.os.Bundle
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.IAdapter
import ceui.lisa.core.RemoteRepo
import ceui.lisa.databinding.FragmentBaseListBinding
import ceui.lisa.databinding.RecyIllustStaggerBinding
import ceui.lisa.model.ListIllust
import ceui.lisa.models.IllustsBean
import ceui.lisa.repo.RankIllustRepo
import ceui.lisa.utils.Params

class FragmentRankIllust : NetListFragment<FragmentBaseListBinding, ListIllust, IllustsBean>() {
    private var mIndex = -1
    private var isManga = false
    private var queryDate = ""

    companion object {
        private val API_TITLES = arrayOf(
            "day",
            "week",
            "month",
            "day_ai",
            "day_male",
            "day_female",
            "week_original",
            "week_rookie",
            "day_r18",
            "week_r18",
            "day_male_r18",
            "day_female_r18",
            "day_r18_ai",
            "week_r18g"
        )
        private val API_TITLES_MANGA = arrayOf(
            "day_manga",
            "week_manga",
            "month_manga",
            "week_rookie_manga",
            "day_r18_manga"
        )

        @JvmStatic
        fun newInstance(index: Int, date: String?, isManga: Boolean): FragmentRankIllust {
            val args = Bundle()
            args.putInt(Params.INDEX, index)
            args.putBoolean(Params.MANGA, isManga)
            args.putString(Params.DAY, date)
            val fragment = FragmentRankIllust()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initBundle(bundle: Bundle) {
        mIndex = bundle.getInt(Params.INDEX)
        queryDate = bundle.getString(Params.DAY).orEmpty()
        isManga = bundle.getBoolean(Params.MANGA)
    }

    override fun showToolbar(): Boolean = false

    override fun repository(): RemoteRepo<ListIllust> =
        RankIllustRepo(if (isManga) API_TITLES_MANGA[mIndex] else API_TITLES[mIndex], queryDate)

    override fun adapter(): BaseAdapter<IllustsBean, RecyIllustStaggerBinding> = IAdapter(allItems, mContext)

    override fun initRecyclerView() {
        staggerRecyclerView()
    }
}
