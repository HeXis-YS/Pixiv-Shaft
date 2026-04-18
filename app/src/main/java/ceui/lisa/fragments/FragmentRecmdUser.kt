package ceui.lisa.fragments

import ceui.lisa.R
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.UAdapter
import ceui.lisa.core.RemoteRepo
import ceui.lisa.databinding.FragmentBaseListBinding
import ceui.lisa.databinding.RecyUserPreviewBinding
import ceui.lisa.model.ListUser
import ceui.lisa.models.UserPreviewsBean
import ceui.lisa.repo.RecmdUserRepo

class FragmentRecmdUser() : NetListFragment<FragmentBaseListBinding, ListUser, UserPreviewsBean>() {
    private var outerItems: List<UserPreviewsBean>? = null
    private var outerNextUrl: String? = null

    constructor(items: List<UserPreviewsBean>?, url: String?) : this() {
        outerItems = items
        outerNextUrl = url
    }

    override fun repository(): RemoteRepo<ListUser> = RecmdUserRepo(false)

    override fun adapter(): BaseAdapter<UserPreviewsBean, RecyUserPreviewBinding> = UAdapter(allItems, mContext)

    override fun getToolbarTitle(): String = getString(R.string.recomment_user)

    override fun shouldAutoRefresh(): Boolean = !(allItems != null && allItems.size > 0)

    override fun lazyData() {
        if (allItems.size == 0 && !outerItems.isNullOrEmpty()) {
            allItems.addAll(outerItems!!)
            mRemoteRepo.setNextUrl(outerNextUrl)
        }
        super.lazyData()
    }
}
