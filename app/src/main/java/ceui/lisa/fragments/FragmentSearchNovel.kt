package ceui.lisa.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.NAdapter
import ceui.lisa.core.BaseRepo
import ceui.lisa.databinding.FragmentBaseListBinding
import ceui.lisa.model.ListNovel
import ceui.lisa.models.NovelBean
import ceui.lisa.repo.SearchNovelRepo
import ceui.lisa.utils.PixivSearchParamUtil
import ceui.lisa.viewmodel.SearchModel
import java.util.Arrays

class FragmentSearchNovel : NetListFragment<FragmentBaseListBinding, ListNovel, NovelBean>() {
    private lateinit var searchModel: SearchModel

    companion object {
        @JvmStatic
        fun newInstance(): FragmentSearchNovel {
            val args = Bundle()
            val fragment = FragmentSearchNovel()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initModel() {
        searchModel = ViewModelProvider(requireActivity())[SearchModel::class.java]
        super.initModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchModel.nowGo.observe(viewLifecycleOwner) {
            if (!Arrays.asList(*PixivSearchParamUtil.TAG_MATCH_VALUE_NOVEL)
                    .contains(searchModel.searchType.value)
            ) {
                return@observe
            }
            (mRemoteRepo as SearchNovelRepo).update(searchModel)
            autoRefresh()
        }
    }

    override fun adapter(): BaseAdapter<*, out ViewBinding> = NAdapter(allItems, mContext)

    override fun repository(): BaseRepo = SearchNovelRepo(
        searchModel.keyword.value,
        searchModel.sortType.value,
        searchModel.searchType.value,
        searchModel.starSize.value,
        searchModel.isPremium.value,
        searchModel.startDate.value,
        searchModel.endDate.value,
        searchModel.r18Restriction.value
    )

    override fun showToolbar(): Boolean = false
}
