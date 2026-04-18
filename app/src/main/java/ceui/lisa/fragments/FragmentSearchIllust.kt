package ceui.lisa.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import ceui.lisa.R
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.IAdapter
import ceui.lisa.core.BaseRepo
import ceui.lisa.databinding.FragmentBaseListBinding
import ceui.lisa.model.ListIllust
import ceui.lisa.models.IllustsBean
import ceui.lisa.repo.SearchIllustRepo
import ceui.lisa.utils.PixivSearchParamUtil
import ceui.lisa.viewmodel.SearchModel
import java.util.Arrays

class FragmentSearchIllust : NetListFragment<FragmentBaseListBinding, ListIllust, IllustsBean>() {
    private lateinit var searchModel: SearchModel
    private val isPopular = false

    companion object {
        @JvmStatic
        fun newInstance(popular: Boolean): FragmentSearchIllust {
            val args = Bundle()
            val fragment = FragmentSearchIllust()
            fragment.arguments = args
            return fragment
        }

        @JvmStatic
        fun newInstance(): FragmentSearchIllust = FragmentSearchIllust()
    }

    override fun initLayout() {
        mLayoutID = R.layout.fragment_base_list
    }

    override fun initModel() {
        searchModel = ViewModelProvider(requireActivity())[SearchModel::class.java]
        super.initModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchModel.nowGo.observe(viewLifecycleOwner) {
            if (!Arrays.asList(*PixivSearchParamUtil.TAG_MATCH_VALUE)
                    .contains(searchModel.searchType.value)
            ) {
                return@observe
            }
            (mRemoteRepo as SearchIllustRepo).update(searchModel)
            if (isPopular) {
                if (TextUtils.isEmpty(searchModel.keyword.value)) {
                    setEnableRefresh(false)
                    return@observe
                } else {
                    setEnableRefresh(true)
                }
            }
            autoRefresh()
        }
    }

    override fun initBundle(bundle: Bundle) {
    }

    override fun adapter(): BaseAdapter<*, out ViewBinding> = IAdapter(allItems, mContext)

    override fun repository(): BaseRepo = SearchIllustRepo(
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

    override fun initRecyclerView() {
        staggerRecyclerView()
    }
}
