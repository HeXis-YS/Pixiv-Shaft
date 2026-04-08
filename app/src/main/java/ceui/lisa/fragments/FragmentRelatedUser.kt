package ceui.lisa.fragments

import android.os.Bundle
import ceui.lisa.R
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.UAdapter
import ceui.lisa.core.RemoteRepo
import ceui.lisa.databinding.FragmentBaseListBinding
import ceui.lisa.databinding.RecyUserPreviewBinding
import ceui.lisa.model.ListUser
import ceui.lisa.models.UserPreviewsBean
import ceui.lisa.repo.RelatedUserRepo
import ceui.lisa.utils.Params

class FragmentRelatedUser : NetListFragment<FragmentBaseListBinding, ListUser, UserPreviewsBean>() {
    private var userID = 0

    companion object {
        @JvmStatic
        fun newInstance(userID: Int): FragmentRelatedUser {
            val args = Bundle()
            args.putInt(Params.USER_ID, userID)
            val fragment = FragmentRelatedUser()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initBundle(bundle: Bundle) {
        userID = bundle.getInt(Params.USER_ID)
    }

    override fun repository(): RemoteRepo<ListUser> = RelatedUserRepo(userID)

    override fun adapter(): BaseAdapter<UserPreviewsBean, RecyUserPreviewBinding> = UAdapter(allItems, mContext)

    override fun getToolbarTitle(): String = getString(R.string.string_436)
}
