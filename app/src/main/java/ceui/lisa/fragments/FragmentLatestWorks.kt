package ceui.lisa.fragments

import android.os.Bundle
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.IAdapter
import ceui.lisa.core.RemoteRepo
import ceui.lisa.databinding.FragmentBaseListBinding
import ceui.lisa.databinding.RecyIllustStaggerBinding
import ceui.lisa.model.ListIllust
import ceui.lisa.models.IllustsBean
import ceui.lisa.repo.LatestIllustRepo
import ceui.lisa.utils.Params

class FragmentLatestWorks : NetListFragment<FragmentBaseListBinding, ListIllust, IllustsBean>() {
    private var workType: String? = null

    companion object {
        @JvmStatic
        fun newInstance(paramWorkType: String): FragmentLatestWorks {
            val args = Bundle()
            args.putString(Params.DATA_TYPE, paramWorkType)
            val fragment = FragmentLatestWorks()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initBundle(bundle: Bundle) {
        workType = bundle.getString(Params.DATA_TYPE)
    }

    override fun repository(): RemoteRepo<ListIllust> = LatestIllustRepo(workType)

    override fun adapter(): BaseAdapter<IllustsBean, RecyIllustStaggerBinding> = IAdapter(allItems, mContext)

    override fun showToolbar(): Boolean = false

    override fun initRecyclerView() {
        staggerRecyclerView()
    }
}
